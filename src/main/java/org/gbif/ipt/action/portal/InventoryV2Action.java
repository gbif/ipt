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
package org.gbif.ipt.action.portal;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.json.annotations.JSON;

import com.opensymphony.xwork2.ActionSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Action serialized into JSON - used to get a simple JSON inventory of registered resources.
 */
public class InventoryV2Action extends ActionSupport {

  private static final long serialVersionUID = 2207415310987539257L;

  private static final Logger LOG = LogManager.getLogger(InventoryV2Action.class);

  private final AppConfig cfg;
  private final ResourceManager resourceManager;
  @Setter
  private List<DatasetItemV2> inventory = new ArrayList<>();

  @Inject
  public InventoryV2Action(
      AppConfig cfg,
      ResourceManager resourceManager) {
    this.cfg = cfg;
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    List<Resource> resources;

    resources = resourceManager.list(PublicationStatus.PUBLIC);

    if (!resources.isEmpty()) {
      populateInventory(resources);
    }
    return SUCCESS;
  }

  @JSON(name = "resources")
  public List<DatasetItemV2> getInventory() {
    return inventory;
  }

  /**
   * Class representing dataset item returned in inventory response serialized into JSON.
   */
  public static class DatasetItemV2 {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String gbifKey;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private ResourceFormatType format;
    @Getter
    @Setter
    private BigDecimal version;
    @Setter
    private Date lastPublished;
    @Getter
    @Setter
    private int records;
    @Getter
    @Setter
    private InventoryArchiveInfo archive;
    @Getter
    @Setter
    private InventoryMetadataInfo metadata;
    @Getter
    @Setter
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return the date the dataset was last published
     */
    @JSON(format = "yyyy-MM-dd")
    public Date getLastPublished() {
      return lastPublished;
    }
  }

  public enum ResourceFormatType {
    DWCA, METADATA, CAMTRAP_DP, COLDP
  }

  public enum ArchiveFormatType {
    DWCA, CAMTRAP_DP, COLDP
  }

  public enum MetadataFormatType {
    EML, FRICTIONLESS
  }

  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InventoryArchiveInfo {
    private ArchiveFormatType type;
    private String url;
  }

  @Setter
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InventoryMetadataInfo {
    private MetadataFormatType type;
    private String url;
  }

  /**
   * Populate and set the list/inventory of DatasetItem from a list of registered resources.
   * Note a DatasetItem represents the last published version of a resource.
   *
   * @param resources list of registered resources
   */
  public void populateInventory(List<Resource> resources) {
    List<DatasetItemV2> items = new ArrayList<>();
    for (Resource r : resources) {
      // reconstruct the last published version of the resource
      BigDecimal version = r.getLastPublishedVersionsVersion();

      // skip resources that has never been published
      if (version == null) {
        continue;
      }

      try {
        DatasetItemV2 item = r.isDataPackage() ? convertToFrictionlessDatasetItem(r) : convertToDwcaDatasetItem(r);
        items.add(item);
      } catch (Exception e) {
        LOG.error("Failed to populate inventory. Resource {}, type {}", r.getShortname(), r.getCoreType());
      }
    }
    setInventory(items);
  }

  private DatasetItemV2 convertToDwcaDatasetItem(Resource r) {
    DatasetItemV2 item = new DatasetItemV2();

    BigDecimal version = r.getLastPublishedVersionsVersion();
    String shortname = r.getShortname();
    VersionHistory versionHistory = r.getLastPublishedVersion();
    DOI doi = Optional.ofNullable(versionHistory).map(VersionHistory::getDoi).orElse(null);
    File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, version);
    UUID gbifKey = r.getKey();

    Resource lastPublished = ResourceUtils.reconstructVersion(
        version, shortname, r.getCoreType(), r.getDataPackageIdentifier(), doi, r.getOrganisation(),
        versionHistory, versionEmlFile, gbifKey
    );

    // populate DatasetItem representing last published version of the registered dataset
    item.setId(shortname);
    item.setTitle(StringUtils.trimToNull(lastPublished.getTitle()));
    item.setRecords(lastPublished.getRecordsPublished());
    item.setLastPublished(lastPublished.getLastPublished());
    Optional.of(lastPublished)
        .map(Resource::getKey)
        .map(UUID::toString)
        .ifPresent(item::setGbifKey);

