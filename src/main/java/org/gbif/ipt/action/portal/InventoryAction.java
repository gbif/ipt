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

import lombok.Getter;
import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.ResourceUtils;

import java.io.File;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.json.annotations.JSON;
import org.apache.struts2.ActionSupport;

/**
 * Action serialized into JSON - used to get a simple JSON inventory of registered resources.
 */
public class InventoryAction extends ActionSupport {

  @Serial
  private static final long serialVersionUID = -549820308428762423L;

  private static final Logger LOG = LogManager.getLogger(InventoryAction.class);

  private final AppConfig cfg;
  private final ResourceManager resourceManager;
  private List<DatasetItem> inventory = new ArrayList<>();

  @Inject
  public InventoryAction(
      AppConfig cfg,
      ResourceManager resourceManager) {
    this.cfg = cfg;
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    // load all registered resources, and populate inventory
    List<Resource> registered = resourceManager.list(PublicationStatus.REGISTERED);
    if (!registered.isEmpty()) {
      populateInventory(registered);
    }
    return SUCCESS;
  }

  @JSON(name = "registeredResources")
  public List<DatasetItem> getInventory() {
    return inventory;
  }

  public void setInventory(List<DatasetItem> inventory) {
    this.inventory = inventory;
  }

  /**
   * Class representing dataset item returned in inventory response serialized into JSON.
   */
  public static class DatasetItem {

    /**
     * -- GETTER --
     *
     * @return the dataset title
     */
    @Getter
    private String title;
    /**
     * -- GETTER --
     *
     * @return the dataset type
     */
    @Getter
    private String type;
    /**
     * -- GETTER --
     *
     * @return the dataset record count
     */
    @Getter
    private int records;
    private Date lastPublished;
    /**
     * -- GETTER --
     *
     * @return the dataset GBIF key (UUID)
     */
    @Getter
    private String gbifKey;
    /**
     * -- GETTER --
     *
     * @return the endpoint URL to the dataset's last published Eml file
     */
    @Getter
    private String eml;
    /**
     * -- GETTER --
     *
     * @return the endpoint URL to the dataset's last published DwC-A file
     */
    @Getter
    private String dwca;
    /**
     * -- GETTER --
     *  Get last published version of resource.
     *
     * @return resource version
     */
    @Getter
    private BigDecimal version;
    /**
     * -- GETTER --
     *
     * @return map containing record counts (map value) by extension (map key) used in the last published DwC-A file,
     * or an empty map
     */
    @Getter
    private Map<String, Integer> recordsByExtension = new HashMap<>();

    public void setTitle(String title) {
      this.title = title;
    }

    public void setType(String type) {
      this.type = type;
    }

    public void setRecords(int records) {
      this.records = records;
    }

    /**
     * @return the date the dataset was last published
     */
    @JSON(format = "yyyy-MM-dd")
    public Date getLastPublished() {
      return lastPublished;
    }

    public void setLastPublished(Date lastPublished) {
      this.lastPublished = lastPublished;
    }

    public void setGbifKey(String gbifKey) {
      this.gbifKey = gbifKey;
    }

    public void setEml(String eml) {
      this.eml = eml;
    }

    public void setDwca(String dwca) {
      this.dwca = dwca;
    }

    /**
     * @param recordsByExtension map of record counts (map value) by extension rowType (map key)
     */
    public void setRecordsByExtension(Map<String, Integer> recordsByExtension) {
      if (recordsByExtension != null) {
        this.recordsByExtension = Collections.unmodifiableMap(recordsByExtension);
      }
    }

    /**
     * Set the last published version of the resource.
     *
     * @param version
     */
    public void setVersion(BigDecimal version) {
      this.version = version;
    }
  }

  /**
   * Populate and set the list/inventory of DatasetItem from a list of registered resources.
   * Note a DatasetItem represents the last published version of a resource.
   *
   * @param resources list of registered resources
   */
  public void populateInventory(List<Resource> resources) {
    List<DatasetItem> items = new ArrayList<>();
    for (Resource r : resources) {
      // reconstruct the last published version of the resource
      BigDecimal version = r.getLastPublishedVersionsVersion();

      // skip resources that has never been published
      // skip data packages - use inventory v2
      if (version == null || r.isDataPackage()) {
        continue;
      }

      try {
        DatasetItem item = convertToDatasetItem(r);
        items.add(item);
      } catch (Exception e) {
        LOG.error("Failed to populate inventory (v1). Resource {}, type {}. Error: {}",
            r.getShortname(), r.getCoreType(), e.getMessage());
      }
    }
    setInventory(items);
  }

  private DatasetItem convertToDatasetItem(Resource r) {
    DatasetItem item = new DatasetItem();
    BigDecimal version = r.getLastPublishedVersionsVersion();
    String shortname = r.getShortname();
    VersionHistory versionHistory = r.getLastPublishedVersion();
    // TODO: possible NPE
    DOI doi = versionHistory.getDoi();
    File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, version);
    UUID gbifKey = r.getKey();
    Resource lastPublished = ResourceUtils.reconstructVersion(
        version, shortname, r.getCoreType(), r.getDataPackageIdentifier(), doi, r.getOrganisation(),
        versionHistory, versionEmlFile, gbifKey
    );

    // populate DatasetItem representing last published version of the registered dataset
    item.setTitle(StringUtils.trimToNull(lastPublished.getTitle()));
    item.setRecords(lastPublished.getRecordsPublished());
    item.setLastPublished(lastPublished.getLastPublished());
    item.setGbifKey(lastPublished.getKey().toString());
    item.setRecordsByExtension(lastPublished.getRecordsByExtension());
    item.setEml(cfg.getResourceEmlUrl(shortname));
    item.setDwca(cfg.getResourceArchiveUrl(shortname));
    item.setVersion(version);
    item.setType(StringUtils.trimToNull(r.getCoreType()));

    return item;
  }
}
