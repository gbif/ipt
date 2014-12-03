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

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.file.CSVReader;
import org.gbif.file.CSVReaderFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.IdentifierStatus;
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
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.task.TaskMessage;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static org.gbif.ipt.task.GenerateDwca.CHARACTER_ENCODING;

public class OverviewAction extends ManagerBaseAction implements ReportHandler {

  // logging
  private static final Logger LOG = Logger.getLogger(OverviewAction.class);

  private static final String PUBLISHING = "publishing";
  private final UserAccountManager userManager;
  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private List<User> potentialManagers;
  private List<Extension> potentialCores;
  private List<Extension> potentialExtensions;
  private List<Organisation> organisations;
  private Organisation organisationWithPrimaryDoiAccount;
  private final EmlValidator emlValidator;
  private boolean missingMetadata;
  private boolean missingRegistrationMetadata;
  private boolean metadataModifiedSinceLastPublication;
  private boolean mappingsModifiedSinceLastPublication;
  private boolean sourcesModifiedSinceLastPublication;
  private StatusReport report;
  private Date now;
  private boolean unpublish = false;
  private boolean reserveDoi = false;
  private boolean deleteDoi = false;
  private boolean delete = false;
  private boolean undelete = false;
  private boolean publish = false;
  private String summary;
  private Map<String, String> frequencies;
  // preview
  private GenerateDwcaFactory dwcaFactory;
  private List<String> columns;
  private List<String[]> peek;
  private Integer mid;
  private static final int PEEK_ROWS = 100;

