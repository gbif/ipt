package org.gbif.ipt.action.portal;


import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.json.annotations.JSON;

/**
 * Action serialized into JSON - used to get a simple JSON inventory of registered resources.
 */
public class InventoryAction extends ActionSupport {

  private final AppConfig cfg;
  private final ResourceManager resourceManager;
  private List<DatasetItem> inventory;

  @Inject
  public InventoryAction(AppConfig cfg, ResourceManager resourceManager) {
    this.cfg = cfg;
    this.resourceManager = resourceManager;
  }

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
  public class DatasetItem {

    private String title;
    private String type;
    private int records;
    private Date lastPublished;
    private String gbifKey;
    private String eml;
    private String dwca;
    private List<String> extensions;

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
     * @return the extension rowTypes of the extensions used in the last published DwC-A file, or an empty list
     */
    public List<String> getExtensions() {
      if (extensions == null) {
        extensions = Lists.newArrayList();
      }
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions;
    }
  }

  /**
   * Populate and set the list/inventory of DatasetItem. Each DatasetItem is populated from a registered Resource.
   *
   * @param resources registered resources list
   */
  public void populateInventory(List<Resource> resources) {
    List<DatasetItem> items = Lists.newArrayList();
    for (Resource r : resources) {
      DatasetItem item = new DatasetItem();
      item.setTitle(Strings.nullToEmpty(r.getTitle()));
      item.setType(Strings.nullToEmpty(r.getCoreType()));
      item.setRecords(r.getRecordsPublished());
      item.setLastPublished(r.getLastPublished());
      item.setGbifKey(r.getKey().toString());
      item.setEml(cfg.getResourceEmlUrl(r.getShortname()));
      item.setDwca(cfg.getResourceArchiveUrl(r.getShortname()));
      // populate list of extension rowTypes (excluding core extensions)
      for (Extension extension: r.getMappedExtensions()) {
        if (!extension.isCore()) {
          item.getExtensions().add(extension.getRowType());
        }
      }
      items.add(item);
    }
    setInventory(items);
  }
}
