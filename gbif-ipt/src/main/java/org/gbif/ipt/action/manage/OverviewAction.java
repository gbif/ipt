/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.MaintUpFreqType;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.EmlValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;

public class OverviewAction extends ManagerBaseAction {

  // logging
  private static final Logger LOG = Logger.getLogger(OverviewAction.class);

  private static final String PUBLISHING = "publishing";
  private final UserAccountManager userManager;
  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private List<User> potentialManagers;
  private List<Extension> potentialExtensions;
  private List<Organisation> organisations;
  private final EmlValidator emlValidator;
  private boolean missingMetadata;
  private boolean missingRegistrationMetadata;
  private StatusReport report;
  private Date now;
  private boolean unpublish = false;
  private Map<String, String> frequencies;

  @Inject
  public OverviewAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, UserAccountManager userAccountManager, ExtensionManager extensionManager,
    VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.userManager = userAccountManager;
    this.extensionManager = extensionManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
    this.vocabManager = vocabManager;
  }

  /**
   * Triggered by add manager button on manage resource page.
   */
  public String addManager() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    User u = userManager.get(id);
    if (u != null && !potentialManagers.contains(u)) {
      addActionError(getText("manage.overview.manager.not.available", new String[] {id}));
    } else if (u != null) {
      resource.addManager(u);
      addActionMessage(getText("manage.overview.user.added", new String[] {u.getName()}));
      saveResource();
      potentialManagers.remove(u);
    }
    return execute();
  }

  /**
   * Cancel resource publication. Publication is all or nothing. If incomplete, the version number of the resource
   * must be rolled back.
   * 
   * @return Struts2 result string
   */
  public String cancel() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    boolean cancelled = resourceManager.cancelPublishing(resource.getShortname(), this);
    if (cancelled) {

      // final logging
      String sVersion = String.valueOf(resource.getEmlVersion());
      String msg = getText("publishing.cancelled", new String[] {sVersion, resource.getShortname()});
      LOG.warn(msg);
      addActionError(msg);

      // restore the previous version of the resource
      resourceManager.restoreVersion(resource, resource.getLastVersion(), this);

      // Struts finishes before callable has a finish to update status report, therefore,
      // temporarily override StatusReport so that Overview page report displaying up-to-date STATE and Exception
      StatusReport tmpReport = new StatusReport(true, GenerateDwca.CANCELLED_STATE_MSG, report.getMessages());
      report = tmpReport;
      return execute();
    }
    addActionError(getText("manage.overview.failed.stop.publishing"));
    return ERROR;
  }

  @Override
  public String delete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      Resource res = resource;
      resourceManager.delete(res);
      addActionMessage(getText("manage.overview.resource.deleted", new String[] {res.toString()}));
      return HOME;
    } catch (IOException e) {
      String msg = getText("manage.resource.delete.failed");
      LOG.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    } catch (DeletionNotAllowedException e) {
      String msg = getText("manage.resource.delete.failed");
      LOG.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    }

    return SUCCESS;
  }

  /**
   * Triggered by delete manager link on manage resource page.
   */
  public String deleteManager() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    User u = userManager.get(id);
    if (u == null || !resource.getManagers().contains(u)) {
      addActionError(getText("manage.overview.manager.not.available", new String[] {id}));
    } else {
      resource.getManagers().remove(u);
      addActionMessage(getText("manage.overview.user.removed", new String[] {u.getName()}));
      saveResource();
      potentialManagers.add(u);
    }
    return execute();
  }

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Validate whether or not to show a confirmation message to overwrite the file(s) recently uploaded.
   * 
   * @return true if a file exist in the user session. False otherwise.
   */
  public boolean getConfirmOverwrite() {
    return session.get(Constants.SESSION_FILE) != null;
  }

  /**
   * Calculate the size of the DwC-A file.
   * 
   * @return the size (human readable) of the DwC-A file.
   */
  public String getDwcaFormattedSize() {
    return FileUtils.formatSize(resourceManager.getDwcaSize(resource), 2);
  }

  /**
   * Calculate the size of the EML file.
   * 
   * @return the size (human readable) of the EML file.
   */
  public String getEmlFormattedSize() {
    return FileUtils.formatSize(resourceManager.getEmlSize(resource), 2);
  }

  /**
   * On the manage resource page page, this map is used to populate the publishing intervals dropdown.
   * 
   * @return update frequencies map
   */
  public Map<String, String> getFrequencies() {
    return frequencies;
  }

  public boolean getMissingBasicMetadata() {
    return !emlValidator.isValid(resource.getEml(), "basic");
  }

  /**
   * @return true if there are something missing metadata. False otherwise.
   */
  public boolean getMissingRegistrationMetadata() {
    return missingRegistrationMetadata;
  }

  public Date getNow() {
    return now;
  }

  public List<Organisation> getOrganisations() {
    return organisations;
  }

  public List<Extension> getPotentialExtensions() {
    return potentialExtensions;
  }

  public List<User> getPotentialManagers() {
    return potentialManagers;
  }

  public StatusReport getReport() {
    return report;
  }

  /**
   * Calculate the size of the RTF file.
   * 
   * @return return the size (human readable) of the RTF file.
   */
  public String getRtfFormattedSize() {
    return FileUtils.formatSize(resourceManager.getRtfSize(resource), 2);
  }

  /**
   * Checks if the resource meets all the conditions required in order to be registered. For example, the resource needs
   * to be published prior to registering with the GBIF Network.
   * 
   * @param resource resource
   * @return true if the resource meets the minimum requirements to be published
   */
  private boolean hasMinimumRegistryInfo(Resource resource) {
    if (resource == null) {
      return false;
    }
    if (resource.getEml() == null) {
      return false;
    }
    if (resource.getCreator() == null) {
      return false;
    }
    if (resource.getCreator().getEmail() == null) {
      return false;
    }
    if (!resource.isPublished()) {
      return false;
    }
    return true;
  }

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  public boolean isRtfFileExisting() {
    return resourceManager.isRtfExisting(resource.getShortname());
  }

  public String locked() throws Exception {
    now = new Date();
    if (report != null && report.isCompleted()) {
      addActionMessage(getText("manage.overview.resource.published"));
      return "cancel";
    }
    return SUCCESS;
  }

  public String makePrivate() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        // makePrivate
        try {
          resourceManager.visibilityToPrivate(resource, this);
          addActionMessage(getText("manage.overview.changed.publication.status", new String[] {resource.getStatus()
            .toString()}));
        } catch (InvalidConfigException e) {
          LOG.error("Cant unpublish resource " + resource, e);
        }
      } else {
        addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
          resource.getStatus().toString()}));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
        resource.getStatus().toString()}));
    }
    return execute();
  }

  public String makePublic() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PRIVATE == resource.getStatus()) {
      try {
        resourceManager.visibilityToPublic(resource, this);
        addActionMessage(getText("manage.overview.changed.publication.status", new String[] {resource.getStatus()
          .toString()}));
      } catch (InvalidConfigException e) {
        LOG.error("Cant publish resource " + resource, e);
      }

    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
        resource.getStatus().toString()}));
    }
    return execute();
  }

  /**
   * Populate frequencies map, representing the publishing interval choices uses have when configuring
   * auto-publishing. The frequencies list is derived from an XML vocabulary, and will contain values in the requested
   * locale, defaulting to English.
   */
  private void populateFrequencies() {
    frequencies = new LinkedHashMap<String, String>();

    if (resource.usesAutoPublishing()) {
      frequencies.put("off", getText("autopublish.off"));
    } else {
      frequencies.put("", getText("autopublish.interval"));
    }

    // update frequencies list, that qualify for auto-publishing
    Map<String, String> filteredFrequencies =
      vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintUpFreqType.AUTO_PUBLISHING_TYPES);
    frequencies.putAll(filteredFrequencies);
  }

  @Override
  public void prepare() {
    super.prepare();
    if (resource != null) {
      // get last archive report
      report = resourceManager.status(resource.getShortname());
      // get potential new managers
      potentialManagers = userManager.list(Role.Publisher);
      potentialManagers.addAll(userManager.list(Role.Manager));
      // remove already associated ones
      for (User u : resource.getManagers()) {
        potentialManagers.remove(u);
      }

      // enabled registry organisations
      organisations = registrationManager.list();

      // remove all DwC mappings with 0 terms mapped
      // this is important do do before populating potential extensions since an empty mapping to occurrence can
      // indicate the resource hasCore is true
      for (ExtensionMapping em : resource.getCoreMappings()) {
        if (em.getFields().isEmpty()) {
          resource.deleteMapping(em);
        }
      }

      // does the resource already have a source mapped to core type?
      if (resource.hasCore()) {
        // show extensions suited for this core
        potentialExtensions = extensionManager.list(resource.getCoreRowType());

        // add core itself
        Extension core = extensionManager.get(resource.getCoreRowType());
        if (core == null) {
          addActionError(getText("manage.overview.no.DwC.extension", new String[] {resource.getCoreRowType()}));
        } else {
          potentialExtensions.add(0, core);
        }
      }
      // has no source been added yet that can be mapped? If not, return empty list of Extensions
      else if (resource.getSources().isEmpty()) {
        potentialExtensions = new ArrayList<Extension>();
      }
      // does the resource have no core type, but at least one source file ready to be mapped?
      else {
        LOG.debug("Resource has core type: " + resource.getCoreType());

        if (Strings.isNullOrEmpty(resource.getCoreType()) || "Other".equalsIgnoreCase(resource.getCoreType())) {
          // populate list of potential list of extensions with core types
          potentialExtensions = extensionManager.listCore();
          LOG.debug("Cores suitable for Other resources: " + potentialExtensions);
          if (potentialExtensions.isEmpty()) {
            addActionError(getText("manage.overview.no.DwC.extensions"));
          }
        } else {
          potentialExtensions = new ArrayList<Extension>();
          // the core type (from metadata pages) determines which core type extension to include
          if (resource.getCoreType().equalsIgnoreCase(CoreRowType.CHECKLIST.toString())) {
            Extension ext = extensionManager.get(Constants.DWC_ROWTYPE_TAXON);
            if (ext == null) {
              addActionError(getText("manage.overview.no.DwC.extension", new String[] {Constants.DWC_ROWTYPE_TAXON}));
            } else {
              potentialExtensions.add(ext);
            }
          } else if (resource.getCoreType().equalsIgnoreCase(CoreRowType.OCCURRENCE.toString())) {
            Extension ext = extensionManager.get(Constants.DWC_ROWTYPE_OCCURRENCE);
            if (ext == null) {
              addActionError(getText("manage.overview.no.DwC.extension",
                new String[] {Constants.DWC_ROWTYPE_OCCURRENCE}));
            } else {
              potentialExtensions.add(ext);
            }
          }
        }
      }
      // check EML
      missingMetadata = !emlValidator.isValid(resource.getEml(), null);
      missingRegistrationMetadata = !hasMinimumRegistryInfo(resource);

      // populate frequencies map
      populateFrequencies();
    }
  }

  /**
   * Executes the instruction to publish the resource.
   * </br>
   * In addition, the method must check if resource has been configured to be auto-published.
   * </br>
   * In addition, the method must clear the processFailures for the resource being published. If a resource has
   * exceeded the maximum number of failed publish events during auto-publication, auto-publication for the resource is
   * suspended. By publishing the resource manually, it is assumed the manager is trying to debug the problem. Without
   * this safeguard in place, a resource can auto-publish in an endless number of failures.
   * 
   * @return Struts2 result string
   * @throws Exception if method fails
   */
  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }

    // clear the processFailures for the resource, allowing auto-publication to proceed
    if (resourceManager.getProcessFailures().containsKey(resource.getShortname())) {
      logProcessFailures(resource);
      LOG.info("Clearing publish event failures for resource: " + resource.getTitleAndShortname());
      resourceManager.getProcessFailures().removeAll(resource.getShortname());
    }

    // look for parameters publication mode and publication frequency
    String pm = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_PUBLICATION_MODE));
    if (!Strings.isNullOrEmpty(pm)) {
      try {
        // auto-publishing being turned OFF
        if (PublicationMode.AUTO_PUBLISH_OFF == PublicationMode.valueOf(pm) && resource.usesAutoPublishing()) {
          resourceManager.publicationModeToOff(resource);
        }
        // auto-publishing being turned ON, or auto-publishing settings being updated
        else {
          String pf = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_PUBLICATION_FREQUENCY));
          if (!Strings.isNullOrEmpty(pf)) {
            resource.setUpdateFrequency(pf);
            resource.setPublicationMode(PublicationMode.valueOf(pm));
          } else {
            LOG.debug("No change to auto-publishing settings");
          }
        }
      } catch (IllegalArgumentException e) {
        LOG.error("Exception encountered while parsing parameters: " + e.getMessage(), e);
      } finally {
        // update frequencies map
        populateFrequencies();
      }
    }

    // increment version number - this will be the version of newly published resource (all of eml/rtf/dwca)
    int v = resource.getNextVersion();

    try {
      // publish a new version of the resource
      if (resourceManager.publish(resource, v, this)) {
        addActionMessage(getText("publishing.started", new String[] {String.valueOf(v), resource.getShortname()}));
        return PUBLISHING;
      } else {
        // show action warning there is no source data and mapping, as long as resource isn't metadata-only
        if (!resource.getCoreType().equalsIgnoreCase(Constants.DATASET_TYPE_METADATA_IDENTIFIER)) {
          addActionWarning(getText("manage.overview.data.missing"));
        }
        missingRegistrationMetadata = !hasMinimumRegistryInfo(resource);
        return SUCCESS;
      }
    } catch (PublicationException e) {
      if (PublicationException.TYPE.LOCKED == e.getType()) {
        addActionError(getText("manage.overview.resource.being.published",
          new String[] {resource.getTitleAndShortname()}));
      } else {
        // alert user publication failed
        addActionError(getText("publishing.failed",
          new String[] {String.valueOf(v), resource.getShortname(), e.getMessage()}));
        // restore the previous version since publication was unsuccessful
        resourceManager.restoreVersion(resource, v - 1, this);
        // keep track of how many failures on auto publication have happened
        resourceManager.getProcessFailures().put(resource.getShortname(), new Date());
      }
    } catch (InvalidConfigException e) {
      // with this type of error, the version cannot be rolled back - just alert user publication failed
      String msg =
        getText("publishing.failed", new String[] {String.valueOf(v), resource.getShortname(), e.getMessage()});
      LOG.error(msg, e);
      addActionError(msg);
    }
    return ERROR;
  }

  public String registerResource() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
          resource.getStatus().toString()}));

      } else {
        // plain managers are not allowed to register a resource
        if (getCurrentUser().hasRegistrationRights()) {
          Organisation org = null;
          try {
            org = registrationManager.get(id);

            // http://code.google.com/p/gbif-providertoolkit/issues/detail?id=594
            // It is safe to test the Organisation here. A resource cannot be registered without an organisation being
            // provided, and the issue 594 is an example how one can produce this sequence of events. A more
            // robust improvement might be to submit a state transition from the form "makePublic",
            // "makePrivate" which would be more atomic.
            if (org == null) {
              return execute();
            }

            // perform registration
            resourceManager.register(resource, org, registrationManager.getIpt(), this);
          } catch (InvalidConfigException e) {
            if (e.getType() == InvalidConfigException.TYPE.INVALID_RESOURCE_MIGRATION) {
              String msg = getText("manage.resource.migrate.failed");
              // concatenate reason
              msg = (Strings.isNullOrEmpty(msg)) ? e.getMessage() : msg + ": " + e.getMessage();
              addActionError(msg);
              LOG.error(msg);
            } else {
              String msg = getText("manage.overview.failed.resource.registration");
              addActionError(msg);
              LOG.error(msg);
            }
          } catch (RegistryException e) {
            // log as specific error message as possible about why the Registry error occurred
            String msg = RegistryException.logRegistryException(e.getType(), this);
            // add error message about Registry error
            addActionError(msg);
            LOG.error(msg);

            // add error message that explains the consequence of the Registry error
            msg = getText("manage.overview.failed.resource.registration");
            addActionError(msg);
            LOG.error(msg);
          }
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append(getText("manage.resource.status.registration.forbidden"));
          sb.append(" ");
          sb.append(getText("manage.resource.role.change"));
          addActionError(sb.toString());
        }
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
        resource.getStatus().toString()}));
    }
    return execute();
  }

  public void setUnpublish(String unpublish) {
    this.unpublish = StringUtils.trimToNull(unpublish) != null;
  }

  /**
   * Log how many times publication has failed for a resource, also detailing when the failures occurred.
   * 
   * @param resource resource
   */
  @VisibleForTesting
  protected void logProcessFailures(Resource resource) {
    StringBuilder sb = new StringBuilder();
    sb.append("Resource [");
    sb.append(resource.getTitleAndShortname());
    sb.append("] has ");
    if (resourceManager.getProcessFailures().containsKey(resource.getShortname())) {
      List<Date> failures = resourceManager.getProcessFailures().get(resource.getShortname());
      sb.append(String.valueOf(failures.size()));
      sb.append(" failed publications on: ");
      Iterator<Date> iter = failures.iterator();
      while (iter.hasNext()) {
        sb.append(DateFormatUtils.format(iter.next(), "yyyy-MM-dd HH:mm:SS"));
        if (iter.hasNext()) {
          sb.append(", ");
        } else {
          sb.append(".");
        }
      }
    } else {
      sb.append("0 failed publications");
    }
    LOG.debug(sb.toString());
  }
}
