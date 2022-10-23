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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.json.annotations.JSON;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action serialized into JSON - used to get a simple JSON inventory of registered resources.
 */
public class InventoryAction extends ActionSupport {

  private final AppConfig cfg;
  private final ResourceManager resourceManager;
  private List<DatasetItem> inventory = new ArrayList<>();

  @Inject
  public InventoryAction(AppConfig cfg, ResourceManager resourceManager) {
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

    private String title;
    private String type;
    private int records;
    private Date lastPublished;
    private String gbifKey;
    private String eml;
    private String dwca;
    private BigDecimal version;
    private Map<String, Integer> recordsByExtension = new HashMap<>();

    /**
     * @return the dataset title
     */
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    /**
     * @return the dataset type
     */
    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    /**
     * @return the dataset record count
     */
    public int getRecords() {
      return records;
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

    /**
     * @return the dataset GBIF key (UUID)
     */
    public String getGbifKey() {
      return gbifKey;
    }

    public void setGbifKey(String gbifKey) {
      this.gbifKey = gbifKey;
    }

    /**
     * @return the endpoint URL to the dataset's last published Eml file
     */
    public String getEml() {
      return eml;
    }

    public void setEml(String eml) {
      this.eml = eml;
    }

    /**
     * @return the endpoint URL to the dataset's last published DwC-A file
     */
    public String getDwca() {
      return dwca;
    }

    public void setDwca(String dwca) {
      this.dwca = dwca;
    }

    /**
     * @return map containing record counts (map value) by extension (map key) used in the last published DwC-A file,
     * or an empty map
     */
    public Map getRecordsByExtension() {
      return recordsByExtension;
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
     * Get last published version of resource.
     *
     * @return resource version
     */
    @NotNull
    public BigDecimal getVersion() {
      return version;
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
      DatasetItem item = new DatasetItem();

      // reconstruct the last published version of the resource
      BigDecimal version = r.getLastPublishedVersionsVersion();
      String shortname = r.getShortname();
      VersionHistory versionHistory = r.getLastPublishedVersion();
      DOI doi = versionHistory.getDoi();
      File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, version);
      UUID gbifKey = r.getKey();
      Resource lastPublished = ResourceUtils.reconstructVersion(
          version, shortname, r.getCoreType(), r.getSchemaIdentifier(), doi, r.getOrganisation(),
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
      items.add(item);
    }
    setInventory(items);
  }
}
