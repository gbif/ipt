/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.service.manage.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiExistsException;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PublicationOptions;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;
import org.gbif.ipt.model.datapackage.metadata.camtrap.RelatedIdentifier;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Temporal;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackage;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackage;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.GeneratorException;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.task.TaskMessage;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.ipt.utils.PublicationFailureEmailUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.metadata.eml.EMLProfileVersion;
import org.gbif.metadata.eml.EmlValidator;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.model.Citation;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TemporalCoverage;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipFile;

import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;

import lombok.Getter;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.CAMTRAP_DP_OBSERVATIONS;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.model.Resource.CoreRowType.METADATA;

public class ResourcePublicationManagerImpl extends BaseManager implements ResourcePublicationManager, ReportHandler {

  private static final int MAX_PROCESS_FAILURES = 3;
  public static final SimpleDateFormat CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


  private final ResourceManager resourceManager;
  private final RegistryManager registryManager;
  private final RegistrationManager registrationManager;
  private final Eml2Rtf eml2Rtf;
  @Getter
  private final GenerateDwcaFactory dwcaFactory;
  @Getter
  private final GenerateDataPackageFactory dataPackageFactory;
  @Getter
  private final GenerateDarwinCoreDataPackageFactory dwcDpFactory;
  private final SimpleTextProvider textProvider;
  private final ResourceMetadataInferringService resourceMetadataInferringService;

  private final ThreadPoolExecutor executor;
  private final Map<String, Future<Map<String, Integer>>> processFutures = new HashMap<>();
  private final ListValuedMap<String, Date> processFailures = new ArrayListValuedHashMap<>();
  private final Map<String, LocalDate> lastLoggedFailures = new ConcurrentHashMap<>();
  private final Map<String, StatusReport> processReports = new ConcurrentHashMap<>();
  private final List<String> resourcesToSkip = new CopyOnWriteArrayList<>();
  private final Set<String> resourcesToNotifyPublicationFailure = ConcurrentHashMap.newKeySet();

