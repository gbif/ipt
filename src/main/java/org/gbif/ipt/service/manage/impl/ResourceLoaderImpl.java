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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceLoader;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.service.manage.ResourcePersister;
import org.gbif.ipt.service.manage.ResourceTypeService;
import org.gbif.ipt.service.manage.ResourceVersioningService;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.ActionLogger;

import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;

/**
 * Reads a complete {@link Resource} (config, EML/inferred metadata, sources, migrations) from its
 * resource directory on disk, applying the same fix-ups and pre-v2.2/v2.2.1 migration logic that
 * used to live directly in {@code ResourceManagerImpl}.
 * <p>
 * This class owns no in-memory index and does not decide when a resource is persisted; where the
 * original logic needed to sync EML fields or save the resource back to disk as a side effect of
 * loading (see {@link #backfillDataPackageVersion}), those are delegated back to the owning manager
 * via the {@code emlSyncer}/{@code resourceSaver} callbacks passed in at construction, to avoid this
 * class depending on {@code ResourceManagerImpl} directly.
 */
public class ResourceLoaderImpl implements ResourceLoader {

  private static final Logger LOG = LogManager.getLogger(ResourceLoaderImpl.class);

  private final AppConfig cfg;
  private final DataDir dataDir;
  private final ExtensionManager extensionManager;
  private final DataPackageSchemaManager schemaManager;
  private final ResourceTypeService resourceTypeService;
  private final ResourceVersioningService resourceVersioningService;
  private final ResourceMetadataLoader resourceMetadataLoader;
  private final SimpleTextProvider textProvider;
  private final RegistrationManager registrationManager;
  private final ResourcePersister resourcePersister;

  @Inject
  public ResourceLoaderImpl(
      AppConfig cfg,
      DataDir dataDir,
      ExtensionManager extensionManager,
      DataPackageSchemaManager schemaManager,
      ResourceTypeService resourceTypeService,
      ResourceVersioningService resourceVersioningService,
      ResourceMetadataLoader resourceMetadataLoader,
      SimpleTextProvider textProvider,
      RegistrationManager registrationManager,
      ResourcePersister resourcePersister) {
    this.dataDir = dataDir;
    this.cfg = cfg;
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.resourceTypeService = resourceTypeService;
    this.resourceVersioningService = resourceVersioningService;
    this.resourceMetadataLoader = resourceMetadataLoader;
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
    this.resourcePersister = resourcePersister;
  }

