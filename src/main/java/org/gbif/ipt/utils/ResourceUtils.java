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
import org.gbif.ipt.model.MetadataFiles;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import jakarta.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DWC_DP;

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
   * Reconstruct published version, using version's metadata file, version history, etc.
   *
   * @param version               version to assign to reconstructed resource
   * @param shortname             shortname to assign to reconstructed resource
   * @param coreTypeOrPackageType coreType or packageType
   * @param dataPackageIdentifier data package identifier (optional)
   * @param doi                   DOI to assign to reconstructed resource
   * @param organisation          organisation to assign to reconstructed resource
   * @param versionHistory        VersionHistory corresponding to resource version being reconstructed
   * @param versionMetadataFile   eml file or metadata file corresponding to version of resource being reconstructed
   * @param key                   GBIF UUID to assign to reconstructed resource
   * @return published version reconstructed
   */
  public static Resource reconstructVersion(@NotNull BigDecimal version, @NotNull String shortname, @NotNull String coreTypeOrPackageType,
                                            @Nullable String dataPackageIdentifier, @Nullable DOI doi, @Nullable Organisation organisation,
                                            @Nullable VersionHistory versionHistory, @Nullable File versionMetadataFile, @Nullable UUID key) {
    return reconstructVersion(version, shortname, coreTypeOrPackageType, dataPackageIdentifier, doi, organisation,
        versionHistory, MetadataFiles.fromSingleFile(versionMetadataFile), key);
  }

  /**
   * Reconstruct published version, using version's metadata files, version history, etc.
   *
   * @param version               version to assign to reconstructed resource
   * @param shortname             shortname to assign to reconstructed resource
   * @param coreTypeOrPackageType coreType or packageType
   * @param dataPackageIdentifier data package identifier (optional)
   * @param doi                   DOI to assign to reconstructed resource
   * @param organisation          organisation to assign to reconstructed resource
   * @param versionHistory        VersionHistory corresponding to resource version being reconstructed
   * @param versionMetadataFiles  any metadata files (EML, datapackage.json etc.) corresponding to version of resource being reconstructed
   * @param key                   GBIF UUID to assign to reconstructed resource
   * @return published version reconstructed
   */
  public static Resource reconstructVersion(@NotNull BigDecimal version, @NotNull String shortname, @NotNull String coreTypeOrPackageType,
                                            @Nullable String dataPackageIdentifier, @Nullable DOI doi, @Nullable Organisation organisation,
                                            @Nullable VersionHistory versionHistory, MetadataFiles versionMetadataFiles, @Nullable UUID key) {
    Objects.requireNonNull(version);
    Objects.requireNonNull(shortname);

    boolean isDataPackageResource = dataPackageIdentifier != null;
    boolean isDwcDp = DWC_DP.equals(coreTypeOrPackageType);
    boolean isColDP = COL_DP.equals(coreTypeOrPackageType);

    if (organisation == null && !isDataPackageResource) {
      throw new IllegalArgumentException(
          "Failed to reconstruct resource version: organisation is null");
    }

    if (versionHistory == null) {
      throw new IllegalArgumentException(
          "Failed to reconstruct resource version: version history is null");
    }

    if (versionMetadataFiles.isEmpty()) {
      throw new IllegalArgumentException(
          "Failed to reconstruct resource version: versioned metadata files are not provided");
    }

    Resource resource = newResourceShell(version, shortname, coreTypeOrPackageType,
        dataPackageIdentifier, doi, organisation, versionHistory, key);

    if (!isDataPackageResource) {
      reconstructDwca(resource, versionMetadataFiles, shortname);
    } else if (isDwcDp) {
      reconstructDwcDp(resource, versionMetadataFiles, shortname);
    } else if (isColDP) {
      reconstructColDp(resource, versionMetadataFiles, shortname);
    } else {
      reconstructDatapackage(resource, versionMetadataFiles, dataPackageIdentifier, shortname);
    }

    return resource;
  }

  // Plain DwC-A resource: EML only, no fallback.
  private static void reconstructDwca(Resource resource, MetadataFiles versionMetadataFiles, String shortname) {
    File emlFile = versionMetadataFiles.getEml();
    requireFile(emlFile, shortname);
    resource.setEml(EmlUtils.loadWithLocale(emlFile, Locale.US));
  }

  // DwC-DP: prefer EML, fall back to datapackage.json if EML is missing.
  private static void reconstructDwcDp(Resource resource, MetadataFiles versionMetadataFiles, String shortname) {
    File emlFile = versionMetadataFiles.getEml();
    if (emlFile != null && emlFile.exists()) {
      resource.setEml(EmlUtils.loadWithLocale(emlFile, Locale.US));
      return;
    }

    File datapackageFile = versionMetadataFiles.getDatapackage();
    if (datapackageFile == null || !datapackageFile.exists()) {
      LOG.error("Failed to reconstruct DwC-DP resource {}: neither EML {} nor datapackage.json {} found!",
          shortname, emlFile, datapackageFile);
      throw new IllegalArgumentException(
          "Failed to reconstruct resource: no valid metadata file found for " + shortname);
    }

    LOG.warn("DwC-DP resource {} does not have EML file {}, falling back to datapackage.json: {}",
        shortname, emlFile, datapackageFile);
    resource.setDataPackageMetadata(
        readMetadata(datapackageFile, jsonMapper, getDataPackageClass(DWC_DP), shortname));
  }

  // COL-DP: datapackage.yaml/json parsed with the YAML mapper into ColMetadata.
  private static void reconstructColDp(Resource resource, MetadataFiles versionMetadataFiles, String shortname) {
    File datapackageFile = versionMetadataFiles.getDatapackage();
    requireFile(datapackageFile, shortname);
    resource.setDataPackageMetadata(
        readMetadata(datapackageFile, yamlMapper, ColMetadata.class, shortname));
  }

  // Generic data package resource: datapackage.json parsed with the JSON mapper.
  private static void reconstructDatapackage(Resource resource, MetadataFiles versionMetadataFiles,
                                             String dataPackageIdentifier, String shortname) {
    File datapackageFile = versionMetadataFiles.getDatapackage();
    requireFile(datapackageFile, shortname);
    resource.setDataPackageMetadata(
        readMetadata(datapackageFile, jsonMapper, getDataPackageClass(dataPackageIdentifier), shortname));
  }

  private static void requireFile(@Nullable File file, String shortname) {
    if (file == null) {
      LOG.error("Failed to reconstruct resource {}: metadata was not provided!", shortname);
      throw new IllegalArgumentException("Failed to reconstruct resource: metadata not provided!");
    }

    if (!file.exists()) {
      String path = file.getAbsolutePath();
      LOG.error("Failed to reconstruct resource {}: {} not found!", shortname, path);
      throw new IllegalArgumentException("Failed to reconstruct resource: " + path + " not found!");
    }
  }

  private static Resource newResourceShell(BigDecimal version, String shortname, String coreTypeOrPackageType,
                                           String dataPackageIdentifier, DOI doi, Organisation organisation,
                                           VersionHistory versionHistory, UUID key) {
    Resource resource = new Resource();
    resource.setCoreType(coreTypeOrPackageType);
    resource.setDataPackageIdentifier(dataPackageIdentifier);
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
    return resource;
  }

  private static <T> T readMetadata(File file, ObjectMapper mapper, Class<T> clazz, String shortname) {
    try {
      return mapper.readValue(file, clazz);
    } catch (IOException e) {
      LOG.error("Failed to produce metadata for the resource {}", shortname);
      LOG.error(e);
      throw new RuntimeException(e);
    }
  }

  private static Class<? extends DataPackageMetadata> getDataPackageClass(String dataPackageIdentifier) {
    if (dataPackageIdentifier.contains(CAMTRAP_DP)) {
      return CamtrapMetadata.class;
    } else if (dataPackageIdentifier.contains(DWC_DP)) {
      return FrictionlessMetadata.class;
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