  public ResourcePublicationManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager,
                                        RegistryManager registryManager, RegistrationManager registrationManager,
                                        Eml2Rtf eml2Rtf, GenerateDwcaFactory dwcaFactory,
                                        GenerateDataPackageFactory dataPackageFactory,
                                        GenerateDarwinCoreDataPackageFactory dwcDpFactory,
                                        SimpleTextProvider textProvider,
                                        ResourceMetadataInferringService resourceMetadataInferringService) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
    this.registrationManager = registrationManager;
    this.eml2Rtf = eml2Rtf;
    this.dwcaFactory = dwcaFactory;
    this.dataPackageFactory = dataPackageFactory;
    this.dwcDpFactory = dwcDpFactory;
    this.textProvider = textProvider;
    this.resourceMetadataInferringService = resourceMetadataInferringService;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cfg.getMaxThreads());
  }

  @Override
  public boolean cancelPublishing(String shortname) {
    boolean canceled = false;
    // get future
    Future<Map<String, Integer>> f = processFutures.get(shortname);
    if (f != null) {
      // cancel job, even if it's running
      canceled = f.cancel(true);
      if (canceled) {
        // remove process from locking list
        processFutures.remove(shortname);
      } else {
        LOG.warn("Canceling publication of resource {} failed", shortname);
      }
    }

    // Remove from the skip list
    resourcesToSkip.remove(shortname);

    return canceled;
  }

  @Override
  public boolean isLocked(String shortname) {
    return isLocked(shortname, new BaseAction(textProvider, cfg, registrationManager));
  }

  @Override
  public boolean isLocked(String shortname, BaseAction action) {
    if (processFutures.containsKey(shortname)) {
      Resource resource = resourceManager.get(shortname);
      BigDecimal version = resource.getMetadataVersion();

      // is listed as locked, but the task might be finished, check
      Future<Map<String, Integer>> f = processFutures.get(shortname);
      // if this task finished
      if (f.isDone()) {
        // remove the process from the locking list immediately! Fixes Issue 1141
        processFutures.remove(shortname);
        boolean succeeded = false;
        boolean checksumMatches = false;
        boolean metadataChanged = isMetadataModifiedSinceLastPublication(resource);
        boolean dataOrMetadataChanged = true;
        boolean skipIfNotChanged = resourcesToSkip.contains(shortname) || resource.isSkipPublicationIfNotChanged();
        boolean noSignificantRecordsDrop = true;
        double dropPercentage;
        String reasonFailed = null;
        Throwable cause = null;
        try {
          if (resource.isSkipPublicationIfRecordsDrop()) {
            // check the number of records after publishing
            int newRecordCount = getResourceRecordsCount(resource, f.get());
            int previousRecordCount = resource.getRecordsPublished();
            int dropThreshold = resource.getRecordsDropThreshold();

            int dropAmount = previousRecordCount - newRecordCount;
            dropPercentage = previousRecordCount == 0 ? 0 : (dropAmount * 100.0) / previousRecordCount;

            if (dropPercentage > dropThreshold) {
              // drop is too big, prevent publication
              noSignificantRecordsDrop = false;
              String message = String.format(
                  "The number of records dropped more than allowed %d%%: %.2f%%.",
                  dropThreshold,
                  dropPercentage
              );
              LOG.error(message);
              getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, message));
            } else {
              LOG.debug("No significant drop in records detected.");
              getTaskMessages(shortname).add(new TaskMessage(Level.ERROR,
                  "No significant drop in records detected."));
            }
          }

          // if no significant drop (or it's switched off) - proceed with the publication
          if (noSignificantRecordsDrop) {
            // store record counts by extension
            resource.setRecordsByExtension(f.get());
            // populate record count
            Integer recordCount = getResourceRecordsCount(resource);
            resource.setRecordsPublished(recordCount);

            if (skipIfNotChanged) {
              getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "? Checking if data has been changed since last published"));
            }

            File resourceArchiveFile = dataDir.resourceArchiveFile(resource, version);
            LOG.debug("Calculating checksum for the resource: {}", shortname);
            try {
              String archiveChecksum = calculateArchiveChecksum(resourceArchiveFile);
              String lastPublishedArchiveChecksum = resource.getLastPublishedArchiveChecksum();
              checksumMatches = archiveChecksum.equals(lastPublishedArchiveChecksum);

              if (lastPublishedArchiveChecksum == null) {
                LOG.debug("No checksum found for the resource {}", shortname);
                resource.setLastPublishedArchiveChecksum(archiveChecksum);

                // do not log additional info about checksum if it is disabled
                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "No checksum found for comparison, skipping."));
                }
              } else if (checksumMatches) {
                LOG.debug("New checksum [{}] matches the stored one [{}] for the resource {}",
                    archiveChecksum, lastPublishedArchiveChecksum, resource.getShortname());

                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Checksum has not changed since last published"));
                }

                if (skipIfNotChanged && !metadataChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Metadata has not changed since last published"));
                  dataOrMetadataChanged = false;
                }
              } else {
                LOG.debug("New checksum [{}] for the resource {}", archiveChecksum, resource.getShortname());
                resource.setLastPublishedArchiveChecksum(archiveChecksum);

                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "✓ Checksum has changed since last published"));
                }
              }
            } catch (Exception e) {
              LOG.error("Failed to calculate checksum for DwC-A: {}", resourceArchiveFile.getName(), e);

              if (skipIfNotChanged) {
                getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Failed to calculate checksum"));
              }
            }

            boolean visibilityChanged = resource.getPendingStatus() != null;
            if (visibilityChanged) {
              getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "Visibility changed to: " + resource.getPendingStatus()));
            }

            // If all sources are file (CSV, TSV, Excel) also check when they were changed
            boolean onlyFileSources = isOnlyFileSources(resource);
            boolean sourcesModifiedSinceLastPublication = isSourcesModifiedSinceLastPublication(resource);

            if (skipIfNotChanged && onlyFileSources && !sourcesModifiedSinceLastPublication) {
              dataOrMetadataChanged = false;

              getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "Source files has not changed since last published"));
            }

            if (dataOrMetadataChanged || visibilityChanged) {
              // finish publication (update registration, persist resource changes)
              publishEnd(resource, action, version);
              // important: indicate publishing finished successfully!
              succeeded = true;
            }
          }
        } catch (ExecutionException e) {
          // getCause holds the actual exception our callable (GenerateDwca) threw
          cause = e.getCause();
          if (cause instanceof GeneratorException) {
            reasonFailed = action.getText("dwca.failed", new String[]{shortname, cause.getMessage()});
          } else if (cause instanceof InterruptedException) {
            reasonFailed = action.getText("dwca.interrupted", new String[]{shortname, cause.getMessage()});
          } else {
            reasonFailed = action.getText("dwca.failed", new String[]{shortname, cause.getMessage()});
          }
        } catch (InterruptedException e) {
          reasonFailed = action.getText("dwca.interrupted", new String[]{shortname, e.getMessage()});
          cause = e;
        } catch (PublicationException e) {
          reasonFailed = action.getText("publishing.error", new String[]{e.getType().toString(), e.getMessage()});
          cause = e;
          // this type of exception happens outside GenerateDwca - so add reason to StatusReport
          getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, reasonFailed));
        } finally {
          // if publication was successful
          if (succeeded) {
            // update StatusReport on publishing page
            String msg =
                action.getText("publishing.success", new String[]{version.toPlainString(), resource.getShortname()});
            StatusReport updated = new StatusReport(true, msg, getTaskMessages(shortname));
            processReports.put(shortname, updated);
            resourcesToNotifyPublicationFailure.remove(resource.getShortname());
          } else {
            boolean failedDueToDataNotChanged = !dataOrMetadataChanged;
            boolean failedDueToRecordsDrop = !noSignificantRecordsDrop;

            if (failedDueToDataNotChanged) {
              reasonFailed = action.getText("publishing.dataNotChanged");
            }

            if (failedDueToRecordsDrop) {
              reasonFailed = action.getText("publishing.dropInRecords");
            }

            // alert user publication failed
            String msg =
                action.getText("publishing.failed", new String[]{version.toPlainString(), shortname, reasonFailed});
            action.addActionError(msg);

            // update StatusReport on publishing page
            if (cause != null) {
              // TODO: add copy constructor?
              StatusReport previous = processReports.get(shortname);
              StatusReport updated = new StatusReport(cause, msg, previous.getStep(), getTaskMessages(shortname));
              processReports.put(shortname, updated);
            }

            if (failedDueToDataNotChanged) {
              String dataNotChanged = action.getText("publishing.dataNotChanged.revert");
              StatusReport updated = new StatusReport(true, dataNotChanged, getTaskMessages(shortname));
              processReports.put(shortname, updated);
              updateNextPublishedDate(new Date(), resource);
            }

            if (failedDueToRecordsDrop) {
              String dropInRecords = action.getText("publishing.dropInRecords.revert");
              StatusReport updated = new StatusReport(true, dropInRecords, getTaskMessages(shortname));
              processReports.put(shortname, updated);
              updateNextPublishedDate(new Date(), resource);
            }

            // the previous version needs to be rolled back
            restoreVersion(resource, version, action);

            // do not count "data not changed" as an actual failure
            if (!failedDueToDataNotChanged) {
              // keep track of how many failures on auto publication have happened
              processFailures.put(resource.getShortname(), new Date());
              sendPublicationFailureEmail(resource, version, reasonFailed);
            }

            resourcesToSkip.remove(resource.getShortname());
            resourcesToNotifyPublicationFailure.remove(resource.getShortname());
          }
        }
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public StatusReport status(String shortname) {
    isLocked(shortname);
    return processReports.get(shortname);
  }

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action)
      throws PublicationException, InvalidConfigException {
    return publish(resource, version, action, false);
  }

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action, boolean skipIfNotChanged)
      throws PublicationException, InvalidConfigException {
    PublicationOptions options = PublicationOptions.builder().skipPublicationIfNotChanged(skipIfNotChanged).build();
    return publish(resource, version, action, options);
  }

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action, PublicationOptions options)
      throws PublicationException, InvalidConfigException {
    String shortname = resource.getShortname();

    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    // add new version history
    addOrUpdateVersionHistory(resource, version, false, action);

    // remove StatusReport from previous publishing round
    StatusReport report = status(shortname);
    if (report != null) {
      processReports.remove(shortname);
    }

    if (!options.isSkipPublicationIfNotChanged()) {
      resourcesToSkip.remove(shortname);
    }
    if (options.isNotifyPublicationFailure()) {
      resourcesToNotifyPublicationFailure.add(shortname);
    } else {
      resourcesToNotifyPublicationFailure.remove(shortname);
    }

    // Abort further publication for Metadata Only - no changes (if skipIfNotChanged activated)
    if (resource.isMetadataOnly() && options.isSkipPublicationIfNotChanged()) {
      Date lastPublished = resource.getLastPublished();
      Date metadataLastModified = resource.getMetadataModified();

      boolean metadataChanged = metadataLastModified == null || metadataLastModified.after(lastPublished);

      if (!metadataChanged) {
        String metadataNotChangedStatus = action.getText("publishing.metadataNotChanged");
        StatusReport updated = new StatusReport(true, metadataNotChangedStatus, getTaskMessages(shortname));
        processReports.put(shortname, updated);
        resourcesToNotifyPublicationFailure.remove(shortname);

        return false;
      }
    }

    try {
      preventPublicationForSourcesInProcessingState(resource);

      publishMetadata(resource, version, action);
      publishRtf(resource, version);

      // (re)generate archive (DwC-A/DP) asynchronously
      boolean archive = false;
      if (resource.hasAnyMappedData()) {
        // for bulk publication keep resources to be skipped
        if (options.isSkipPublicationIfNotChanged()) {
          resourcesToSkip.add(shortname);
        }
        generateArchive(resource);
        archive = true;
      } else {
        // set number of records published
        resource.setRecordsPublished(0);
        // finish publication now
        publishEnd(resource, action, version);
      }

      return archive;
    } catch (RuntimeException e) {
      resourcesToNotifyPublicationFailure.remove(shortname);
      throw e;
    }

  }

  // Generic method for DwC-A and data packages
  private void generateArchive(Resource resource) {
    if (resource.isDwcDp()) {
      generateDwcDP(resource);
    } else if (resource.isDataPackage()) {
      generateDataPackage(resource);
    } else {
      generateDwca(resource);
    }
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDwca(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDwca worker = dwcaFactory.create(resource, this);
    Future<Map<String, Integer>> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDataPackage(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDataPackage worker = dataPackageFactory.create(resource, this);
    Future<Map<String, Integer>> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDwcDP(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDarwinCoreDataPackage worker = dwcDpFactory.create(resource, this);
    Future<Map<String, Integer>> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  @Override
  public void publishDataPackageMetadata(Resource resource, BigDecimal version) {
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
          "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    // update metadata version
    resource.setMetadataVersion(version);
    if (resource.getDataPackageMetadata() instanceof FrictionlessMetadata frictionlessMetadata) {
      frictionlessMetadata.setCreated(new Date());
    }

    // update metadata created (represents date when the resource was last published)
    resource.getDataPackageMetadata().setVersion(version.toPlainString());

    // update metadata with inferred data (if infer automatically is turned on)
    if (CAMTRAP_DP.equals(resource.getCoreType())
        && (resource.isInferGeocoverageAutomatically()
        || resource.isInferTaxonomicCoverageAutomatically()
        || resource.isInferTemporalCoverageAutomatically())) {
      InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) resourceMetadataInferringService.inferMetadata(resource);
      // save inferred metadata
      resource.setInferredMetadata(inferredMetadata);
      resourceManager.saveInferredMetadata(resource);

      if (resource.isInferGeocoverageAutomatically()) {
        updateCamtrapGeographicScopeWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTaxonomicCoverageAutomatically()) {
        updateCamtrapTaxonomicScopeWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTemporalCoverageAutomatically()) {
        updateCamtrapTemporalScopeWithInferredFromSourceData(resource, inferredMetadata);
      }
    }

    // save all changes to metadata
    resourceManager.saveDatapackageMetadata(resource);

    // create versioned metadata file
    File trunkFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    File versionedFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType(), version);
    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
          "Can't publish metadata file for resource " + resource.getShortname(), e);
    }
  }

  /**
   * Updates the resource's alternate identifier for its corresponding Registry UUID and saves the EML.
   * If called on a resource that is already registered, the method ensures that it won't be added a second time.
   * To accommodate updates from older versions of the IPT, the identifier is added by calling this method every
   * time the resource gets re-published.
   *
   * @param resource resource
   * @return resource with Registry UUID for the resource updated
   */
  @Override
  public Resource updateAlternateIdentifierForRegistry(Resource resource) {
    Eml eml = resource.getEml();
    if (eml != null) {
      // retrieve a list of the resource's alternate identifiers
      List<String> currentIds = eml.getAlternateIdentifiers();
      if (currentIds != null) {
        // make new list of alternative identifiers in lower case so comparison is done in lower case only
        List<String> ids = new ArrayList<>();
        for (String id : currentIds) {
          ids.add(id.toLowerCase());
        }
        if (resource.isRegistered()) {
          // GBIF Registry UUID
          UUID key = resource.getKey();
          // has the Registry UUID been added as an alternative identifier yet? If not, add it!
          if (key != null && !ids.contains(key.toString().toLowerCase())) {
            currentIds.add(key.toString());
            // save all changes to Eml
            resourceManager.saveEml(resource);
            if (cfg.debug()) {
              LOG.info("GBIF Registry UUID added to Resource's list of alternate identifiers");
            }
          }
        }
      }
    } else {
      resource.setEml(new Eml());
    }

    return resource;
  }

  @Override
  public Resource updateAlternateIdentifierForIPTURLToResource(Resource resource) {
    // retrieve a list of the resource's alternate identifiers
    List<String> ids = null;
    if (resource.getEml() != null) {
      ids = resource.getEml().getAlternateIdentifiers();
    } else {
      resource.setEml(new Eml());
    }

    if (ids != null) {
      // has this been added before, perhaps with a different baseURL?
      boolean exists = false;
      String existingId = null;
      for (String id : ids) {
        // try to match "resource"
        if (id.contains(Constants.REQ_PATH_RESOURCE)) {
          exists = true;
          existingId = id;
        }
      }
      // if the resource is PUBLIC, or REGISTERED
      if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) != 0) {
        String url = cfg.getResourceUrl(resource.getShortname());
        // if identifier does not exist yet - add it!
        // if it already exists, then replace it just in case the baseURL has changed, for example
        if (exists) {
          ids.remove(existingId);
        }
        // lastly, be sure to add it
        ids.add(url);
        // save all changes to Eml
        resourceManager.saveEml(resource);
        if (cfg.debug()) {
          LOG.info("IPT URL to resource added to (or updated in) Resource's list of alt ids");
        }
      }
      // otherwise if the resource is PRIVATE
      else if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) == 0) {
        // no public resource alternate identifier can exist if the resource visibility is private - remove it if app.
        if (exists) {
          ids.remove(existingId);
          // save all changes to Eml
          resourceManager.saveEml(resource);
          if (cfg.debug()) {
            LOG.info("Following visibility change, IPT URL to resource was removed from Resource's list of alt ids");
          }
        }
      }
    }
    return resource;
  }

  @Override
  public void restoreVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }

    if (resource.isDataPackage()) {
      restoreDataPackageResourceVersion(resource, rollingBack, action);
    } else {
      restoreDarwinCoreResourceVersion(resource, rollingBack, action);
    }
  }

  /**
   * Remove an archived version in the resource history and from the file system
   */
  @SuppressWarnings("BigDecimalEquals")
  @Override
  public void removeVersion(Resource resource, BigDecimal version) {
    // Cannot remove the most recent version, only archived versions
    if ((version != null) && !version.equals(resource.getMetadataVersion())) {
      LOG.debug("Removing version {} for resource: {}", version, resource.getShortname());
      try {
        removeVersionInternal(resource, version);
        resource.removeVersionHistory(version);
        resourceManager.save(resource);
        LOG.debug("Version {} has been removed for resource: {}", version, resource.getShortname());
      } catch (IOException e) {
        LOG.error("Cannot remove version {} for resource: {}", version, resource.getShortname(), e);
      }
    }
  }

  /**
   * Remove an archive version from the file system (because it has been replaced by a new published version for
   * example).
   *
   * @param version of archive to remove
   */
  @Override
  public void removeArchiveVersion(String shortname, BigDecimal version) {
    File dwcaFile = dataDir.resourceDwcaFile(shortname, version);
    if (dwcaFile != null && dwcaFile.exists()) {
      boolean deleted = FileUtils.deleteQuietly(dwcaFile);
      if (deleted) {
        LOG.debug("{} has been successfully deleted.", dwcaFile.getAbsolutePath());
      }
    }

    File dpArchiveFile = dataDir.resourceDataPackageFile(shortname, version);
    if (dpArchiveFile != null && dpArchiveFile.exists()) {
      boolean deleted = FileUtils.deleteQuietly(dpArchiveFile);
      if (deleted) {
        LOG.debug("{} has been successfully deleted.", dpArchiveFile.getAbsolutePath());
      }
    }
  }

  private void removeVersionInternal(Resource resource, BigDecimal version) throws IOException {
    String shortname = resource.getShortname();

    // delete eml-*.xml if it exists (eml.xml must remain)
    File versionedEMLFile = dataDir.resourceEmlFile(shortname, version);
    if (versionedEMLFile.exists()) {
      FileUtils.forceDelete(versionedEMLFile);
    }

    // delete datapackage-*.json if it exists (datapackage.json must remain)
    File versionedDataPackageMetadataFile =
        dataDir.resourceDatapackageMetadataFile(shortname, resource.getCoreType(), version);
    if (versionedDataPackageMetadataFile.exists()) {
      FileUtils.forceDelete(versionedDataPackageMetadataFile);
    }

    // delete shortname-*.rtf if it exists
    File versionedRTFFile = dataDir.resourceRtfFile(shortname, version);
    if (versionedRTFFile.exists()) {
      FileUtils.forceDelete(versionedRTFFile);
    }

    // delete dwca-*.zip if it exists
    File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, version);
    if (versionedDwcaFile.exists()) {
      FileUtils.forceDelete(versionedDwcaFile);
    }

    // delete datapackage-*.zip if it exists
    File versionedDataPackageArchiveFile = dataDir.resourceDataPackageFile(shortname, version);
    if (versionedDataPackageArchiveFile.exists()) {
      FileUtils.forceDelete(versionedDataPackageArchiveFile);
    }
  }

  @Override
  public void register(Resource resource, Organisation organisation, Ipt ipt, BaseAction action)
      throws RegistryException {
    ActionLogger alog = new ActionLogger(this.LOG, action);

    if (PublicationStatus.REGISTERED != resource.getStatus() && PublicationStatus.PUBLIC == resource.getStatus()) {

      // Check: is there a chance this resource is meant to update an existing registered resource?
      // Populate set of UUIDs from eml.alternateIdentifiers that could represent existing registered resource UUIDs
      Set<UUID> candidateResourceUUIDs = collectCandidateResourceUUIDsFromAlternateIds(resource);

      // there can be max 1 candidate UUID. This safeguards against migration errors
      if (candidateResourceUUIDs.size() > 1) {
        String reason =
            action.getText("manage.resource.migrate.failed.multipleUUIDs", new String[]{organisation.getName()});
        String help = action.getText("manage.resource.migrate.failed.help");
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help);
      }
      // resource migration can happen if a single UUID corresponding to the resource UUID of an existing registered
      // resource owned by the specified organization has been found in the resource's alternate ids
      else if (candidateResourceUUIDs.size() == 1) {

        // there cannot be any public res with the same alternate identifier UUID, or registered res with the same UUID
        UUID candidate = candidateResourceUUIDs.iterator().next();
        List<String> duplicateUses = detectDuplicateUsesOfUUID(candidate, resource.getShortname());
        if (duplicateUses.isEmpty()) {
          if (organisation.getKey() != null && organisation.getName() != null) {
            // check in the registry resource with the provided key has this publishing organisation
            boolean matched =
                registryManager.isResourceBelongsToOrganisation(candidate.toString(), organisation.getKey().toString());

            if (matched) {
              LOG.debug("Resource matched to existing registered resource, UUID={}", organisation.getKey());

              // fill in registration info - we've found the original resource being migrated to the IPT
              resource.setStatus(PublicationStatus.REGISTERED);
              resource.setKey(candidate);
              resource.setOrganisation(organisation);

              // display update about migration to user
              alog.info("manage.resource.migrate", new String[]{organisation.getKey().toString(), organisation.getName()});

              // update the resource, adding the new service(s)
              updateRegistration(resource, action);
            }
            // if no match was ever found, this is considered a failed resource migration
            else {
              String reason =
                  action.getText("manage.resource.migrate.failed.badUUID", new String[]{organisation.getName()});
              String help = action.getText("manage.resource.migrate.failed.help");
              throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help);
            }
          }
        } else {
          String reason = action.getText("manage.resource.migrate.failed.duplicate",
              new String[]{candidate.toString(), duplicateUses.toString()});
          String help1 = action.getText("manage.resource.migrate.failed.help");
          String help2 = action.getText("manage.resource.migrate.failed.duplicate.help");
          throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help1 + " " + help2);
        }
      } else {
        UUID key = registryManager.register(resource, organisation, ipt);
        if (key == null) {
          throw new RegistryException(RegistryException.Type.MISSING_METADATA, null,
              "No key returned for registered resource");
        }
        // display success to user
        alog.info("manage.overview.resource.registered", new String[]{organisation.getName()});

        // change status to registered
        resource.setStatus(PublicationStatus.REGISTERED);

        // ensure alternate identifier for Registry UUID set
        updateAlternateIdentifierForRegistry(resource);

        // update stored resources
        resourceManager.updateStoredResources(resource);
      }
      // save all changes to resource
      resourceManager.save(resource);
    } else {
      LOG.error("Registration request failed: the resource must be public. Status={}", resource.getStatus().toString());
    }
  }

  @Override
  public synchronized void report(String shortname, StatusReport report) {
    processReports.put(shortname, report);
  }

  @Override
  public void updateRegistration(Resource resource, BaseAction action) throws PublicationException {
    if (resource.isRegistered()) {
      // prevent null action from being handled
      if (action == null) {
        action = new BaseAction(textProvider, cfg, registrationManager);
      }
      try {
        LOG.debug("Updating registration of resource with key: {}", resource.getKey().toString());

        // get IPT key
        String iptKey = null;
        if (registrationManager.getIpt() != null) {
          iptKey =
              (registrationManager.getIpt().getKey() == null) ? null : registrationManager.getIpt().getKey().toString();
        }

        // perform update
        registryManager.updateResource(resource, iptKey);
      } catch (RegistryException e) {
        // log as specific error message as possible about why the Registry error occurred
        String msg = RegistryException.logRegistryException(e, action);
        action.addActionError(msg);
        LOG.error(msg);
        // add error message that explains the root cause of the Registry error to user
        msg = action.getText("admin.config.updateMetadata.resource.fail.registry", new String[]{e.getMessage()});
        action.addActionError(msg);
        LOG.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      } catch (InvalidConfigException e) {
        String msg = action.getText("manage.overview.failed.resource.update", new String[]{e.getMessage()});
        action.addActionError(msg);
        LOG.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      }
    }
  }

  @Override
  public void visibilityToPrivate(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(InvalidConfigException.TYPE.RESOURCE_ALREADY_REGISTERED,
          "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      // update visibility to private
      resource.setPendingStatus(PublicationStatus.PRIVATE);

      // Changing the visibility means some public alternateIds need to be removed, e.g. IPT URL
      // not applicable for data packages
      if (!resource.isDataPackage()) {
        updateAlternateIdentifierForIPTURLToResource(resource);
      }

      // save all changes to resource
      resourceManager.save(resource);
    }
  }

  @Override
  public void visibilityToPublic(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(InvalidConfigException.TYPE.RESOURCE_ALREADY_REGISTERED,
          "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      // update visibility to public
      resource.setPendingStatus(PublicationStatus.PUBLIC);

      // erase make public date
      resource.setMakePublicDate(null);

      // Changing the visibility means some public alternateIds need to be added, e.g. IPT URL
      // not applicable for data packages
      if (!resource.isDataPackage()) {
        updateAlternateIdentifierForIPTURLToResource(resource);
      }

      // save all changes to resource
      resourceManager.save(resource);
    }
  }

  @Override
  public void updatePublicationMode(Resource resource) {
    if (resource.usesAutoPublishing()) {
      updateNextPublishedDate(new Date(), resource);
    } else {
      resource.setNextPublished(null);
    }
  }

  @Override
  public synchronized void cleanArchiveVersions(Resource resource) {
    if (cfg.isArchivalMode() && cfg.getArchivalLimit() != null && cfg.getArchivalLimit() > 0) {
      LOG.info("Archival mode is ON with a limit of {} elements)", cfg.getArchivalLimit());
      LOG.info("Clean archive versions, if needed, for resource: {}", resource.getShortname());
      List<VersionHistory> history = resource.getVersionHistory();
      if (history.size() > cfg.getArchivalLimit()) {
        for (int i = cfg.getArchivalLimit(); i < history.size(); i++) {
          VersionHistory oldVersion = history.get(i);
          try {
            BigDecimal version = new BigDecimal(oldVersion.getVersion());
            LOG.info("Deleting archive version {} for resource: {}", version, resource.getShortname());
            removeArchiveVersion(resource.getShortname(), version);
          } catch (Exception e) {
            LOG.error("Cannot delete old archive versions for resource: {}", resource.getShortname(), e);
            return;
          }
        }
      }
    }
  }

  @Override
  public boolean hasMaxProcessFailures(Resource resource) {
    String resourceShortname = resource.getShortname();

    if (processFailures.containsKey(resourceShortname)) {
      List<Date> failures = processFailures.get(resourceShortname);
      int count = failures.size();

      LocalDate today = LocalDate.now();
      LocalDate last = lastLoggedFailures.get(resourceShortname);

      if (count < MAX_PROCESS_FAILURES) { // always log if count is below max
        LOG.debug("Publication has failed {} time(s) for resource: {}",
            count, resource.getTitleAndShortname());
      } else if (last == null || !last.equals(today)) { // once the limit is reached, only log once per day
        LOG.debug("Publication has failed {} time(s) for resource: {} (max amount of failures)",
            count, resource.getTitleAndShortname());
        lastLoggedFailures.put(resourceShortname, today);
      }

      return count >= MAX_PROCESS_FAILURES;
    }
    return false;
  }

  @Override
  public String calculateChecksum(File file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    InputStream fis = new FileInputStream(file);

    byte[] byteArray = new byte[1024];
    int bytesCount = 0;

    while ((bytesCount = fis.read(byteArray)) != -1) {
      digest.update(byteArray, 0, bytesCount);
    }
    fis.close();

    byte[] bytes = digest.digest();

    // Convert to hex string
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }

  /**
   * Calculates a checksum of the DwC archive or the data package.
   *
   * @param archive archive
   * @return checksum of the archive
   */
  @Override
  public String calculateArchiveChecksum(File archive) throws Exception {
    // Create a MessageDigest instance for SHA-256
    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    try (ZipFile zipFile = new ZipFile(archive)) {
      // Iterate through the files in the DwCA
      zipFile.stream().forEach(entry -> {
        // Skip the EML metadata file
        if (entry.getName().endsWith(".xml") && entry.getName().toLowerCase().contains("eml")) {
          return;
        }

        // Skip the data package metadata file
        if (entry.getName().endsWith(".json") && entry.getName().toLowerCase().contains("datapackage")) {
          return;
        }

        // If it's a data file, calculate its checksum
        try (InputStream is = zipFile.getInputStream(entry)) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
          }
        } catch (IOException e) {
          LOG.error("Failed to read data", e);
        }
      });
    }

    // Convert the final checksum to a hex string
    byte[] hashBytes = digest.digest();
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      hexString.append(String.format("%02x", b));
    }
    return hexString.toString();
  }

  @Override
  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  @Override
  public Map<String, Future<Map<String, Integer>>> getProcessFutures() {
    return processFutures;
  }

  @Override
  public ListValuedMap<String, Date> getProcessFailures() {
    return processFailures;
  }

  @Override
  public Map<String, StatusReport> getProcessReports() {
    return processReports;
  }

  @Override
  public void clearProcessReports() {
    processReports.clear();
  }

  /**
   * Update the resource's registration (if registered) and persist any changes to the resource.
   * </br>
   * Publishing is split into 2 parts because DwC-A generation is asynchronous. This 2nd part of publishing can only
   * be called after DwC-A has completed successfully.
   *
   * @param resource resource
   * @param action   action
   * @param version  version number to finalize publishing
   * @throws PublicationException   if publication was unsuccessful
   * @throws InvalidConfigException if resource configuration could not be saved
   */
  private void publishEnd(Resource resource, BaseAction action, BigDecimal version)
      throws PublicationException, InvalidConfigException {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    BigDecimal replacedMetadataVersion = resource.getReplacedMetadataVersion();

    // update the resource's registration (if registered), even if it is a metadata-only resource.
    updateRegistration(resource, action);
    // set the last published date
    resource.setLastPublished(new Date());
    // set the next published date (if resource configured for auto-publishing)
    updateNextPublishedDate(new Date(), resource);
    // register/update DOI
    executeDoiWorkflow(resource, version, replacedMetadataVersion, action);
    // finalise/update version history
    addOrUpdateVersionHistory(resource, version, true, action);

    // visibility change - erase pending status, update visibility
    if (resource.getPendingStatus() != null) {
      resource.setStatus(resource.getPendingStatus());
      resource.setPendingStatus(null);
    }

    // remove resource from the list if it's private
    if (resource.getStatus() == PublicationStatus.PRIVATE) {
      resourceManager.removePublishedPublicVersion(resource.getShortname());
    }
    // persist resource object changes
    resourceManager.save(resource);
    // if archival mode is NOT turned on, don't keep former archive version (version replaced)
    if (!cfg.isArchivalMode() && version.compareTo(replacedMetadataVersion) != 0) {
      removeArchiveVersion(resource.getShortname(), replacedMetadataVersion);
    }
    // clean archive versions
    if (cfg.isArchivalMode() && cfg.getArchivalLimit() != null && cfg.getArchivalLimit() > 0) {
      cleanArchiveVersions(resource);
    }
    // final logging
    String msg = action
        .getText("publishing.success", new String[]{String.valueOf(resource.getMetadataVersion()), resource.getShortname()});
    action.addActionMessage(msg);
    LOG.info(msg);
  }

  /**
   * Updates the date the resource is scheduled to be published next. The resource must have been configured with
   * a maintenance update frequency that is suitable for auto-publishing (annually, biannually, monthly, weekly,
   * daily), and have auto-publishing mode turned on for this update to take place.
   *
   * @param resource resource
   * @throws PublicationException if the next published date cannot be set for any reason
   */
  @Override
  public void updateNextPublishedDate(Date currentDate, Resource resource) throws PublicationException {
    if (resource.usesAutoPublishing()) {
      try {
        LOG.debug("Updating next published date of resource: {}", resource.getShortname());

        Date nextPublished = null;

        // get update period
        MaintenanceUpdateFrequency frequency = resource.getUpdateFrequency();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        // Using the old auto publish configuration
        if (resource.isDeprecatedAutoPublishingConfiguration()) {
          // use predefined period for previous IPT version
          int days = frequency.getPeriodInDays();
          cal.add(Calendar.DATE, days);
          nextPublished = cal.getTime();
        }
        // Using the new auto publish configuration
        else {
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
          switch (frequency) {
            case ANNUALLY:
              cal.set(Calendar.MONTH, resource.getUpdateFrequencyMonth().getMonthId());
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            case BIANNUALLY:
              cal.set(Calendar.MONTH, resource.getUpdateFrequencyBiMonth().getBiMonthId());
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.MONTH, 6);
                nextPublished = cal.getTime();
                if (nextPublished.before(currentDate)) {
                  cal.add(Calendar.MONTH, 6);
                  nextPublished = cal.getTime();
                }
              }
              break;
            case MONTHLY:
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.MONTH, 1);
                nextPublished = cal.getTime();
              }
              break;
            case WEEKLY:
              cal.set(Calendar.DAY_OF_WEEK, resource.getUpdateFrequencyDayOfWeek().getDayId());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            case DAILY:
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            default:
              // Do not process others
              break;
          }
        }

        // alert user that auto publishing has been turned on
        if (resource.getNextPublished() == null) {
          LOG.debug("Auto-publishing turned on");
        }

        if (nextPublished == null) {
          String msg = "Error to compute the next publication date";
          LOG.error(msg);
          throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg);
        }

        // set next published date
        resource.setNextPublished(nextPublished);

        // log
        LOG.debug("The next publication date is: {}", nextPublished);
      } catch (Exception e) {
        resource.setNextPublished(null);
        // add error message that explains the consequence of the error to user
        String msg = "Auto-publishing failed: " + e.getMessage();
        LOG.error(msg, e);
        throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg, e);
      }
    } else {
      resource.setNextPublished(null);
      LOG.debug("Resource: {} has not been configured to use auto-publishing", resource.getShortname());
    }
  }

  private boolean isOnlyFileSources(@NotNull Resource resource) {
    return resource.getSources().stream()
        .allMatch(s -> s.isFileSource() || s.isExcelSource());
  }

  private boolean isMetadataModifiedSinceLastPublication(@NotNull Resource resource) {
    Date lastPublished = resource.getLastPublished();
    Date metadataModified = resource.getMetadataModified();

    return metadataModified == null
        || lastPublished == null
        || metadataModified.after(lastPublished);
  }

  private boolean isSourcesModifiedSinceLastPublication(@NotNull Resource resource) {
    Date lastPublished = resource.getLastPublished();
    Date sourcesModified = resource.getSourcesModified();

    return sourcesModified != null
        && (lastPublished == null || sourcesModified.after(lastPublished));
  }

  /**
   * For a candidate UUID, find out:
   * -how many public resources have a matching alternate identifier UUID
   * -how many registered resources have the same UUID
   *
   * @param candidate UUID
   * @param shortname shortname of resource to exclude from matching
   * @return list of names of resources that have matched candidate UUID
   */
  @Override
  public List<String> detectDuplicateUsesOfUUID(UUID candidate, String shortname) {
    ListValuedMap<UUID, String> duplicateUses = new ArrayListValuedHashMap<>();
    for (Resource other : resourceManager.list()) {
      // only resources having a different shortname should be matched against
      if (!other.getShortname().equalsIgnoreCase(shortname)) {
        // are there public resources with this alternate identifier?
        if (other.getStatus().equals(PublicationStatus.PUBLIC)) {
          Set<UUID> otherCandidateUUIDs = collectCandidateResourceUUIDsFromAlternateIds(other);
          if (!otherCandidateUUIDs.isEmpty()) {
            for (UUID otherCandidate : otherCandidateUUIDs) {
              if (otherCandidate.equals(candidate)) {
                duplicateUses.put(candidate, other.getTitleAndShortname());
              }
            }
          }
        }
        // are there registered resources with this UUID?
        else if (other.getStatus().equals(PublicationStatus.REGISTERED)) {
          if (other.getKey().equals(candidate)) {
            duplicateUses.put(candidate, other.getTitleAndShortname());
          }
        }
      }
    }
    return duplicateUses.get(candidate);
  }

  /**
   * Collect a set of UUIDs from the resource's list of alternate identifiers that could qualify as GBIF Registry
   * Dataset UUIDs.
   *
   * @param resource resource
   * @return set of UUIDs that could qualify as GBIF Registry Dataset UUIDs
   */
  private Set<UUID> collectCandidateResourceUUIDsFromAlternateIds(Resource resource) {
    Set<UUID> ls = new HashSet<>();
    if (resource.getEml() != null && !resource.isDataPackage()) {
      List<String> ids = resource.getEml().getAlternateIdentifiers();
      for (String id : ids) {
        try {
          UUID uuid = UUID.fromString(id);
          ls.add(uuid);
        } catch (IllegalArgumentException e) {
          // skip, isn't a candidate UUID
        }
      }
    } else if (resource.getDataPackageMetadata() != null
        && resource.getDataPackageMetadata() instanceof CamtrapMetadata metadata
        && CAMTRAP_DP.equals(resource.getCoreType())) {
      List<RelatedIdentifier> relatedIdentifiers = metadata.getRelatedIdentifiers();
      for (RelatedIdentifier identifier : relatedIdentifiers) {
        if (identifier != null && identifier.getRelatedIdentifier() != null
            && identifier.getRelatedIdentifier().contains("gbif")
            && identifier.getRelatedIdentifierType() == RelatedIdentifier.RelatedIdentifierType.URL) {
          String[] urlParts = identifier.getRelatedIdentifier().split("/");
          if (urlParts.length > 0) {
            String lastSegment = urlParts[urlParts.length - 1];
            try {
              UUID uuid = UUID.fromString(lastSegment);
              ls.add(uuid);
            } catch (IllegalArgumentException e) {
              // skip, isn't a candidate UUID
            }
          }
        }
      }
    }
    return ls;
  }

  /**
   * Publishes a new version of the RTF file for the given resource.
   *
   * @param resource Resource
   * @param version  version number to publish
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishRtf(Resource resource, BigDecimal version) throws PublicationException {
    // Skip RTF for data packages
    if (resource.isDataPackage()) {
      return;
    }

    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
          "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    Document doc = new Document();
    File rtfFile = dataDir.resourceRtfFile(resource.getShortname(), version);
    OutputStream out = null;
    try {
      out = new FileOutputStream(rtfFile);
      RtfWriter2.getInstance(doc, out);
      eml2Rtf.writeEmlIntoRtf(doc, resource);
    } catch (FileNotFoundException e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
          "Can't find rtf file to write metadata to: " + rtfFile.getAbsolutePath(), e);
    } catch (DocumentException e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
          "RTF DocumentException while writing to file: " + rtfFile.getAbsolutePath(), e);
    } catch (Exception e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
          "An unexpected error occurred while writing RTF file: " + e.getMessage(), e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          LOG.warn("FileOutputStream to RTF file could not be closed");
        }
      }
    }
  }

  private void updateCamtrapTemporalScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredTemporalScope() != null
        && inferredMetadata.getInferredTemporalScope().isInferred()) {

      InferredCamtrapTemporalScope inferredTemporalScope = inferredMetadata.getInferredTemporalScope();

      Temporal temporal = new Temporal();
      temporal.setStart(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getStartDate()));
      temporal.setEnd(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getEndDate()));

      ((CamtrapMetadata) resource.getDataPackageMetadata()).setTemporal(temporal);
    }
  }

  private void sendPublicationFailureEmail(Resource resource, BigDecimal version, String reason) {
    if (resourcesToNotifyPublicationFailure.contains(resource.getShortname())
        && PublicationFailureEmailUtils.isConfigured(cfg)) {
      try {
        PublicationFailureEmailUtils.send(cfg, resource, version, reason);
      } catch (MessagingException e) {
        LOG.error("Failed to send publication failure email for resource {}", resource.getShortname(), e);
      }
    }
  }

  /**
   * Depending on the state of the resource and its DOI, execute one of the following operations:
   * - Register DOI
   * - Update DOI
   * - Register DOI and replace previous DOI
   *
   * @param resource        resource published
   * @param version         resource version being published
   * @param versionReplaced resource version being replaced
   * @param action          action
   * @throws PublicationException thrown if any part of DOI workflow failed
   */
  private void executeDoiWorkflow(Resource resource, BigDecimal version, BigDecimal versionReplaced, BaseAction action)
      throws PublicationException {
    // All DOI operations require resource be publicly available, and resource DOI be PUBLIC/PUBLIC_PENDING_PUBLICATION
    if (resource.getDoi() != null && resource.isPubliclyAvailable() && (
        resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION) || resource
            .getIdentifierStatus().equals(IdentifierStatus.PUBLIC))) {
      if (resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION)) {
        if (resource.isAlreadyAssignedDoi()) {
          // another new major version that replaces previous version
          doReplaceDoi(resource, version, versionReplaced);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion.replaces",
              new String[]{resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        } else {
          // initial major version
          doRegisterDoi(resource, null);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion",
              new String[]{resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        }
      } else {
        // minor version increment
        doUpdateDoi(resource);
        String msg = action.getText("manage.overview.publishing.doi.publish.newMinorVersion",
            new String[]{resource.getDoi().toString()});
        LOG.info(msg);
        action.addActionMessage(msg);
      }
    }
  }

  /**
   * Update DOI metadata. The DOI URI isn't changed. This is done for each minor version change.
   *
   * @param resource resource whose DOI will be updated
   */
  @Override
  public void doUpdateDoi(Resource resource) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DOI doi = resource.getDoi();
      try {
        DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
        registrationManager.getDoiService().update(doi, dataCiteMetadata);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doi.toString() + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to update DOI!");
    }
  }

  /**
   * Replace DOI currently assigned to resource with new DOI that has been reserved for resource.
   * This corresponds to a new major version change.
   *
   * @param resource        resource whose DOI will be registered
   * @param version         new version
   * @param replacedVersion previous version being replaced
   */
  @Override
  public void doReplaceDoi(Resource resource, BigDecimal version, BigDecimal replacedVersion) {
    Objects.requireNonNull(resource);

    DOI doiToRegister = resource.getDoi();
    DOI doiToReplace = resource.getAssignedDoi();

    if (doiToRegister != null && resource.isPubliclyAvailable() && doiToReplace != null
        && resource.getMetadataVersion() != null && resource.getMetadataVersion().compareTo(version) == 0
        && replacedVersion != null && resource.findVersionHistory(replacedVersion) != null) {

      // register new DOI first, indicating it replaces former DOI
      doRegisterDoi(resource, doiToReplace);

      // update previously assigned DOI, indicating it has been replaced by new DOI
      try {
        // reconstruct last published version (version being replaced)
        File replacedVersionEmlFile = dataDir.resourceEmlFile(resource.getShortname(), replacedVersion);
        Resource lastPublishedVersion = ResourceUtils
            .reconstructVersion(replacedVersion, resource.getShortname(), resource.getCoreType(), resource.getDataPackageIdentifier(), doiToReplace, resource.getOrganisation(),
                resource.findVersionHistory(replacedVersion), replacedVersionEmlFile, resource.getKey());

        DataCiteMetadata assignedDoiMetadata =
            DataCiteMetadataBuilder.createDataCiteMetadata(doiToReplace, lastPublishedVersion);

        // add isPreviousVersionOf new resource version registered above
        DataCiteMetadataBuilder.addIsPreviousVersionOfDOIRelatedIdentifier(assignedDoiMetadata, doiToRegister);

        // update its URI first
        URI resourceVersionUri = cfg.getResourceVersionUri(resource.getShortname(), replacedVersion);
        registrationManager.getDoiService().update(doiToReplace, resourceVersionUri);
        // then update its metadata
        registrationManager.getDoiService().update(doiToReplace, assignedDoiMetadata);

      } catch (InvalidMetadataException e) {
        String errorMsg = "Failed to update " + doiToReplace + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (IllegalArgumentException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg, e);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to replace DOI!");
    }
  }

  /**
   * Register DOI. Corresponds to a major version change.
   *
   * @param resource resource whose DOI will be registered
   */
  @Override
  public void doRegisterDoi(Resource resource, @Nullable DOI replaced) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DataCiteMetadata dataCiteMetadata = null;
      DOI doi = resource.getDoi();
      try {
        // DOI resolves to IPT public resource page
        URI uri = cfg.getResourceUri(resource.getShortname());
        dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);

        // if this resource (DOI) replaces a former resource version (DOI) add isNewVersionOf RelatedIdentifier
        if (replaced != null) {
          DataCiteMetadataBuilder.addIsNewVersionOfDOIRelatedIdentifier(dataCiteMetadata, replaced);
        }

        registrationManager.getDoiService().register(doi, uri, dataCiteMetadata);
        resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
        resource.updateAlternateIdentifierForDOI();
        resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
      } catch (DoiExistsException e) {
        LOG.warn(
            "Received DoiExistsException registering resource meaning this is an existing DOI that should be updated instead",
            e);
        try {
          registrationManager.getDoiService().update(doi, dataCiteMetadata);
          resource.setIdentifierStatus(
              IdentifierStatus.PUBLIC); // must transition reused (registered DOI) from public_pending_publication to public
          resource.updateAlternateIdentifierForDOI();
          resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
        } catch (DoiException e2) {
          String errorMsg = "Failed to update existing DOI  " + doi.toString() + ": " + e2.getMessage();
          LOG.error(errorMsg, e2);
          throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e2);
        }
      } catch (InvalidMetadataException e) {
        String errorMsg =
            "Failed to register " + doi.toString() + " because DOI metadata was invalid: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to register " + doi.toString() + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DOI_REGISTRATION,
          "Resource not in required state to register DOI!");
    }
  }

  private void restoreDarwinCoreResourceVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // determine version to restore (looking at version history)
    BigDecimal toRestore = getVersionToRestore(resource, rollingBack);

    if (toRestore != null) {
      String shortname = resource.getShortname();
      LOG.info(
          "Rolling back version #" + rollingBack.toPlainString() + ". Restoring version #" + toRestore.toPlainString()
              + " of resource " + shortname);

      try {
        // delete eml-1.1.xml if it exists (eml.xml must remain)
        File versionedEMLFile = dataDir.resourceEmlFile(shortname, rollingBack);
        if (versionedEMLFile.exists()) {
          FileUtils.forceDelete(versionedEMLFile);
        }
        // delete shortname-1.1.rtf if it exists
        File versionedRTFFile = dataDir.resourceRtfFile(shortname, rollingBack);
        if (versionedRTFFile.exists()) {
          FileUtils.forceDelete(versionedRTFFile);
        }
        // delete dwca-1.1.zip if it exists
        File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, rollingBack);
        if (versionedDwcaFile.exists()) {
          FileUtils.forceDelete(versionedDwcaFile);
        }

        // remove VersionHistory of version being rolled back
        resource.removeVersionHistory(rollingBack);

        // reset recordsPublished count from restored VersionHistory
        VersionHistory restoredVersionVersionHistory = resource.findVersionHistory(toRestore);
        if (restoredVersionVersionHistory != null) {
          resource.setRecordsPublished(restoredVersionVersionHistory.getRecordsPublished());
        }

        // update version
        resource.setMetadataVersion(toRestore);

        // update replaced version with next last version
        if (resource.getVersionHistory().size() > 1) {
          BigDecimal replacedVersion = new BigDecimal(resource.getVersionHistory().get(1).getVersion());
          resource.setReplacedEmlVersion(replacedVersion);
        }

        // persist resource.xml changes
        resourceManager.save(resource);

        // restore EML pubDate to last published date (provided last published date exists)
        if (resource.getLastPublished() != null) {
          resource.getEml().setPubDate(resource.getLastPublished());
        }

        // persist EML changes
        resourceManager.saveEml(resource);

      } catch (IOException e) {
        String msg = action
            .getText("restore.resource.failed", new String[]{toRestore.toPlainString(), shortname, e.getMessage()});
        LOG.error(msg, e);
        action.addActionError(msg);
      }
      // alert user version rollback was successful
      String msg = action.getText("restore.resource.success", new String[]{toRestore.toPlainString(), shortname});
      LOG.info(msg);
      action.addActionMessage(msg);
      // update StatusReport on publishing page
      // Warning: don't retrieve status report using status() otherwise a cyclical call to isLocked results
      StatusReport report = processReports.get(shortname);
      if (report != null) {
        report.getMessages().add(new TaskMessage(Level.INFO, msg));
      }
    } else {
      String msg = action
          .getText("restore.resource.failed.version.notFound", new String[]{rollingBack.toPlainString()});
      LOG.error(msg);
      action.addActionError(msg);
    }
  }

  private void restoreDataPackageResourceVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // determine version to restore (looking at version history)
    BigDecimal toRestore = getVersionToRestore(resource, rollingBack);

    if (toRestore != null) {
      String shortname = resource.getShortname();
      LOG.info(
          "Rolling back version #" + rollingBack.toPlainString() + ". Restoring version #" + toRestore.toPlainString()
              + " of resource " + shortname);

      try {
        // delete versioned EML file if it exists (eml.xml must remain)
        File versionedEMLFile = dataDir.resourceEmlFile(shortname, rollingBack);
        if (versionedEMLFile.exists()) {
          FileUtils.forceDelete(versionedEMLFile);
        }

        // delete versioned metadata file if exists (datapackage.json must remain)
        File versionedCamtrapMetadataFile = dataDir.resourceDatapackageMetadataFile(shortname, CAMTRAP_DP, rollingBack);
        if (versionedCamtrapMetadataFile.exists()) {
          FileUtils.forceDelete(versionedCamtrapMetadataFile);
        }

        File versionedColMetadataFile = dataDir.resourceDatapackageMetadataFile(shortname, COL_DP, rollingBack);
        if (versionedColMetadataFile.exists()) {
          FileUtils.forceDelete(versionedColMetadataFile);
        }

        // delete versioned data package archive if exists
        File versionedDataPackageFile = dataDir.resourceDataPackageFile(shortname, rollingBack);
        if (versionedDataPackageFile.exists()) {
          FileUtils.forceDelete(versionedDataPackageFile);
        }

        // remove VersionHistory of version being rolled back
        resource.removeVersionHistory(rollingBack);

        // update version
        resource.setMetadataVersion(toRestore);

        // update replaced version with next last version
        if (resource.getVersionHistory().size() > 1) {
          BigDecimal replacedVersion = new BigDecimal(resource.getVersionHistory().get(1).getVersion());
          resource.setReplacedDataPackageMetadataVersion(replacedVersion);
          resource.setReplacedEmlVersion(replacedVersion);
        }

        // persist resource.xml changes
        resourceManager.save(resource);

        // persist metadata changes
        resourceManager.saveDatapackageMetadata(resource);

      } catch (IOException e) {
        String msg = action
            .getText("restore.resource.failed", new String[]{toRestore.toPlainString(), shortname, e.getMessage()});
        LOG.error(msg, e);
        action.addActionError(msg);
      }
      // alert user version rollback was successful
      String msg = action.getText("restore.resource.success", new String[]{toRestore.toPlainString(), shortname});
      LOG.info(msg);
      action.addActionMessage(msg);
      // update StatusReport on publishing page
      // Warning: don't retrieve status report using status() otherwise a cyclical call to isLocked results
      StatusReport report = processReports.get(shortname);
      if (report != null) {
        report.getMessages().add(new TaskMessage(Level.INFO, msg));
      }
    } else {
      String msg = action
          .getText("restore.resource.failed.version.notFound", new String[]{rollingBack.toPlainString()});
      LOG.error(msg);
      action.addActionError(msg);
    }
  }

  private void publishMetadata(Resource resource, BigDecimal version, BaseAction action) throws PublicationException {
    if (resource.isDataPackage()) {
      publishDataPackageMetadata(resource, version);
      if (resource.isDwcDp()) {
        publishEml(resource, version, action);
      }
    } else {
      publishEml(resource, version, action);
    }
  }

  /**
   * Publishes a new version of the EML file for the given resource.
   *
   * @param resource Resource
   * @param version  version number to publish
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishEml(Resource resource, BigDecimal version, BaseAction action) throws PublicationException {
    String shortname = resource.getShortname();

    // check if publishing task is already running
    if (isLocked(shortname)) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
          "Resource " + shortname + " is currently locked by another process");
    }

    if (resource.isMetadataOnly()) {
      StatusReport report = new StatusReport("Started publishing EML #" + version, new ArrayList<>());
      processReports.put(shortname, report);
      getTaskMessages(shortname).add(
          new TaskMessage(Level.INFO, "EML generation started for version #" + version));
    }

    // ensure alternate identifier for Registry UUID is set - if resource is registered
    updateAlternateIdentifierForRegistry(resource);
    // ensure alternate identifier for IPT URL to resource is set - if resource is public
    updateAlternateIdentifierForIPTURLToResource(resource);
    // update eml version
    resource.setMetadataVersion(version);
    // update eml pubDate (represents date when the resource was last published)
    resource.getEml().setPubDate(new Date());
    // set eml dateStamp (represents date when the resource was published for the first time). Do only once
    if (resource.getEml().getDateStamp() == null) {
      resource.getEml().setDateStamp(new Date());
    }
    // update resource citation with auto generated citation (if auto-generation has been turned on)
    if (resource.isCitationAutoGenerated()) {
      URI homepage = cfg.getResourceVersionUri(shortname, version); // potential citation identifier
      String citation = resource.generateResourceCitation(version, homepage);
      if (resource.getEml().getCitation() != null) {
        resource.getEml().getCitation().setCitation(citation);
      } else {
        Citation c = new Citation();
        c.setCitation(citation);
        resource.getEml().setCitation(c);
      }
    }
    // update eml with inferred data (if infer automatically is turned on)
    if (resource.isInferGeocoverageAutomatically()
        || resource.isInferTaxonomicCoverageAutomatically()
        || resource.isInferTemporalCoverageAutomatically()) {
      InferredEmlMetadata inferredMetadata = (InferredEmlMetadata) resourceMetadataInferringService.inferMetadata(resource);
      // save inferred metadata
      resource.setInferredMetadata(inferredMetadata);
      resourceManager.saveInferredMetadata(resource);

      if (resource.isInferGeocoverageAutomatically()) {
        updateEmlGeocoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTaxonomicCoverageAutomatically()) {
        updateEmlTaxonomicCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTemporalCoverageAutomatically()) {
        updateEmlTemporalCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }
    }

    // save all changes to Eml
    resourceManager.saveEml(resource);

    // create versioned eml file
    File trunkFile = dataDir.resourceEmlFile(shortname);

    // validate EML (only for metadata-only resources, otherwise it will be validated afterward)
    if (METADATA.toString().equalsIgnoreCase(resource.getCoreType())) {
      try {
        EmlValidator emlValidator = EmlValidator.newValidator(EMLProfileVersion.GBIF_1_3);
        String emlString = FileUtils.readFileToString(trunkFile, StandardCharsets.UTF_8);

        getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "? Validating EML file"));
        emlValidator.validate(emlString);
        getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "✓ Validated EML file"));
        StatusReport report = new StatusReport(
            true,
            action.getText("publishing.success", new String[]{version.toPlainString(), shortname}),
            getTaskMessages(shortname));
        processReports.put(shortname, report);
      } catch (IOException | SAXException e) {
        getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, "Failed to validate EML"));
        PublicationException exception = new PublicationException(PublicationException.TYPE.EML,
            "Can't publish eml file for resource " + shortname + ". Failed to validate EML", e);
        StatusReport errorReport = new StatusReport(
            exception,
            action.getText("publishing.failed", new String[]{version.toPlainString(), shortname, "Failed to validate EML"}),
            getTaskMessages(shortname));
        processReports.put(shortname, errorReport);
        throw exception;
      } catch (InvalidEmlException e) {
        getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, "Invalid EML:  " + e.getMessage()));
        PublicationException exception = new PublicationException(PublicationException.TYPE.EML,
            "Can't publish eml file for resource " + resource.getShortname() + ". Invalid EML", e);
        StatusReport errorReport = new StatusReport(
            exception,
            action.getText("publishing.failed", new String[]{version.toPlainString(), shortname, "Invalid EML"}),
            getTaskMessages(shortname));
        processReports.put(shortname, errorReport);
        throw exception;
      }
    }

    File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);

    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
          "Can't publish eml file for resource " + resource.getShortname(), e);
    }
  }

  private void updateCamtrapGeographicScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredGeographicScope() != null
        && inferredMetadata.getInferredGeographicScope().isInferred()) {

      Geojson geojson = new Geojson();
      geojson.setType(Geojson.Type.POLYGON);
      List<List<List<Double>>> coordinates = new ArrayList<>();
      InferredCamtrapGeographicScope inferredScope = inferredMetadata.getInferredGeographicScope();

      coordinates.add(
          Arrays.asList(
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMinLatitude()),
              Arrays.asList(inferredScope.getMaxLongitude(), inferredScope.getMinLatitude()),
              Arrays.asList(inferredScope.getMaxLongitude(), inferredScope.getMaxLatitude()),
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMaxLatitude()),
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMinLatitude())
          )
      );

      geojson.setCoordinates(coordinates);

      ((CamtrapMetadata) resource.getDataPackageMetadata()).setSpatial(geojson);
    }
  }

  private void updateEmlTaxonomicCoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredTaxonomicCoverage() != null
        && inferredMetadata.getInferredTaxonomicCoverage().getData() != null) {
      TaxonomicCoverage inferredTaxonomicCoverage = inferredMetadata.getInferredTaxonomicCoverage().getData();

      // check object to preserve description
      if (!resource.getEml().getTaxonomicCoverages().isEmpty()) {
        inferredTaxonomicCoverage.setDescription(resource.getEml().getTaxonomicCoverages().get(0).getDescription());
      } else {
        inferredTaxonomicCoverage.setDescription("N/A");
      }
      resource.getEml().getTaxonomicCoverages().clear();
      resource.getEml().addTaxonomicCoverage(inferredTaxonomicCoverage);
    }
  }

  private void updateCamtrapTaxonomicScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredTaxonomicScope() != null
        && inferredMetadata.getInferredTaxonomicScope().isInferred()) {

      InferredCamtrapTaxonomicScope inferredTaxonomicScope = inferredMetadata.getInferredTaxonomicScope();
      ((CamtrapMetadata) resource.getDataPackageMetadata()).setTaxonomic(inferredTaxonomicScope.getData());
    }
  }

  private void updateEmlTemporalCoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredTemporalCoverage() != null
        && inferredMetadata.getInferredTemporalCoverage().getData() != null) {
      TemporalCoverage inferredTemporalCoverage = inferredMetadata.getInferredTemporalCoverage().getData();
      resource.getEml().getTemporalCoverages().clear();
      resource.getEml().addTemporalCoverage(inferredTemporalCoverage);
    }
  }

  /**
   * After ensuring the version being rolled back is equal to the last version of the resource attempted to be
   * published, the method returns the last successfully published version, which is the version to restore.
   *
   * @param resource   resource
   * @param toRollBack version to rollback
   * @return the version to restore, or null if version history is invalid
   */
  private BigDecimal getVersionToRestore(@NotNull Resource resource, @NotNull BigDecimal toRollBack) {
    BigDecimal lastVersion = resource.getLastVersionHistoryVersion();
    BigDecimal penultimateVersion = resource.getLastPublishedVersionsVersion();

    // return penultimate version if all checks pass
    if (penultimateVersion != null && penultimateVersion.compareTo(Constants.INITIAL_RESOURCE_VERSION) >= 0
        && lastVersion != null && lastVersion.compareTo(toRollBack) == 0
        && penultimateVersion.compareTo(lastVersion) != 0) {
      return penultimateVersion;
    }
    return null;
  }

  private void updateEmlGeocoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredGeographicCoverage() != null
        && inferredMetadata.getInferredGeographicCoverage().getData() != null) {
      GeospatialCoverage inferredGeocoverage = inferredMetadata.getInferredGeographicCoverage().getData();

      // check object to preserve description
      if (!resource.getEml().getGeospatialCoverages().isEmpty()) {
        inferredGeocoverage.setDescription(resource.getEml().getGeospatialCoverages().get(0).getDescription());
      } else {
        inferredGeocoverage.setDescription("N/A");
      }
      resource.getEml().getGeospatialCoverages().clear();
      resource.getEml().addGeospatialCoverage(inferredGeocoverage);
    }
  }

  private void preventPublicationForSourcesInProcessingState(Resource resource) {
    Optional<Source> sourceBeingProcessed = resource.getMappings().stream()
        .map(ExtensionMapping::getSource)
        .filter(Source::isProcessing)
        .findAny();

    if (sourceBeingProcessed.isPresent()) {
      PublicationException e = new PublicationException(
          PublicationException.TYPE.LOCKED,
          "Resource's " + resource.getShortname() + " source " + sourceBeingProcessed.get() + " is currently being processed");
      e.addAdditionalParameter("source", sourceBeingProcessed.get().getName());

      throw e;
    }
  }

  /**
   * Construct or update the VersionHistory for version v of resource, and make sure that it is added to the resource's
   * VersionHistory List.
   *
   * @param resource  resource published
   * @param version   version of resource published
   * @param published true if this version has been published successfully, false otherwise
   * @param action    action
   */
  protected synchronized void addOrUpdateVersionHistory(Resource resource, BigDecimal version, boolean published,
                                                        BaseAction action) {
    LOG.info("Adding or updating version: {}", version.toPlainString());

    VersionHistory versionHistory;
    // Construct new VersionHistory, or update existing one if it exists
    VersionHistory existingVersionHistory = resource.findVersionHistory(version);
    if (existingVersionHistory == null) {
      PublicationStatus publicationStatus =
          resource.getPendingStatus() != null ? resource.getPendingStatus() : resource.getStatus();
      versionHistory = new VersionHistory(version, publicationStatus);
      resource.addVersionHistory(versionHistory);
      LOG.info("Adding VersionHistory for version {}", version.toPlainString());
    } else {
      versionHistory = existingVersionHistory;
      LOG.info("Updating VersionHistory for version {}", version.toPlainString());
    }

    // DOI
    versionHistory.setDoi(resource.getDoi());
    // DOI status
    versionHistory.setStatus(resource.getIdentifierStatus());
    // change summary
    versionHistory.setChangeSummary(resource.getChangeSummary());
    // core records published
    versionHistory.setRecordsPublished(resource.getRecordsPublished());
    // record published by extension
    versionHistory.setRecordsByExtension(resource.getRecordsByExtension());
    // modifiedBy
    User modifiedBy = action.getCurrentUser();
    if (modifiedBy != null) {
      versionHistory.setModifiedBy(modifiedBy);
    }
    // released - only set when version was published successfully
    if (published) {
      versionHistory.setReleased(new Date());
    }
  }

  private Integer getResourceRecordsCount(Resource resource) {
    Integer recordCount;
    if (resource.isDataPackage()) {
      // take number of observations as number of records for Camtrap
      // for the rest data packages - total number of all records
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        recordCount = resource.getRecordsByExtension().get(CAMTRAP_DP_OBSERVATIONS);
      } else {
        recordCount = resource.getRecordsByExtension().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
      }
    } else {
      recordCount = resource.getRecordsByExtension().get(StringUtils.trimToEmpty(resource.getCoreRowType()));
    }
    return recordCount != null ? recordCount : 0;
  }

  private Integer getResourceRecordsCount(Resource resource, Map<String, Integer> publishedRecordsByExtension) {
    Integer recordCount;
    if (resource.isDataPackage()) {
      // take number of observations as number of records for Camtrap
      // for the rest data packages - total number of all records
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        recordCount = publishedRecordsByExtension.get(CAMTRAP_DP_OBSERVATIONS);
      } else {
        recordCount = publishedRecordsByExtension.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
      }
    } else {
      recordCount = publishedRecordsByExtension.get(StringUtils.trimToEmpty(resource.getCoreRowType()));
    }
    return recordCount != null ? recordCount : 0;
  }

  /**
   * Return a resource's StatusReport's list of TaskMessage. If no report exists for the resource, return an empty
   * list of TaskMessage.
   *
   * @param shortname resource shortname
   * @return resource's StatusReport's list of TaskMessage or an empty list if no StatusReport exists for resource
   */
  private List<TaskMessage> getTaskMessages(String shortname) {
    return processReports.get(shortname) == null ? new ArrayList<>()
        : processReports.get(shortname).getMessages();
  }
}
