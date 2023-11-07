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
package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;

public class ResourceUtils {

  protected static final Logger LOG = LogManager.getLogger(ResourceUtils.class);

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

  /*
   * Empty constructor.
   */
  private ResourceUtils() {
  }

  /**
   * Reconstruct published version, using version's Eml file, version history, etc.
   *
   * @param version version to assign to reconstructed resource
   * @param shortname shortname to assign to reconstructed resource
   * @param coreTypeOrPackageType coreType or packageType
   * @param schemaIdentifier data package schema identifier (optional)
   * @param doi DOI to assign to reconstructed resource
   * @param organisation organisation to assign to reconstructed resource
   * @param versionHistory VersionHistory corresponding to resource version being reconstructed
   * @param versionMetadataFile eml file ord metadata file corresponding to version of resource being reconstructed
   * @param key GBIF UUID to assign to reconstructed resource
   *
   * @return published version reconstructed
   */
  public static Resource reconstructVersion(@NotNull BigDecimal version, @NotNull String shortname, @NotNull String coreTypeOrPackageType,
    @Nullable String schemaIdentifier, @Nullable DOI doi, @Nullable Organisation organisation,
    @Nullable VersionHistory versionHistory, @Nullable File versionMetadataFile, @Nullable UUID key) {
    Objects.requireNonNull(version);
    Objects.requireNonNull(shortname);

    boolean isDataPackageResource = schemaIdentifier != null;

    if (organisation == null && !isDataPackageResource) {
      throw new IllegalArgumentException(
              "Failed to reconstruct resource version: organisation is null");
    }

    if (versionHistory == null) {
      throw new IllegalArgumentException(
              "Failed to reconstruct resource version: version history is null");
    }

    if (versionMetadataFile == null) {
      throw new IllegalArgumentException(
              "Failed to reconstruct resource version: version eml file is null");
    }

    // initiate new version, and set properties
    Resource resource = new Resource();
    resource.setCoreType(coreTypeOrPackageType);
    resource.setSchemaIdentifier(schemaIdentifier);
    resource.setShortname(shortname);
    resource.setMetadataVersion(version);
    resource.setDoi(doi);
    resource.setOrganisation(organisation);
    resource.setKey(key);
    resource.setStatus(versionHistory.getPublicationStatus());
    resource.setIdentifierStatus(versionHistory.getStatus());
    resource.setRecordsPublished(versionHistory.getRecordsPublished());
    resource.setLastPublished(versionHistory.getReleased());
    resource.setRecordsByExtension(versionHistory.getRecordsByExtension());

    if (versionMetadataFile.exists()) {
      if (isDataPackageResource) {
        if (COL_DP.equals(coreTypeOrPackageType)) {
          ColMetadata metadata;
          try {
            metadata = yamlMapper.readValue(versionMetadataFile, ColMetadata.class);
            resource.setDataPackageMetadata(metadata);
          } catch (IOException e) {
            LOG.error("Failed to produce ColDP metadata for the resource {}", shortname);
            LOG.error(e);
            throw new RuntimeException(e);
          }
        } else {
          DataPackageMetadata metadata;
          try {
            metadata = jsonMapper.readValue(versionMetadataFile, getDataPackageClass(schemaIdentifier));
            resource.setDataPackageMetadata(metadata);
          } catch (IOException e) {
            LOG.error("Failed to produce metadata for the data package resource {}", shortname);
            LOG.error(e);
            throw new RuntimeException(e);
          }
        }
      } else {
        Eml eml = EmlUtils.loadWithLocale(versionMetadataFile, Locale.US);
        resource.setEml(eml);
      }
    } else {
      LOG.error("Failed to reconstruct resource: {} not found!", versionMetadataFile.getAbsolutePath());
      throw new IllegalArgumentException(
        "Failed to reconstruct resource: " + versionMetadataFile.getAbsolutePath() + " not found!");
    }
    return resource;
  }

  private static Class<? extends DataPackageMetadata> getDataPackageClass(String schemaIdentifier) {
    if (schemaIdentifier.contains(CAMTRAP_DP)) {
      return CamtrapMetadata.class;
    } else {
      return FrictionlessColMetadata.class;
    }
  }

  /**
   * Assert that version b is greater than version a. Comparison must take into account major_version.minor_version
   * scheme, e.g. version 2.0 is greater than version 1.100, and version 1.100 is greater than 1.99.
   *
   * @param b version
   * @param a version
   *
   * @return true if version a is greater than version b, false otherwise
   */
  public static boolean assertVersionOrder(BigDecimal b, BigDecimal a) {
    if (a != null && b != null) {
      // comparison on major_version
      if (b.intValue() > a.intValue()) {
        return true;
      }
      // comparison on minor_version, if major_version was the same
      else if (b.intValue() == a.intValue()) {
        int scaleB = b.scale(); // 0.10 has a scale of 2
        BigDecimal scaledB = b.scaleByPowerOfTen(scaleB); // 0.10 * 10(2) = 10

        int scaleA = a.scale(); // 0.9 has a scale of 1
        BigDecimal scaledA = a.scaleByPowerOfTen(scaleA); // 0.9 * 10(1) = 9

        return scaledB.compareTo(scaledA) > 0;
      }
    }
    return false;
  }
}