  @Inject
  public OverviewAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, UserAccountManager userAccountManager, ExtensionManager extensionManager,
    VocabulariesManager vocabManager, GenerateDwcaFactory dwcaFactory) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.userManager = userAccountManager;
    this.extensionManager = extensionManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
    this.vocabManager = vocabManager;
    this.dwcaFactory = dwcaFactory;
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
      BigDecimal version = resource.getEmlVersion();
      String msg = getText("publishing.cancelled", new String[] {version.toPlainString(), resource.getShortname()});
      LOG.warn(msg);
      addActionError(msg);

      // restore the previous version of the resource
      resourceManager.restoreVersion(resource, version, resource.getReplacedEmlVersion(), this);

      // Struts finishes before callable has a finish to update status report, therefore,
      // temporarily override StatusReport so that Overview page report displaying up-to-date STATE and Exception
      StatusReport tmpReport = new StatusReport(true, GenerateDwca.CANCELLED_STATE_MSG, report.getMessages());
      report = tmpReport;
      return execute();
    }
    addActionError(getText("manage.overview.failed.stop.publishing"));
    return ERROR;
  }

  /**
   * TODO Deleting a resource with a DOI: a) makes the DOI unavailable, b) deletes the resource from GBIF (if its
   * registered with GBIF), c) preserves the dataset and all its archived versions in the IPT
   */
  @Override
  public String delete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (delete) {
      try {
        Resource res = resource;
        if (isAlreadyAssignedDoi()) {
          // TODO
          resource.setStatus(PublicationStatus.DELETED);
          saveResource();
          addActionMessage(getText("manage.overview.resource.deleted", new String[] {res.toString()}));
        } else {
          resourceManager.delete(res);
        }
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
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
        resource.getStatus().toString()}));
    }
    return SUCCESS;
  }

  /**
   * TODO Undeleting a resource with a DOI: a) makes the DOI available, b) undeletes the resource from GBIF (if it was registered with GBIF)
   * It must check that the publishing organisation is still associated to the IPT. Alternatively, cannot delete an organisation from IPT
   * if it still has a deleted but public resource.
   */
  public String undelete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (undelete) {
      try {
        Resource res = resource;
        //resourceManager.undelete(res);
        // TODO handle DOI
        // TODO set to REGISTERED if it was registered, PUBLIC otherwise
        res.setStatus(PublicationStatus.PUBLIC);
        saveResource();
        addActionMessage(getText("manage.overview.resource.undeleted", new String[] {res.toString()}));
        return SUCCESS;
      } catch (Exception e) {
        String msg = getText("manage.resource.undelete.failed");
        LOG.error(msg, e);
        addActionError(msg);
        addActionExceptionWarning(e);
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
        resource.getStatus().toString()}));
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
   * On the manage resource page page, this map is used to populate the publishing intervals dropdown.
   *
   * @return update frequencies map
   */
  public Map<String, String> getFrequencies() {
    return frequencies;
  }

  /**
   * Determine whether the metadata has been modified since the last publication.
   *
   * @param resource resource
   *
   * @return true if metadata has been modified since last publication, false otherwise
   */
  public boolean setMetadataModifiedSinceLastPublication(@NotNull Resource resource) {
    if (resource.getLastPublished() == null) {
      return resource.getMetadataModified() != null;
    } else {
      if (resource.getMetadataModified() != null) {
        return resource.getMetadataModified().compareTo(resource.getLastPublished()) > 0;
      }
    }
    return false;
  }

  /**
   * Determine whether the source mappings has been modified since the last publication.
   *
   * @param resource resource
   *
   * @return true if source mappings has been modified since last publication, false otherwise
   */
  public boolean setMappingsModifiedSinceLastPublication(@NotNull Resource resource) {
    if (resource.getLastPublished() == null) {
      return resource.getMappingsModified() != null;
    } else {
      if (resource.getMappingsModified() != null) {
        return resource.getMappingsModified().compareTo(resource.getLastPublished()) > 0;
      }
    }
    return false;
  }

  /**
   * Determine whether the sources have been modified since the last publication.
   *
   * @param resource resource
   *
   * @return true if sources have been modified since last publication, false otherwise
   */
  public boolean setSourcesModifiedSinceLastPublication(@NotNull Resource resource) {
    if (resource.getLastPublished() == null) {
      return resource.getSourcesModified() != null;
    } else {
      if (resource.getSourcesModified() != null) {
        return resource.getSourcesModified().compareTo(resource.getLastPublished()) > 0;
      }
    }
    return false;
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

  public List<Extension> getPotentialCores() {
    return potentialCores;
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
   * Checks if the resource meets all the conditions required in order to be registered. For example, the resource needs
   * to be published prior to registering with the GBIF Network.
   *
   * @param resource resource
   * @return true if the resource meets the minimum requirements to be published
   */
  private boolean hasMinimumRegistryInfo(Resource resource) {
    if (missingMetadata) {
      return false;
    }
    return resource.isPublished();
  }

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  public String locked() throws Exception {
    now = new Date();
    if (report != null && report.isCompleted()) {
      addActionMessage(getText("manage.overview.resource.published"));
      return "cancel";
    }
    return SUCCESS;
  }

  /**
   * Change the visibility of a resource from public to private. This operation cannot be performed, if the resource
   * has been assigned a DOI (DOI that is registered, not reserved), or if the resource has been registered with GBIF.
   */
  public String makePrivate() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (unpublish) {
      if (PublicationStatus.PUBLIC == resource.getStatus() && !isAlreadyAssignedDoi()) {
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
   * Reserve a DOI for a resource. Constructs the DOI metadata document, and registers it without making it public.
   *
   * Can be done for any resource with any status.
   *
   * To accommodate resources with existing DOIs, this method checks if the resource has an existing DOI.
   * If the prefix of the existing DOI matches the prefix of the IPT primary DOI account, the DOI will be automatically
   * reused. Otherwise, the user must remove the DOI to reserve a new one, or update the DOI prefix used by the IPT
   * primary DOI account.
   *
   * Must add DOI to EML alternative identifiers, and set DOI as EML citation identifier (unpublished EML)
   *
   * DOI can be used on mapping core (datasetID field).
   *
   * If the resource has an existing DOI already, its an indication the resource is being transitioned to a new DOI.
   * In this case, the previous DOI must be replaced by the new DOI.
   */
  public String reserveDoi() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (reserveDoi) {
      try {
        if (resource.getIdentifierStatus() == IdentifierStatus.UNRESERVED && !isAlreadyAssignedDoi()) {
          String existingDoi = findExistingDoi(resource);
          if (existingDoi == null) {
            resource.setDoi(makeDoi());
            resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
            saveResource();
          } else {
            String prefix = parseDoiPrefix(existingDoi);
            String prefixAllowed = organisationWithPrimaryDoiAccount.getDoiPrefix();
            // is the prefix of the existing DOI equal to the prefix assigned to the DOI account configured for this IPT?
            if (prefix != null && prefixAllowed != null && prefix.equals(prefixAllowed)) {
              // TODO: test this DOI actually exists
              resource.setDoi(existingDoi);
              resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
              saveResource();
              addActionMessage(getText("manage.overview.publishing.doi.reserve.reused", new String[] {existingDoi}));
            } else {
              addActionError(getText("manage.overview.publishing.doi.reserve.notRreused", new String[] {existingDoi,
                prefixAllowed}));
            }
          }
        } else if (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC && isAlreadyAssignedDoi()) {
          resource.setDoi(makeDoi());
          resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
          saveResource();
        }

      } catch (Exception e) {
        LOG.error("Failed to reserve DOI for resource " + resource.getShortname(), e);
      }
    } else {
      addActionWarning(getText("manage.overview.resource.doi.invalid.operation", new String[] {resource.getShortname(),
        resource.getIdentifierStatus().toString()}));
    }
    return execute();
  }

  /**
   * Return the existing DOI assigned to this resource. An existing DOI is set as the citation identifier.
   * The citation identifier is determined to be a DOI, if the identifier starts with "doi:", or if it starts with
   * "http://dx.doi.org/" - the DOI Proxy server which resolves DOIs. Be sure to strip the "doi:" or
   * "http://dx.doi.org/" before returning it though.
   *
   * @return the existing DOI assigned to this resource, or null if none was found.
   */
  @VisibleForTesting
  public String findExistingDoi(Resource resource) {
    if (resource != null && resource.getEml() != null) {
      Citation citation = resource.getEml().getCitation();
      if (citation != null) {
        String identifier = StringUtils.trimToEmpty(citation.getIdentifier()).toLowerCase();
        if (identifier.startsWith(Constants.DOI_ACCESS_SCHEMA)) {
          return citation.getIdentifier().substring(Constants.DOI_ACCESS_SCHEMA.length());
        } else if (identifier.startsWith(Constants.DOI_PROXY_SERVER_URL)) {
          return citation.getIdentifier().substring(Constants.DOI_PROXY_SERVER_URL.length());
        }
      }
    }
    return null;
  }

  /**
   * Deletes a reserved DOI.
   *
   * Can only be done to a resource whose DOI is reserved but not public. If the resource previously had been assigned
   * a DOI, that DOI is reassigned.
   */
  public String deleteDoi() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (deleteDoi) {
      try {
        if (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC_PENDING_PUBLICATION) {
          if (isAlreadyAssignedDoi()) {
            // reassign previous DOI, and reset identifier status
            resource.setDoi(resource.getVersionHistory().get(0).getDoi());
            resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
            saveResource();
          } else {
            resource.setDoi(null);
            resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);
            saveResource();
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to delete reserved DOI for resource " + resource.getShortname(), e);
      }
    } else {
      addActionWarning(getText("manage.overview.resource.doi.invalid.operation", new String[] {resource.getShortname(),
        resource.getIdentifierStatus().toString()}));
    }
    return execute();
  }

  /**
   * Take a resource offline, and ensure its DOI resolves to a page explaining the resource has been removed. If the
   * resource was registered with GBIF, it is deleted from GBIF.
   *
   * The resource does not have to be republished.
   *
   * DOI can no longer be used on mapping core (datasetID field).
   *
   * This changes the resource status to private.
   *
   * Can be done for any non-private resource whose DOI has been registered.
   */
  private void makeDoiUnavailable() throws Exception {
    resource.setIdentifierStatus(IdentifierStatus.UNAVAILABLE);
    resourceManager.updateAlternateIdentifierForDOI(resource);
    saveResource();
  }

  /**
   * Take a resource online again, and ensure its DOI resolves to the resource homepage of the last published version.
   * If the resource was registered with GBIF, it will required written communication with the GBIF Helpdesk.
   *
   * The resource does not have to be republished.
   *
   * DOI can be used on mapping core (datasetID field).
   *
   * This changes the resource status to public, unless the resource was previously registered in which case it will be
   * set to registered again.
   *
   * Can be done for any private resource, whose DOI has been made unavailable.
   */
  private void makeDoiAvailable() throws Exception {
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    resourceManager.updateAlternateIdentifierForDOI(resource);
    saveResource();
  }

  /**
   * @return DOI prefix, e.g. 10.1063 is the prefix for doi:10.1063/BhTX9uO. The DOI may contain the "doi:" access
   * schema or DOI proxy URL "http://dx.doi.org/".
   */
  @VisibleForTesting
  public String parseDoiPrefix(String doi) {
    if (doi != null) {
      doi = StringUtils
        .trim(doi.replaceAll(Constants.DOI_ACCESS_SCHEMA, "").replaceAll(Constants.DOI_PROXY_SERVER_URL, ""));
      int slash = doi.indexOf("/");
      return doi.substring(0, slash);
    }
    return null;
  }

  /**
   * @return part before first forward slash
   */
  public String getDoiSuffix() {
    String doi = Strings.emptyToNull(resource.getDoi());
    if (doi != null) {
      LOG.info("Get DOI suffix from: " + doi);
      int slash = doi.indexOf("/");
      LOG.info("DOI suffix: " + doi.substring(slash + 1));
      return doi.substring(slash  + 1);
    }
    return null;
  }

  /**
   * @return true if the resource has already been assigned a DOI, false otherwise. Remember only DOIs that are public
   * have officially been assigned/registered.
   */
  public boolean isAlreadyAssignedDoi() {
    if (!resource.getVersionHistory().isEmpty()) {
      String doi = resource.getVersionHistory().get(0).getDoi();
      IdentifierStatus status = resource.getVersionHistory().get(0).getStatus();
      if (doi != null && status == IdentifierStatus.PUBLIC) {
        LOG.debug("The last published version of resource [" + resource.getShortname() + "] has doi: [" + doi +
                 "] with status: [" + status + "]");
        return true;
      }
    }
    return false;
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
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS);
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

      // Does the resource already have a source mapped to core type?
      // The core type can be set from the basic metadata page, and determines which core extensions to show
      String coreRowType = Strings.emptyToNull(resource.getCoreRowType());
      potentialCores = Lists.newArrayList();
      potentialExtensions = Lists.newArrayList();

      if (!resource.getSources().isEmpty()) {
        if (coreRowType != null) {
          Extension core = extensionManager.get(coreRowType);
          if (core == null) {
            addActionError(getText("manage.overview.no.DwC.extension", new String[] {coreRowType}));
          } else {
            // core always appears first in list
            potentialCores.add(core);

            // are there other cores that can be used as extensions for this core?
            List<Extension> otherCores = extensionManager.listCore(coreRowType);
            potentialExtensions.addAll(otherCores);

            // are there other extensions suited for this core?
            List<Extension> others = extensionManager.list(coreRowType);
            potentialExtensions.addAll(others);
          }
        } else {
          potentialCores = extensionManager.listCore();
          if (potentialCores.isEmpty()) {
            addActionError(getText("manage.overview.no.DwC.extensions"));
          }
        }
      }

      // check EML
      missingMetadata = !emlValidator.isValid(resource, null);
      // check resource meets all the conditions required in order to be registered
      missingRegistrationMetadata = !hasMinimumRegistryInfo(resource);
      // check the metadata has been modified since the last publication
      metadataModifiedSinceLastPublication = setMetadataModifiedSinceLastPublication(resource);
      // check the source mappings has been modified since the last publication
      mappingsModifiedSinceLastPublication = setMappingsModifiedSinceLastPublication(resource);
      // check if the sources have been modified since the last publication
      sourcesModifiedSinceLastPublication = setSourcesModifiedSinceLastPublication(resource);
      // find the organisation that can register DOIs for datasets
      organisationWithPrimaryDoiAccount = registrationManager.findPrimaryDoiAgencyAccount();

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
   * TODO: if DOI is PUBLIC_PENDING_PUBLICATION, and resource is PUBLIC, try to register DOI if publication is successful. Fail publication if register DOI fails.
   * TODO: if DOI has a different prefix to the last DOI assigned to this resource, throw an Exception
   *
   * @return Struts2 result string
   * @throws Exception if method fails
   */
  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (publish) {
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

      BigDecimal nextVersion = new BigDecimal(resource.getNextVersion().toPlainString());
      BigDecimal replacedVersion = new BigDecimal(resource.getEmlVersion().toPlainString());

      // TODO: implement - below is a temporary implementation.
      if (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC_PENDING_PUBLICATION &&
          (resource.getStatus() == PublicationStatus.PUBLIC || resource.getStatus() == PublicationStatus.REGISTERED)) {
        resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
      }

      // save change summary
      if (getSummary() != null) {
        resource.setChangeSummary(getSummary());
      }

      try {
        // publish a new version of the resource
        if (resourceManager.publish(resource, nextVersion, this)) {
          addActionMessage(getText("publishing.started", new String[] {String.valueOf(nextVersion), resource.getShortname()}));
          return PUBLISHING;
        } else {
          // show action warning there is no source data and mapping, as long as resource isn't metadata-only
          if (resource.getCoreType() != null &&
              !resource.getCoreType().equalsIgnoreCase(Constants.DATASET_TYPE_METADATA_IDENTIFIER)) {
            addActionWarning(getText("manage.overview.data.missing"));
          }
          missingRegistrationMetadata = !hasMinimumRegistryInfo(resource);
          metadataModifiedSinceLastPublication = setMetadataModifiedSinceLastPublication(resource);
          mappingsModifiedSinceLastPublication = setMappingsModifiedSinceLastPublication(resource);
          return SUCCESS;
        }
      } catch (PublicationException e) {
        if (PublicationException.TYPE.LOCKED == e.getType()) {
          addActionError(getText("manage.overview.resource.being.published",
            new String[] {resource.getTitleAndShortname()}));
        } else {
          // alert user publication failed
          addActionError(getText("publishing.failed",
            new String[] {String.valueOf(nextVersion), resource.getShortname(), e.getMessage()}));
          // restore the previous version since publication was unsuccessful
          resourceManager.restoreVersion(resource, nextVersion, replacedVersion, this);
          // keep track of how many failures on auto publication have happened
          resourceManager.getProcessFailures().put(resource.getShortname(), new Date());
        }
      } catch (InvalidConfigException e) {
        // with this type of error, the version cannot be rolled back - just alert user publication failed
        String msg =
          getText("publishing.failed", new String[] {String.valueOf(nextVersion), resource.getShortname(), e.getMessage()});
        LOG.error(msg, e);
        addActionError(msg);
      }
    } else {
      // just return the user to the manage resources page
      return HOME;
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
            org = resource.getOrganisation();

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

  /**
   * To hold the state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param unpublish form variable
   */
  public void setUnpublish(String unpublish) {
    this.unpublish = StringUtils.trimToNull(unpublish) != null;
  }

  /**
   * To hold the identifier state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param reserveDoi form variable
   */
  public void setReserveDoi(String reserveDoi) {
    this.reserveDoi = StringUtils.trimToNull(reserveDoi) != null;
  }

  /**
   * To hold the identifier state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param deleteDoi form variable
   */
  public void setDeleteDoi(String deleteDoi) {
    this.deleteDoi = StringUtils.trimToNull(deleteDoi) != null;
  }

  /**
   * To hold the state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param delete form variable
   */
  public void setDelete(String delete) {
    this.delete = StringUtils.trimToNull(delete) != null;
  }

  /**
   * To hold the state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param undelete form variable
   */
  public void setUndelete(String undelete) {
    this.undelete = StringUtils.trimToNull(undelete) != null;
  }

  /**
   * To hold the publish request, so the same request triggered purely by a URL will not work.
   *
   * @param publish form variable
   */
  public void setPublish(String publish) {
    this.publish = StringUtils.trimToNull(publish) != null;
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

  /**
   * Called from manage resource page.
   *
   * @return true if metadata has been modified since last publication, false otherwise
   */
  public boolean isMetadataModifiedSinceLastPublication() {
    return metadataModifiedSinceLastPublication;
  }

  /**
   * Called from manage resource page.
   *
   * @return true if source mappings has been modified since last publication, false otherwise.
   */
  public boolean isMappingsModifiedSinceLastPublication() {
    return mappingsModifiedSinceLastPublication;
  }

  /**
   * Called from manage resource page.
   *
   * @return true if sources have been modified since last publication, false otherwise.
   */
  public boolean isSourcesModifiedSinceLastPublication() {
    return sourcesModifiedSinceLastPublication;
  }

  /**
   * Preview the first "peekRows" number of rows for a given mapping. The mapping is specified by the combination
   * of rowType and mapping ID.
   */
  public String peek() {
    if (resource == null) {
      return NOT_FOUND;
    }

    peek = Lists.newArrayList();
    columns = Lists.newArrayList();
    Exception exception = null;
    List<TaskMessage> messages = new LinkedList<TaskMessage>();

    if (id != null && mid != null) {
      ExtensionMapping mapping = resource.getMappings(id).get(mid);
      if (mapping != null) {
        try {
          GenerateDwca worker = dwcaFactory.create(resource, this);
          worker.report();
          File tmpDir = Files.createTempDir();
          worker.setDwcaFolder(tmpDir);
          Archive archive = new Archive();
          worker.setArchive(archive);
          // create the data file inside the temp directory
          worker.addDataFile(Lists.newArrayList(mapping), PEEK_ROWS);
          // preview the data file, by writing header and rows
          File[] files = tmpDir.listFiles();
          if (files != null && files.length > 0) {
            // file either represents a core file or an extension
            ArchiveFile core = archive.getCore();
            ArchiveFile ext = archive.getExtension(id, false);
            String delimiter = (core == null) ? ext.getFieldsTerminatedBy() : core.getFieldsTerminatedBy();
            Character quotes = (core == null) ? ext.getFieldsEnclosedBy() : core.getFieldsEnclosedBy();
            int headerRows = (core == null) ? ext.getIgnoreHeaderLines() : core.getIgnoreHeaderLines();

            CSVReader reader = CSVReaderFactory.build(files[0], CHARACTER_ENCODING, delimiter, quotes, headerRows);
            while (reader.hasNext()) {
              peek.add(reader.next());
              if (columns.isEmpty()) {
                columns = Arrays.asList(reader.header);
              }
            }
          } else {
            messages.add(new TaskMessage(Level.ERROR, getText("mapping.preview.not.found")));
          }
        } catch (Exception e) {
          exception = e;
          messages.add(new TaskMessage(Level.ERROR, getText("mapping.preview.error", new String[] {e.getMessage()})));
        }
      } else {
        messages.add(new TaskMessage(Level.ERROR, getText("mapping.preview.mapping.not.found", new String[] {id, String.valueOf(mid)})));
      }
    } else {
      messages.add(new TaskMessage(Level.ERROR, getText("mapping.preview.bad.request")));
    }

    // add messages to those collected while generating preview
    if (!messages.isEmpty()) {
      report.getMessages().addAll(messages);
    }

    report = (exception == null) ? new StatusReport(true, "succeeded", report.getMessages())
      : new StatusReport(exception, "failed", messages);

    return SUCCESS;
  }

  public List<String[]> getPeek() {
    return peek;
  }

  public List<String> getColumns() {
    return columns;
  }

  public Integer getMid() {
    return mid;
  }

  public void setMid(Integer mid) {
    this.mid = mid;
  }

  @Override
  public void report(String resourceShortname, StatusReport report) {
    this.report = report;
  }

  /**
   * @return the organisation associated to this IPT that has a DOI registration agency account, which has been
   * activated enabling it to register DOIs for datasets.
   */
  public Organisation getOrganisationWithPrimaryDoiAccount() {
    return organisationWithPrimaryDoiAccount;
  }

  /**
   * Temporary method used to create a DOI.
   * The DOI is case insensitive.
   *
   * @return DOI in format prefix/suffix
   */
  private String makeDoi() {
    String prefix =
      (organisationWithPrimaryDoiAccount == null || organisationWithPrimaryDoiAccount.getDoiPrefix() == null)
        ? Constants.TEST_DOI_PREFIX : organisationWithPrimaryDoiAccount.getDoiPrefix();
    return prefix + "/" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
  }

  /**
   *
   * @param summary change summary for new published version, entered by the user in the confirm dialog
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * @return the change summary for new published version, entered by the user in the confirm dialog
   */
  public String getSummary() {
    return Strings.emptyToNull(summary);
  }
}
