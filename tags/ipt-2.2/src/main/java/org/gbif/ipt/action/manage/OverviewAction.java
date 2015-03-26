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

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.DoiData;
import org.gbif.api.model.common.DoiStatus;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiExistsException;
import org.gbif.doi.service.InvalidMetadataException;
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
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.UndeletNotAllowedException;
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
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Inject;
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
  private Organisation doiAccount;
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
    this.doiAccount = registrationManager.findPrimaryDoiAgencyAccount();
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
   * Deletes a resource different ways depending on whether it was ever assigned a DOI or not.
   * </br>
   * If this resource was assigned a DOI, it makes the DOI unavailable meaning it still resolves, but to a page
   * explaining the resource has been removed. Furthermore, its archived published versions must be preserved in case
   * the resource is made available again in the future.
   * </br>
   * If this resource was not assigned a DOI, it's safe to just delete the resource and all its archived published
   * versions.
   * </br>
   * Regardless of whether the resource was assigned a DOI, it deletes the resource from GBIF if this resource was
   * registered with GBIF.
   * </br>
   */
  @Override
  public String delete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (delete) {
      if (resource.getStatus().equals(PublicationStatus.DELETED)) {
        addActionWarning(getText("manage.overview.resource.invalid.operation", new String[] {resource.getShortname(),
          resource.getStatus().toString()}));
        return INPUT;
      }
      try {
        DOI doi = resource.getDoi();
        if (doi != null) {
          // prevent deletion if it will trigger a DOI operation, but no DOI agency account has been activated yet
          if (registrationManager.getDoiService() == null) {
            String msg = getText("manage.overview.doi.operation.failed.noAccount");
            LOG.error(msg);
            addActionError(msg);
            return INPUT;
          }

          // de-register resource, but don't delete resource directory
          if (resource.isRegistered()) {
            resourceManager.delete(resource, false);
          }

          // next try to deactivate as many DOIs assigned to the resource as possible (and delete DOI if reserved)
          doDeactivateDOI(doi);
          resource.setIdentifierStatus(
            (resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION))
              ? IdentifierStatus.UNRESERVED : IdentifierStatus.UNAVAILABLE);

          // delete previously assigned DOIs also
          Set<String> deleted = Sets.newHashSet(doi.toString());
          if (!resource.getVersionHistory().isEmpty()) {
            for (VersionHistory history: resource.getVersionHistory()) {
              DOI formerDoi = history.getDoi();
              if (formerDoi != null && !deleted.contains(formerDoi.toString())) {
                doDeactivateDOI(formerDoi);
                deleted.add(formerDoi.toString());
              }
            }
          }

          resource.setStatus(PublicationStatus.DELETED);
          resource.updateAlternateIdentifierForDOI();
          resource.updateCitationIdentifierForDOI(); // unset DOI as citation identifier
          saveResource();
          addActionMessage(getText("manage.overview.resource.deleted", new String[] {resource.toString()}));
        } else {
          // de-register resource, and delete resource directory
          resourceManager.delete(resource, true);
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
   * Resolve DOI, then depending on its state delete it (if reserved) or deactivate it (if registered).
   *
   * @param doi DOI to delete/deactivate
   *
   * @throws org.gbif.ipt.service.DeletionNotAllowedException if deletion failed
   */
  private void doDeactivateDOI(DOI doi) throws DeletionNotAllowedException {
    Preconditions.checkNotNull(registrationManager.getDoiService());
    Preconditions.checkNotNull(doi);
    try {
      DoiData doiData = registrationManager.getDoiService().resolve(doi);
      if (doiData != null && doiData.getStatus() != null) {
        if (doiData.getStatus().equals(DoiStatus.RESERVED)) {
          LOG.info("Deleting reserved DOI: " + doi.toString() + "...");
          registrationManager.getDoiService().delete(doi);
          String msg = getText("manage.overview.publishing.doi.delete.success", new String[] {doi.toString()});
          LOG.info(msg);
          addActionMessage(msg);
        } else if (doiData.getStatus().equals(DoiStatus.REGISTERED)) {
          LOG.info("Deactivating registered DOI: " + doi.toString() + "...");
          registrationManager.getDoiService().delete(doi);
          String msg = getText("manage.overview.publishing.doi.deactivate.success", new String[]{doi.toString()});
          LOG.info(msg);
          addActionMessage(msg);
        } else {
          LOG.error(
            "Not appropriate to delete DOI: " + doi.toString() + ". DOI status=" + doiData.getStatus().toString());
        }
      } else {
        throw new DeletionNotAllowedException(DeletionNotAllowedException.Reason.DOI_REGISTRATION_AGENCY_ERROR, getText("manage.overview.publishing.doi.delete.failed.notResolved", new String[] {doi.toString()}));
      }
    } catch (DoiException e) {
      throw new DeletionNotAllowedException(DeletionNotAllowedException.Reason.DOI_REGISTRATION_AGENCY_ERROR, getText("manage.overview.publishing.doi.delete.failed.exception", new String[]{doi.toString(), e.getMessage()}));
    }
  }

  /**
   * Undeletes a resource (only applicable to resources that were previously assigned a DOI).
   * </br>
   * Undeleting a resource makes the DOI available, resolving to the resource homepage of the last published version.
   * It also undeletes all previous DOIs assigned to the resource, resolving them to the appropriate versioned resource
   * homepages.
   * </br>
   * Undeleting a resource should also undelete the resource from GBIF if it was previously registered with GBIF,
   * however, at this time the GBIF registry api or GBIF registry legacy apis do not support the undelete operation and
   * thus require communication with the GBIF Helpdesk.   *
   */
  public String undelete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (undelete) {
      if (!resource.getStatus().equals(PublicationStatus.DELETED)) {
        addActionWarning(getText("manage.overview.resource.invalid.operation",
          new String[] {resource.getShortname(), resource.getStatus().toString()}));
        return INPUT;
      }
      // note: the DOI of last published version is undeleted
      DOI doi = resource.getAssignedDoi();
      if (doi != null) {
        try {
          // prevent deletion if it will trigger a DOI operation, but no DOI agency account has been activated yet
          if (registrationManager.getDoiService() == null) {
            String msg = getText("manage.overview.doi.operation.failed.noAccount");
            LOG.error(msg);
            addActionError(msg);
            return INPUT;
          }

          Organisation organisation = resource.getOrganisation();
          if (organisation == null) {
            throw new InvalidConfigException(InvalidConfigException.TYPE.RESOURCE_CONFIG,
              "Resource being undeleted missing publishing organisation!");
          } else {
            Organisation retrieved = registrationManager.get(organisation.getKey());
            if (retrieved == null) {
              throw new UndeletNotAllowedException(UndeletNotAllowedException.Reason.ORGANISATION_NOT_ASSOCIATED_TO_IPT, getText("manage.overview.publishing.doi.undelete.failed.noOrganisation", new String[] {organisation.getKey().toString()}));
            } else {
              Organisation doiAccountActivated = registrationManager.findPrimaryDoiAgencyAccount();
              if (doiAccountActivated != null && doiAccountActivated.getDoiPrefix() != null
                  && !doi.getDoiName().toLowerCase().startsWith(doiAccountActivated.getDoiPrefix().toLowerCase())) {
                throw new UndeletNotAllowedException(UndeletNotAllowedException.Reason.DOI_PREFIX_NOT_MATCHING, getText("manage.overview.publishing.doi.undelete.failed.badPrefix", new String[] {doi.toString(), doiAccountActivated.getDoiPrefix()}));
              }
            }
          }

          // reconstruct version being undeleted
          String shortname = resource.getShortname();
          BigDecimal versionToUndelete = resource.getLastPublishedVersionsVersion();
          UUID key = resource.getKey();
          File versionToUndeleteEmlFile = cfg.getDataDir().resourceEmlFile(shortname, versionToUndelete);
          Resource reconstructed = ResourceUtils.reconstructVersion(versionToUndelete, shortname, doi, organisation,
            resource.findVersionHistory(versionToUndelete), versionToUndeleteEmlFile, key);
          URI target = cfg.getResourceUri(shortname);
          // perform undelete
          doUndeleteDOI(doi, reconstructed, target);
          // reassign DOI of last published version
          resource.setDoi(doi);
          resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
          resource.updateCitationIdentifierForDOI();

          // undelete previously assigned DOIs also, which were all deleted/deactivated
          Set<String> undeleted = Sets.newHashSet(doi.toString());
          if (!resource.getVersionHistory().isEmpty()) {
            for (VersionHistory history : resource.getVersionHistory()) {
              DOI formerDoi = history.getDoi();
              if (formerDoi != null && !undeleted.contains(formerDoi.toString())) {
                // reconstruct version being undeleted
                BigDecimal formerVersionToUndelete = new BigDecimal(history.getVersion());
                File formerVersionToUndeleteEmlFile =
                  cfg.getDataDir().resourceEmlFile(shortname, formerVersionToUndelete);
                Resource formerVersionReconstructed = ResourceUtils
                  .reconstructVersion(formerVersionToUndelete, shortname, formerDoi, organisation,
                    resource.findVersionHistory(formerVersionToUndelete), formerVersionToUndeleteEmlFile, key);
                // prepare target URI equal to version resource page
                URI formerTarget = cfg.getResourceVersionUri(shortname, formerVersionToUndelete);
                // perform undelete
                doUndeleteDOI(formerDoi, formerVersionReconstructed, formerTarget);
                undeleted.add(formerDoi.toString());
              }
            }
          }

          // revert resource status back to PUBLIC/REGISTERED
          if (reconstructed.isRegistered()) {
            resource.setStatus(PublicationStatus.REGISTERED);
            // TODO: undelete it from GBIF if it was registered (requires GBIF API change)
            addActionWarning(getText("manage.overview.resource.undelete.warning.gbif"));
          } else {
            resource.setStatus(PublicationStatus.PUBLIC);
          }

          saveResource();
          addActionMessage(
            getText("manage.overview.resource.undeleted", new String[] {resource.getTitleAndShortname()}));
          return SUCCESS;
        } catch (UndeletNotAllowedException e) {
          String msg = getText("manage.resource.undelete.failed");
          LOG.error(msg, e);
          addActionError(msg);
          addActionExceptionWarning(e);
        } catch (IllegalArgumentException e) {
          String msg = getText("manage.resource.undelete.failed");
          LOG.error(msg, e);
          addActionError(msg);
          addActionExceptionWarning(e);
        }

      } else {
        addActionWarning(getText("manage.overview.resource.invalid.operation",
          new String[] {resource.getShortname(), resource.getStatus().toString()}));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation",
        new String[] {resource.getShortname(), resource.getStatus().toString()}));
    }
    return INPUT;
  }

  /**
   * Resolve DOI, then depending on its state reactivate/undelete it if deleted.
   *
   * @param doi DOI to undelete
   * @param resource resource version to undelete
   * @param target target URI of DOI to undelete
   *
   * @throws org.gbif.ipt.service.UndeletNotAllowedException if undelete failed
   */
  private void doUndeleteDOI(DOI doi, Resource resource, URI target) throws UndeletNotAllowedException {
    Preconditions.checkNotNull(registrationManager.getDoiService());
    Preconditions.checkNotNull(doi);
    Preconditions.checkNotNull(resource);
    Preconditions.checkNotNull(target);
    try {
      DoiData doiData = registrationManager.getDoiService().resolve(doi);
      if (doiData != null && doiData.getStatus() != null) {
        if (doiData.getStatus().equals(DoiStatus.DELETED)) {
          LOG.info("Undeleting deleted DOI: " + doi.toString() + "...");
          DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
          registrationManager.getDoiService().register(doi, target, dataCiteMetadata);
          String msg = getText("manage.overview.publishing.doi.undelete.success", new String[]{doi.toString()});
          LOG.info(msg);
          addActionMessage(msg);
        } else {
          throw new UndeletNotAllowedException(UndeletNotAllowedException.Reason.DOI_NOT_DELETED, getText("manage.overview.publishing.doi.undelete.failed.badStatus", new String[] {doi.toString(), doiData.getStatus().toString()}));
        }
      } else {
        throw new UndeletNotAllowedException(UndeletNotAllowedException.Reason.DOI_DOES_NOT_EXIST, getText("manage.overview.publishing.doi.undelete.failed.notResolved", new String[] {doi.toString()}));
      }
    } catch (DoiException e) {
      throw new UndeletNotAllowedException(UndeletNotAllowedException.Reason.DOI_REGISTRATION_AGENCY_ERROR, getText("manage.overview.publishing.doi.undelete.failed.exception", new String[] {doi.toString(), e.getMessage()}));
    }
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
      if (PublicationStatus.PUBLIC == resource.getStatus() && !resource.isAlreadyAssignedDoi()) {
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
      // prevent reservation if no DOI registration agency account configured
      if (registrationManager.getDoiService() == null) {
        String msg = getText("manage.overview.doi.operation.failed.noAccount");
        LOG.error(msg);
        addActionError(msg);
        return INPUT;
      }
      DOI existingDoi = findExistingDoi(resource);
      if ((existingDoi == null && resource.getIdentifierStatus() == IdentifierStatus.UNRESERVED && !resource
        .isAlreadyAssignedDoi()) || (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC && resource
        .isAlreadyAssignedDoi())) {
        DOI doi = DOIUtils.mintDOI(doiAccount.getDoiRegistrationAgency(), doiAccount.getDoiPrefix());
        LOG.info("Reserving " + doi.toString() + " for " + resource.getTitleAndShortname());
        try {
          doReserveDOI(doi, resource);
          String msg = getText("manage.overview.publishing.doi.reserve.success", new String[] {doi.toString()});
          LOG.info(msg);
          addActionMessage(msg);
        } catch (DoiExistsException e) {
          LOG.error("Failed to reserve " + doi.toString() + " because it exists already. Trying again...", e);
          reserveDoi();
        } catch (InvalidMetadataException e) {
          String errorMsg = getText("manage.overview.publishing.doi.reserve.failed.metadata", new String[] {doi.toString(), e.getMessage()});
          LOG.error(errorMsg, e);
          addActionError(errorMsg);
        } catch (DoiException e) {
          String errorMsg = getText("manage.overview.publishing.doi.reserve.failed", new String[]{doi.toString(), e.getMessage()});
          LOG.error(errorMsg, e);
          addActionError(errorMsg);
        }
      } else if (existingDoi != null && resource.getIdentifierStatus() == IdentifierStatus.UNRESERVED && !resource
        .isAlreadyAssignedDoi()) {

        String prefixAllowed = doiAccount.getDoiPrefix();
        // ensure the prefix of the DOI account configured for this IPT matches the prefix of the existing DOI
        if (prefixAllowed != null && existingDoi.getDoiName().startsWith(prefixAllowed.toLowerCase())) {
          try {
            DoiData doiData = registrationManager.getDoiService().resolve(existingDoi);
            // verify the existing DOI is either reserved or registered already
            if (doiData != null && doiData.getStatus().equals(DoiStatus.RESERVED)) {
              LOG.info("Assigning " + existingDoi.toString() + " (existing reserved DOI) to " + resource.getTitleAndShortname());
              doReuseDOI(existingDoi, resource);
            } else if (doiData != null && doiData.getStatus().equals(DoiStatus.REGISTERED)) {
              LOG.info("Assigning " + existingDoi.toString() + " (existing registered DOI) to " + resource.getTitleAndShortname());

              // the DOI is registered and should resolve to this resource's public homepage, so verify the homepage is publicly accessible
              LOG.debug("Resource " + resource.getShortname() + " has status=" + resource.getStatus());
              if (!resource.isPubliclyAvailable()) {
                // TODO: i18n
                String errorMsg = "Failed to reuse existing registered DOI (" + existingDoi.toString() + "). To be able to assign it to this resource, the resource must be publicly accessible.";
                LOG.error(errorMsg);
                addActionError(errorMsg);
              } else {
                // the DOI is registered and its target URI should be equal to the public resource homepage URI
                URI target = doiData.getTarget();
                LOG.debug(existingDoi.toString() + " has target URI=" + target);
                URI homepage = cfg.getResourceUri(resource.getShortname());
                if (target != null && target.equals(homepage)) {
                  LOG.debug("Verified target URI of existing registered DOI is equal to public resource homepage URI");
                  doReuseDOI(existingDoi, resource);
                } else {
                  // TODO: i18n
                  String errorMsg = "Failed to reuse existing registered DOI (" + existingDoi.toString() + "). To be able to assign it to this resource, its target URI must be changed to " + homepage.toString();
                  LOG.error(errorMsg);
                  addActionError(errorMsg);
                }
              }
            } else {
              String errorMsg = getText("manage.overview.publishing.doi.reserve.reused.failed", new String[] {existingDoi.toString()});
              LOG.error(errorMsg);
              addActionError(errorMsg);
            }
          } catch (DoiException e) {
            String errorMsg = getText("manage.overview.publishing.doi.reserve.reused.failed.exception", new String[] {existingDoi.toString(), e.getMessage()});
            LOG.error(errorMsg, e);
            addActionError(errorMsg);
          }
        } else {
          addActionError(getText("manage.overview.publishing.doi.reserve.notRreused", new String[] {existingDoi.toString(), prefixAllowed}));
        }
      } else {
        addActionWarning(getText("manage.overview.resource.doi.invalid.operation",
          new String[] {resource.getShortname(), resource.getIdentifierStatus().toString()}));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.doi.invalid.operation",
        new String[] {resource.getShortname(), resource.getIdentifierStatus().toString()}));
    }
    return execute();
  }

  /**
   * Do the changes to the resource, necessary to reuse an existing DOI.
   *
   * @param doi existing DOI to reuse
   * @param resource resource to apply changes to
   */
  private void doReuseDOI(DOI doi, Resource resource) {
    resource.setDoi(doi);
    resource.setDoiOrganisationKey(registrationManager.findPrimaryDoiAgencyAccount().getKey());
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.updateAlternateIdentifierForDOI();
    resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
    saveResource();
    String msg = getText("manage.overview.publishing.doi.reserve.reused", new String[] {doi.toString()});
    LOG.info(msg);
    addActionMessage(msg);
  }

  /**
   * Reserve a DOI for the resource.
   *
   * @param doi      DOI to reserve
   * @param resource resource to reserve DOI for
   *
   * @throws DoiExistsException if the DOI being reserved already exists so that reserving can be retried with new DOI
   */
  private void doReserveDOI(DOI doi, Resource resource) throws DoiException {
    Preconditions.checkNotNull(registrationManager.getDoiService());
    // reserve a new DOI for this resource using the primary DOI account, and update EML alternateIdentifier list
    DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
    registrationManager.getDoiService().reserve(doi, dataCiteMetadata);
    resource.setDoi(doi);
    resource.setDoiOrganisationKey(registrationManager.findPrimaryDoiAgencyAccount().getKey());
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.updateAlternateIdentifierForDOI();
    resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
    saveResource();
  }

  /**
   * Delete a DOI for the resource. Optionally reassign DOI.
   *
   * @param reservedDoi DOI to delete
   * @param resource resource to delete DOI for (optional)
   * @param reassignedDoi DOI to reassign
   *
   * @throws DoiException if the deletion failed
   */
  private void doDeleteReservedDOI(DOI reservedDoi, Resource resource, @Nullable DOI reassignedDoi) throws DoiException {
    Preconditions.checkNotNull(registrationManager.getDoiService());

    // safeguard - prevent deleting existing registered DOIs
    DoiData doiData = registrationManager.getDoiService().resolve(reservedDoi);
    if (doiData != null && doiData.getStatus() != null && !doiData.getStatus().equals(DoiStatus.REGISTERED)) {
      LOG.debug("Deleting reserved DOI=" + reservedDoi.toString());
      // delete reserved DOI for this resource
      registrationManager.getDoiService().delete(reservedDoi);
    } else {
      LOG.debug("Deleting reserved DOI bypassed because this is an existing registered DOI: " + reservedDoi.toString());
    }

    // reset resource DOI
    resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);
    resource.updateAlternateIdentifierForDOI(); // remove DOI from list of alternate ids
    resource.updateCitationIdentifierForDOI(); // unset DOI as citation identifier
    resource.setDoi(null);
    resource.setDoiOrganisationKey(null);

    // reassign resource DOI if necessary
    if ((reassignedDoi != null)) {
      resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
      resource.setDoi(reassignedDoi);
      resource.updateAlternateIdentifierForDOI(); // add DOI to list of alternate ids
      resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
      resource.setDoiOrganisationKey(registrationManager.findPrimaryDoiAgencyAccount().getKey());
    }

    saveResource();
  }



  /**
   * Return the existing DOI assigned to this resource. An existing DOI is set as the citation identifier.
   *
   * @return the existing DOI assigned to this resource, or null if none was found.
   */
  @VisibleForTesting
  public DOI findExistingDoi(Resource resource) {
    if (resource != null && resource.getEml() != null) {
      Citation citation = resource.getEml().getCitation();
      if (citation != null) {
        // be sure to trim identifier, user may have added extra space which causes resolve to fail!
        if (DOI.isParsable(StringUtils.trimToNull(citation.getIdentifier()))) {
          return new DOI(citation.getIdentifier());
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
    if (registrationManager.getDoiService() == null) {
      String msg = getText("manage.overview.doi.operation.failed.noAccount");
      LOG.error(msg);
      addActionError(msg);
    }
    if (deleteDoi) {
        DOI reservedDoi = resource.getDoi();
        if (reservedDoi != null && resource.getIdentifierStatus() == IdentifierStatus.PUBLIC_PENDING_PUBLICATION) {
          DOI assignedDoi = resource.getAssignedDoi();
          if (assignedDoi != null) {
            LOG.info("Deleting reserved " + reservedDoi.toString() + " and reassigning " + assignedDoi.toString());
            try {
              // delete reserved DOI, reassign previous DOI to resource, and update EML alternateIdentifier list
              doDeleteReservedDOI(reservedDoi, resource, assignedDoi);
              String msg = getText("manage.overview.publishing.doi.delete.reassign.success", new String[] {reservedDoi.toString(), assignedDoi.toString()});
              LOG.info(msg);
              addActionMessage(msg);
            } catch (DoiException e) {
              String errorMsg = getText("manage.overview.publishing.doi.delete.failed.exception", new String[] {resource.getDoi().toString(), e.getMessage()});
              LOG.error(errorMsg, e);
              addActionError(errorMsg);
            }
          } else {
            LOG.info("Deleting reserved " + reservedDoi.toString());
            try {
              // delete reserved DOI, and update EML alternateIdentifier list
              doDeleteReservedDOI(reservedDoi, resource, null);
              String msg = getText("manage.overview.publishing.doi.delete.success", new String[] {reservedDoi.toString()});
              LOG.info(msg);
              addActionMessage(msg);
            } catch (DoiException e) {
              String errorMsg = getText("manage.overview.publishing.doi.delete.failed.exception", new String[]{resource.getDoi().toString(), e.getMessage()});
              LOG.error(errorMsg, e);
              addActionError(errorMsg);
            }
          }
        } else {
          addActionWarning(getText("manage.overview.resource.doi.invalid.operation",
            new String[] {resource.getShortname(), resource.getIdentifierStatus().toString()}));
        }
    } else {
      addActionWarning(getText("manage.overview.resource.doi.invalid.operation",
        new String[] {resource.getShortname(), resource.getIdentifierStatus().toString()}));
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

      // populate frequencies map
      populateFrequencies();
    }
  }

  /**
   * Executes the instruction to publish the resource, handles update GBIF registrations, and handles all DOI
   * registrations and DOI update registrations.
   * </br>
   * If the resource is public, and its DOI is reserved (not public), its DOI is registered if publication is successful
   * otherwise the previous version has to be restored. This is done for all new major versions (e.g. the first time a
   * DOI is assigned to the resource, and to transition the resource to a new DOI in case of major scientific changes).
   * It is impossible to transition a DOI from one prefix to another, so a check should ensure this doesn't happen.
   * </br>
   * If the resource is public, and its DOI is registered (public), its DOI registration is updated if publication is
   * successful otherwise the previous version has to be restored. This is done for all new minor versions.
   * </br>
   * Publication should fail, and the previous version restored if the DOI operation fails.
   * </br>
   * The method must check if resource has been configured to be auto-published.
   * </br>
   * In addition, the method must clear the processFailures for the resource being published. If a resource has
   * exceeded the maximum number of failed publish events during auto-publication, auto-publication for the resource is
   * suspended. By publishing the resource manually, it is assumed the manager is trying to debug the problem. Without
   * this safeguard in place, a resource can auto-publish in an endless number of failures.
   *
   * @return Struts2 result string
   *
   * @throws Exception if method fails
   */
  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (publish) {
      // prevent publishing if resource is registered but it hasn't been assigned a GBIF-supported license
      if (resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()) {
        String msg = getText("manage.overview.prevented.resource.publishing.noGBIFLicense");
        addActionError(msg);
        LOG.error(msg);
        return INPUT;
      }
      // prevent publishing if publishing will trigger a DOI operation, but no DOI agency account has been activated yet
      if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
        if (registrationManager.getDoiService() == null) {
          String msg = getText("manage.overview.doi.operation.failed.noAccount");
          LOG.error(msg);
          addActionError(msg);
          return INPUT;
        }

        // prevent publishing if DOI does not resolve/exist, or if DOI agency account cannot resolve this DOI
        try {
          DoiData doiData = registrationManager.getDoiService().resolve(resource.getDoi());
          if (doiData != null && doiData.getStatus() != null) {
            if (doiData.getStatus().compareTo(DoiStatus.RESERVED) == 0 || doiData.getStatus().compareTo(DoiStatus.REGISTERED) == 0) {
              LOG.info("Pre-publication check: successfully resolved " + resource.getDoi().toString());
            } else {
              String errorMsg = getText("manage.overview.publishing.doi.publish.check.registered.failed", new String[] {resource.getDoi().toString(), doiData.getStatus().toString()});
              LOG.error(errorMsg);
              addActionError(errorMsg);
              return INPUT;
            }
          } else {
            String errorMsg = getText("manage.overview.publishing.doi.publish.check.existing.failed", new String[] {resource.getDoi().toString()});
            LOG.error(errorMsg);
            addActionError(errorMsg);
            return INPUT;
          }
        } catch (DoiException e) {
          String errorMsg = getText("manage.overview.publishing.doi.publish.check.existing.failed.exception", new String[] {resource.getDoi().toString(), e.getMessage()});
          LOG.error(errorMsg, e);
          addActionError(errorMsg);
          return INPUT;
        }
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

      BigDecimal nextVersion = new BigDecimal(resource.getNextVersion().toPlainString());
      BigDecimal replacedVersion = new BigDecimal(resource.getEmlVersion().toPlainString());

      // set resource's change summary as entered in confirm popup (defaults to empty string)
      resource.setChangeSummary(getSummary());

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
    // prevent registration if last published version was not public (at the time of publishing)
    if (!resource.isLastPublishedVersionPublic()) {
      String msg = getText("manage.overview.failed.resource.registration.notPublic");
      addActionError(msg);
      LOG.error(msg);
      return INPUT;
    }
    // prevent registration if last published version was not assigned a GBIF-supported license
    if (!isLastPublishedVersionAssignedGBIFSupportedLicense(resource)) {
      String msg = getText("manage.overview.prevented.resource.registration.noGBIFLicense");
      addActionError(msg);
      LOG.error(msg);
      return INPUT;
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
   * Used before registering current (last) published version.
   *
   * @return true if the last published version has been assigned a GBIF-supported license, false otherwise
   */
  public boolean isLastPublishedVersionAssignedGBIFSupportedLicense(Resource resource) {
    List<VersionHistory> history = resource.getVersionHistory();
    if (!history.isEmpty()) {
      VersionHistory latestVersionHistory = history.get(0);
      BigDecimal latestVersion = new BigDecimal(latestVersionHistory.getVersion());
      File emlFile = cfg.getDataDir().resourceEmlFile(resource.getShortname(), latestVersion);
      if (emlFile.exists()) {
        try {
          LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
          InputStream in = new FileInputStream(emlFile);
          Eml eml = EmlFactory.build(in);
          if (eml.parseLicenseUrl() != null) {
            LOG.debug("Checking if license (URL=" + eml.parseLicenseUrl() + ") is supported by GBIF..");
            return Constants.GBIF_SUPPORTED_LICENSES.contains(eml.parseLicenseUrl());
          }
        } catch (Exception e) {
          LOG.error(
            "Failed to check if last published version of resource has been assigned a GBIF-supported license: " + e
              .getMessage(), e);
        }
      }
    }
    return false;
  } //getLastPublishedVersionAssignedLicense()

  /**
   * @return license URL assigned to the last published version or null if none was assigned
   */
  public String getLastPublishedVersionAssignedLicense(Resource resource) {
    List<VersionHistory> history = resource.getVersionHistory();
    if (!history.isEmpty()) {
      VersionHistory latestVersionHistory = history.get(0);
      BigDecimal latestVersion = new BigDecimal(latestVersionHistory.getVersion());
      File emlFile = cfg.getDataDir().resourceEmlFile(resource.getShortname(), latestVersion);
      if (emlFile.exists()) {
        try {
          LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
          InputStream in = new FileInputStream(emlFile);
          Eml eml = EmlFactory.build(in);
          return eml.parseLicenseUrl();
        } catch (Exception e) {
          LOG.error(
            "Failed to check if last published version of resource has been assigned a GBIF-supported license: " + e
              .getMessage(), e);
        }
      }
    }
    return null;
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
    return doiAccount;
  }

  /**
   *
   * @param summary change summary for new published version, entered by the user in the confirm dialog defaulting to
   *                empty string
   */
  public void setSummary(String summary) {
    this.summary = StringUtils.trimToEmpty(summary);
  }

  /**
   * @return the change summary for new published version, entered by the user in the confirm dialog
   */
  public String getSummary() {
    return Strings.emptyToNull(summary);
  }
}