  /**
   * Calls {@link #load(File, User, ActionLogger, Consumer, Consumer)}, inserting a new instance of ActionLogger.
   *
   * @param resourceDir   resource directory
   * @param creator       User that created resource (only used to populate creator when missing)
   * @param emlSyncer     called on a non-data-package resource after a load, to sync its EML version/GUID/keywords
   *                      with the resource's current state (currently {@code ResourceManagerImpl::syncEmlWithResource})
   * @param resourceSaver called to persist a resource whose data package version was backfilled during a load
   *                      (currently {@code ResourceManagerImpl::save})
   * @return loaded Resource
   */
  @Override
  public Resource load(File resourceDir, @Nullable User creator, Consumer<Resource> emlSyncer, Consumer<Resource> resourceSaver) {
    return load(resourceDir, creator, new ActionLogger(LOG, new BaseAction(textProvider, cfg, registrationManager)), emlSyncer, resourceSaver);
  }

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache.
   */
  @Override
  public Resource load(
      File resourceDir,
      @Nullable User creator,
      ActionLogger alog,
      Consumer<Resource> emlSyncer,
      Consumer<Resource> resourceSaver) throws InvalidConfigException {
    if (resourceDir.exists()) {
      // load full configuration from resource.xml and eml.xml files
      String shortname = resourceDir.getName();
      try {
        File cfgFile = dataDir.resourceFile(shortname);
        InputStream input = new FileInputStream(cfgFile);
        Resource resource = resourcePersister.load(input);

        // populate a missing creator - it cannot be null! (this fixes issue #1309)
        if (creator != null && resource.getCreator() == null) {
          resource.setCreator(creator);
          LOG.warn("On load, populated missing creator for resource: {}", shortname);
        }

        // non-existing users end up being a NULL in the set, so remove them
        // shouldn't really happen - but people can even manually cause a mess
        resource.getManagers().remove(null);

        // 1. Non-existent Extension ends up being NULL
        // E.g., a user is trying to import a resource from one IPT to another without all required extensions installed.
        // 2. Auto-generating IDs are only available for Taxon core extension since IPT v2.1,
        // therefore, if a non-Taxon core extension is using auto-generated IDs, the coreID is set to No ID (-99)
        for (ExtensionMapping ext : resource.getMappings()) {
          Extension x = ext.getExtension();
          if (x == null) {
            alog.warn("manage.resource.create.extension.null", new String[]{ext.getExtensionVerbatim()});
            throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-existent extension");
          } else if (extensionManager.get(x.getRowType()) == null) {
            alog.warn("manage.resource.create.rowType.null", new String[]{x.getRowType()});
            throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-installed extension");
          }
          // is the ExtensionMapping of core type, not taxon core type, and uses a coreIdColumn mapping?
          if (ext.isCore() && !ext.isTaxonCore() && ext.getIdColumn() != null) {
            if (ext.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER) || ext.getIdColumn()
                .equals(ExtensionMapping.IDGEN_UUID)) {
              ext.setIdColumn(ExtensionMapping.NO_ID);
            }
          }
        }

        // shortname persists as folder name, so xstream doesn't handle this:
        resource.setShortname(shortname);

        // infer coreType if null
        if (resource.getCoreType() == null) {
          resourceTypeService.inferCoreType(resource);
        }

        // standardize subtype if not null
        if (resource.getSubtype() != null) {
          resourceTypeService.standardizeSubtype(resource);
        }

        // add proper source file pointer
        for (Source src : resource.getSources()) {
          src.setResource(resource);
          src.setProcessing(false);
          if (src instanceof FileSource frSrc) {
            frSrc.setFile(dataDir.sourceFile(resource, frSrc));
          }
        }

        // pre v2.2 resources: set IdentifierStatus if null
        if (resource.getIdentifierStatus() == null) {
          resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);
        }

        // load metadata (this must be done before trying to convert version below)
        resourceMetadataLoader.loadMetadata(resource);

        // load inferred metadata
        loadInferredMetadata(resource);

        // pre v2.2 resources: convert resource version from integer to major_version.minor_version style
        // also convert/rename eml, rtf, and dwca versioned files also
        if (!resource.isDataPackage()) {
          BigDecimal converted = resourceVersioningService.convertVersion(resource);
          if (converted != null) {
            resourceVersioningService.updateResourceVersion(resource, resource.getMetadataVersion(), converted);
          }
        }

        // pre v2.2 resources: construct a VersionHistory for last published version (if appropriate)
        VersionHistory history = resourceVersioningService.constructVersionHistoryForLastPublishedVersion(resource);
        if (history != null) {
          resource.addVersionHistory(history);
        }

        if (!resource.isDataPackage()) {
          // pre v2.2.1 resources: rename dwca.zip to dwca-18.0.zip (where 18.0 is the last published version for example)
          if (resource.getLastPublishedVersionsVersion() != null) {
            resourceVersioningService.renameDwcaToIncludeVersion(resource, resource.getLastPublishedVersionsVersion());
          }

          // update EML with the latest resource basics (version and GUID)
          emlSyncer.accept(resource);
        }

        // clean up data package mappings (remove dangling field mappings)
        // backfill data package version if not set
        if (resource.isDataPackage()) {
          cleanUpDataPackageMappings(resource);
          backfillDataPackageVersion(resource, resourceSaver);
        }

        LOG.debug("Read resource configuration for {}", shortname);
        return resource;
      } catch (Exception e) {
        LOG.error("Cannot read resource configuration for {}", shortname, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG,
            "Cannot read resource configuration for " + shortname + ": " + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Loads a resource's inferred metadata from the XML file located inside its resource directory.
   * If no inferredMetadata.xml file was found, the resource is loaded with an empty InferredMetadata instance.
   *
   * @param resource resource
   */
  public void loadInferredMetadata(Resource resource) {
    File inferredMetadataFile = dataDir.resourceInferredMetadataFile(resource.getShortname());

    if (resource.isDataPackage()) {
      // skip non-camtrap resources
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        return;
      }

      // no metadata file found - initialize with an empty object
      if (!inferredMetadataFile.exists()) {
        resource.setInferredMetadata(new InferredCamtrapMetadata());
        return;
      }

      // otherwise read the metadata file
      try {
        InputStream input = Files.newInputStream(inferredMetadataFile.toPath());
        InferredCamtrapMetadata inferredMetadata = resourcePersister.loadInferredCamtrapMetadata(input);
        resource.setInferredMetadata(inferredMetadata);
      } catch (Exception e) {
        LOG.error("Cannot read inferred metadata file (Camtrap) for resource {}", resource.getShortname(), e);
        resource.setInferredMetadata(new InferredCamtrapMetadata());
      }
    } else {
      if (inferredMetadataFile == null || !inferredMetadataFile.exists()) {
        resource.setInferredMetadata(new InferredEmlMetadata());
        return;
      }

      try {
        InputStream input = Files.newInputStream(inferredMetadataFile.toPath());
        InferredEmlMetadata inferredMetadata = resourcePersister.loadInferredEmlMetadata(input);
        resource.setInferredMetadata(inferredMetadata);
      } catch (Exception e) {
        LOG.error("Cannot read inferred metadata file (EML) for resource {}", resource.getShortname(), e);
        resource.setInferredMetadata(new InferredEmlMetadata());
      }
    }
  }

  /**
   * Remove field mappings from mappings that do not reference any actual fields.
   * <ol>
   *   <li>Only an index is present, but references no field</li>
   *   <li>Both index and field are absent</li>
   * </ol>
   *
   * @param resource resource
   */
  private void cleanUpDataPackageMappings(Resource resource) {
    for (DataPackageMapping dpm : resource.getDataPackageMappings()) {
      dpm.getFields().removeIf(f -> onlyFieldIndexPresent(f) || emptyMapping(f));
    }
  }

  /**
   * Backfill the data package version if not set.
   *
   * @param resource      resource
   * @param resourceSaver called to persist a resource whose data package version was backfilled during a load
   *                      (currently {@code ResourceManagerImpl::save})
   */
  private void backfillDataPackageVersion(Resource resource, Consumer<Resource> resourceSaver) {
    if (resource.getDataPackageVersion() == null) {
      String identifier = resource.getDataPackageIdentifier();
      String installedVersion = schemaManager.getVersion(identifier);
      if (installedVersion != null) {
        resource.setDataPackageVersion(installedVersion);
        resourceSaver.accept(resource);
        LOG.warn("Backfilled dataPackageVersion={} for resource {} (schema {})",
            installedVersion, resource.getShortname(), identifier);
      } else {
        LOG.error("Could not backfill dataPackageVersion for resource {}: schema {} not installed",
            resource.getShortname(), identifier);
      }
    }
  }

  private boolean onlyFieldIndexPresent(DataPackageFieldMapping dpfm) {
    return dpfm.getIndex() != null && dpfm.getField() == null && StringUtils.isEmpty(dpfm.getDefaultValue());
  }

  private boolean emptyMapping(DataPackageFieldMapping dpfm) {
    return dpfm.getIndex() == null && dpfm.getField() == null && StringUtils.isEmpty(dpfm.getDefaultValue());
  }
}
