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

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.manage.ResourceVersioningService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ResourceVersioningServiceImpl implements ResourceVersioningService {

  private static final Logger LOG = LogManager.getLogger(ResourceVersioningServiceImpl.class);

  private final DataDir dataDir;

  public ResourceVersioningServiceImpl(DataDir dataDir) {
    this.dataDir = dataDir;
  }

  /**
   * Convert an integer version number to major_version.minor_version version number. Please note IPTs before v2.2 used
   * integer-based version numbers.
   * <p>
   * This is only used by Darwin Core Archive resources, Data Package resources (Frictionless Data) use
   * an integer-based version number.
   *
   * @param resource resource
   * @return converted version number, or null if no conversion happened
   */
  @SuppressWarnings("BigDecimalEquals")
  @Override
  public BigDecimal convertVersion(Resource resource) {
    if (resource.getMetadataVersion() != null) {
      BigDecimal version = resource.getMetadataVersion();
      // special conversion: 0 -> 1.0
      if (version.equals(BigDecimal.ZERO)) {
        return Constants.INITIAL_RESOURCE_VERSION;
      } else if (version.scale() == 0) {
        BigDecimal majorMinorVersion = version.setScale(1, RoundingMode.CEILING);
        LOG.debug("Converted version [{}] to [{}]", version.toPlainString(), majorMinorVersion.toPlainString());
        return majorMinorVersion;
      }
    }
    return null;
  }

  /**
   * Update a resource's version and rename its eml, rtf, and dwca versioned files to have the new version also.
   *
   * @param resource   resource to update
   * @param oldVersion old version number
   * @param newVersion new version number
   * @return resource whose version number and files' version numbers have been updated
   */
  @SuppressWarnings("BigDecimalEquals")
  @Override
  public Resource updateResourceVersion(Resource resource, BigDecimal oldVersion, BigDecimal newVersion) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(oldVersion);
    Objects.requireNonNull(newVersion);
    // proceed if old and new versions are not equal in both value and scale - comparison done using .equals
    if (!oldVersion.equals(newVersion)) {
      try {
        // rename e.g. eml-18.xml to eml-18.0.xml (if eml-18.xml exists)
        File oldEml = dataDir.resourceEmlFile(resource.getShortname(), oldVersion);
        File newEml = dataDir.resourceEmlFile(resource.getShortname(), newVersion);
        if (oldEml.exists() && !newEml.exists()) {
          FileUtils.moveFile(oldEml, newEml);
        }

        // rename e.g. zvv-18.rtf to zvv-18.0.rtf
        File oldRtf = dataDir.resourceRtfFile(resource.getShortname(), oldVersion);
        File newRtf = dataDir.resourceRtfFile(resource.getShortname(), newVersion);
        if (oldRtf.exists() && !newRtf.exists()) {
          FileUtils.moveFile(oldRtf, newRtf);
        }

        // rename e.g. dwca-18.zip to dwca-18.0.zip
        File oldDwca = dataDir.resourceDwcaFile(resource.getShortname(), oldVersion);
        File newDwca = dataDir.resourceDwcaFile(resource.getShortname(), newVersion);
        if (oldDwca.exists() && !newDwca.exists()) {
          FileUtils.moveFile(oldDwca, newDwca);
        }

        // if all renames were successful (didn't throw an exception), set new version
        resource.setMetadataVersion(newVersion);
      } catch (IOException e) {
        LOG.error("Failed to update version number for {}", resource.getShortname(), e);
        throw new InvalidConfigException(InvalidConfigException.TYPE.CONFIG_WRITE,
            "Failed to update version number for " + resource.getShortname() + ": " + e.getMessage());
      }
    }
    return resource;
  }

  /**
   * Rename a resource's dwca.zip to have the last published version, e.g. dwca-18.0.zip
   *
   * @param resource resource to update
   * @param version  last published version number
   */
  @Override
  public void renameDwcaToIncludeVersion(Resource resource, BigDecimal version) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(version);
    File unversionedDwca = dataDir.resourceDwcaFile(resource.getShortname());
    File versionedDwca = dataDir.resourceDwcaFile(resource.getShortname(), version);
    // proceed if resource has previously been published, and versioned dwca does not exist
    if (unversionedDwca.exists() && !versionedDwca.exists()) {
      try {
        FileUtils.moveFile(unversionedDwca, versionedDwca);
        LOG.debug("Renamed dwca.zip to {}", versionedDwca.getName());
      } catch (IOException e) {
        LOG.error("Failed to rename dwca.zip file name with version number for {}", resource.getShortname(), e);
        throw new InvalidConfigException(InvalidConfigException.TYPE.CONFIG_WRITE,
            "Failed to update version number for " + resource.getShortname() + ": " + e.getMessage());
      }
    }
  }

  /**
   * Construct VersionHistory for the last published version of a resource if the resource has been published but had no
   * VersionHistory. Please note IPTs before v2.2 had no list of VersionHistory.
   *
   * @param resource resource
   * @return VersionHistory, or null if no VersionHistory needed to be created.
   */
  @Override
  public VersionHistory constructVersionHistoryForLastPublishedVersion(Resource resource) {
    if (resource.isPublished() && resource.getVersionHistory().isEmpty()) {
      VersionHistory vh =
          new VersionHistory(resource.getMetadataVersion(), resource.getLastPublished(), resource.getStatus());
      vh.setRecordsPublished(resource.getRecordsPublished());
      return vh;
    }
    return null;
  }
}
