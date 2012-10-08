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
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.ipt.validation.EmlValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class OverviewAction extends ManagerBaseAction {

  // logging
  private static final Logger log = Logger.getLogger(OverviewAction.class);

  private static final String PUBLISHING = "publishing";
  private UserAccountManager userManager;
  private ExtensionManager extensionManager;
  private List<User> potentialManagers;
  private List<Extension> potentialExtensions;
  private List<Organisation> organisations;
  private EmlValidator emlValidator;
  private boolean missingMetadata;
  private boolean missingRegistrationMetadata;
  private StatusReport report;
  private Date now;
  private boolean unpublish = false;

  @Inject
  public OverviewAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, UserAccountManager userAccountManager, ExtensionManager extensionManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.userManager = userAccountManager;
    this.extensionManager = extensionManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
  }

  public String addmanager() throws Exception {
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

  public String cancel() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }

    try {
      resourceManager.cancelPublishing(resource.getShortname(), this);
      addActionMessage(getText("manage.overview.stopped.publishing", new String[] {resource.toString()}));
    } catch (PublicationException e) {
      String reason = "";
      if (e.getMessage() != null) {
        reason = e.getMessage();
      }
      addActionError(getText("manage.overview.failed.stop.publishing", new String[] {reason}));
      return ERROR;
    }
    return execute();
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
      log.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    } catch (DeletionNotAllowedException e) {
      String msg = getText("manage.resource.delete.failed");
      log.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    }

    return SUCCESS;
  }

  public String delmanager() throws Exception {
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
          addActionMessage(
            getText("manage.overview.changed.publication.status", new String[] {resource.getStatus().toString()}));
        } catch (InvalidConfigException e) {
          log.error("Cant unpublish resource " + resource, e);
        }
      } else {
        addActionWarning(getText("manage.overview.resource.invalid.operation",
          new String[] {resource.getShortname(), resource.getStatus().toString()}));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation",
        new String[] {resource.getShortname(), resource.getStatus().toString()}));
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
        addActionMessage(
          getText("manage.overview.changed.publication.status", new String[] {resource.getStatus().toString()}));
      } catch (InvalidConfigException e) {
        log.error("Cant publish resource " + resource, e);
      }

    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation",
        new String[] {resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

  private boolean minimumRegistryInfo(Resource resource) {
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
        if (Strings.isNullOrEmpty(resource.getCoreType()) || "Other".equalsIgnoreCase(resource.getCoreType())) {
          // populate list of potential list of extensions with core types
          potentialExtensions = extensionManager.listCore();
          if (potentialExtensions.size() == 0) {
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
              addActionError(getText("manage.overview.no.DwC.extension", new String[] {Constants.DWC_ROWTYPE_OCCURRENCE}));
            } else {
              potentialExtensions.add(ext);
            }
          }
        }
      }
      // check EML
      missingMetadata = !emlValidator.isValid(resource.getEml(), null);
      missingRegistrationMetadata = !minimumRegistryInfo(resource);

      // remove all DwC mappings with 0 terms mapped
      for (ExtensionMapping em : resource.getCoreMappings()) {
        if (em.getFields().isEmpty()) {
          resource.deleteMapping(em);
        }
      }
    }
  }

  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      // Update the Resource's Registration, if the Resource has been registered. If successful, broadcast with a msg.
      // It could be the resource is a metadata-only resource, but its registration should always be updated on publish.
      if (resource.isRegistered()) {
        resourceManager.updateRegistration(resource);
        addActionMessage(getText("manage.overview.resource.update.registration", new String[] {resource.getTitle()}));
      }

      // Publish the Resource
      if (resourceManager.publish(resource, this)) {
        addActionMessage(getText("manage.overview.publishing.resource.version",
          new String[] {Integer.toString(resource.getEmlVersion())}));
        return PUBLISHING;
      } else {
        if (resource.hasMappedData()) {
          addActionWarning(getText("manage.overview.no.data.archive.generated"));
        } else {
          addActionWarning(getText("manage.overview.data.missing"));
        }
        addActionMessage(
          getText("manage.overview.published.eml.version", new String[] {String.valueOf(resource.getEmlVersion())}));
        missingRegistrationMetadata = !minimumRegistryInfo(resource);
        return SUCCESS;
      }

    } catch (PublicationException e) {
      if (PublicationException.TYPE.LOCKED == e.getType()) {
        addActionWarning(getText("manage.overview.resource.being.published"));
      } else {
        addActionWarning(getText("manage.overview.publishing.error"), e);
      }
    } catch (Exception e) {
      log.error("Error publishing resource", e);
      addActionWarning(getText("manage.overview.publishing.error"), e);
    }
    return ERROR;
  }

  public String registerResource() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        addActionWarning(getText("manage.overview.resource.invalid.operation",
          new String[] {resource.getShortname(), resource.getStatus().toString()}));

      } else {
        // plain managers are not allowed to register a resource
        if (getCurrentUser().hasRegistrationRights()) {
          Organisation org = null;
          try {
            org = registrationManager.get(id);

            // http://code.google.com/p/gbif-providertoolkit/issues/detail?id=594
            // It is safe to test the Organisation here. A resource
            // cannot be registered
            // without an organisation being provided, and the issue
            // 594 is an example
            // how one can produce this sequence of events. A more
            // robust improvement
            // would might be to submit a state transition from the
            // form "makePublic",
            // "makePrivate" which would be more atomic.
            if (org == null) {
              return execute();
            }

            resourceManager.register(resource, org, registrationManager.getIpt(), this);
            addActionMessage(getText("manage.overview.resource.registered", new String[] {org.getName()}));

          } catch (RegistryException e) {
            log.error("Cant register resource " + resource + " with organisation " + org, e);
            addActionError(getText("manage.overview.failed.resource.registration"));
          }
        } else {
          addActionError(getText("manage.resource.status.registration.forbidden"));
        }
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation",
        new String[] {resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

  public void setUnpublish(String unpublish) {
    this.unpublish = StringUtils.trimToNull(unpublish) != null;
  }
}