    InventoryArchiveInfo archiveInfo = new InventoryArchiveInfo();
    if (!ResourceFormatType.METADATA.toString().equalsIgnoreCase(r.getCoreType())) {
      archiveInfo.setType(ArchiveFormatType.DWCA);
      archiveInfo.setUrl(cfg.getResourceArchiveUrl(shortname));
      item.setArchive(archiveInfo);
    }

    InventoryMetadataInfo metadata = new InventoryMetadataInfo();
    metadata.setType(MetadataFormatType.EML);
    metadata.setUrl(cfg.getResourceEmlUrl(shortname));
    item.setMetadata(metadata);

    item.setVersion(version);

    if (ResourceFormatType.METADATA.toString().equalsIgnoreCase(r.getCoreType())) {
      item.setFormat(ResourceFormatType.METADATA);
    } else if (!lastPublished.isDataPackage()) {
      item.setFormat(ResourceFormatType.DWCA);

      Map<String, Object> metrics = new HashMap<>();
      metrics.put("recordsByExtension", lastPublished.getRecordsByExtension());
      metrics.put("core", r.getCoreType());
      item.setAdditionalProperties(metrics);
    }

    return item;
  }

  private DatasetItemV2 convertToFrictionlessDatasetItem(Resource r) {
    DatasetItemV2 item = new DatasetItemV2();

    BigDecimal version = r.getLastPublishedVersionsVersion();
    String shortname = r.getShortname();
    VersionHistory versionHistory = r.getLastPublishedVersion();
    DOI doi = Optional.ofNullable(versionHistory).map(VersionHistory::getDoi).orElse(null);
    File versionedDatapackageFile = cfg.getDataDir().resourceDatapackageMetadataFile(shortname, r.getCoreType(), version);
    UUID gbifKey = r.getKey();

    Resource lastPublished = ResourceUtils.reconstructVersion(
        version, shortname, r.getCoreType(), r.getDataPackageIdentifier(), doi, r.getOrganisation(),
        versionHistory, versionedDatapackageFile, gbifKey
    );

    // populate DatasetItem representing last published version of the registered dataset
    item.setId(shortname);
    item.setTitle(StringUtils.trimToNull(lastPublished.getTitle()));
    item.setRecords(lastPublished.getRecordsPublished());
    item.setLastPublished(lastPublished.getLastPublished());
    Optional.of(lastPublished)
        .map(Resource::getKey)
        .map(UUID::toString)
        .ifPresent(item::setGbifKey);

    InventoryArchiveInfo archiveInfo = new InventoryArchiveInfo();
    if (Constants.CAMTRAP_DP.equalsIgnoreCase(r.getCoreType())) {
      archiveInfo.setType(ArchiveFormatType.CAMTRAP_DP);
      archiveInfo.setUrl(cfg.getResourceArchiveUrl(shortname));
      item.setArchive(archiveInfo);
      item.setFormat(ResourceFormatType.CAMTRAP_DP);
    } else if (Constants.COL_DP.equalsIgnoreCase(r.getCoreType())) {
      archiveInfo.setType(ArchiveFormatType.COLDP);
      archiveInfo.setUrl(cfg.getResourceArchiveUrl(shortname));
      item.setArchive(archiveInfo);
      item.setFormat(ResourceFormatType.COLDP);
    } else {
      LOG.warn("Unsupported archive type {}", r.getCoreType());
    }

    InventoryMetadataInfo metadata = new InventoryMetadataInfo();
    metadata.setType(MetadataFormatType.FRICTIONLESS);
    metadata.setUrl(cfg.getResourceDataPackageMetadataUrl(shortname));
    item.setMetadata(metadata);

    item.setVersion(version);

    Map<String, Object> metrics = new HashMap<>();
    metrics.put("recordsByTable", lastPublished.getRecordsByExtension());
    item.setAdditionalProperties(metrics);

    return item;
  }
}
