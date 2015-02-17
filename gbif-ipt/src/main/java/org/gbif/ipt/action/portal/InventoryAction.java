package org.gbif.ipt.action.portal;


import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import java.text.SimpleDateFormat;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.json.annotations.JSON;

/**
 * Action serialized into JSON - used to get a simple JSON inventory of registered resources.
 */
public class InventoryAction extends ActionSupport {

  // format used to format/parse a Date field
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final String TITLE_KEY = "title";
  private static final String TYPE_KEY = "type";
  private static final String COUNT_KEY = "records";
  private static final String LAST_PUBLISHED_KEY = "lastPublished";
  private static final String GBIF_UUID_KEY = "gbifKey";
  private static final String EML_KEY = "eml";
  private static final String DWCA_KEY = "dwca";

  private final AppConfig cfg;
  private final ResourceManager resourceManager;
  private List<Resource> registered;

  @Inject
  public InventoryAction(AppConfig cfg, ResourceManager resourceManager) {
    this.cfg = cfg;
    this.resourceManager = resourceManager;
  }

  public String execute() {
    // load all registered resources
    registered = resourceManager.list(PublicationStatus.REGISTERED);
    return SUCCESS;
  }

  /**
   * @return JSON array as string representing all registered resources, null values converted to empty string
   */
  @JSON(name = "registeredResources")
  public String getResources() {
    JsonArray jsonArray = new JsonArray();
    // convert each registered resource into JsonObject with select properties
    for (Resource r : registered) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(TITLE_KEY, Strings.nullToEmpty(r.getTitle()));
      jsonObject.addProperty(TYPE_KEY, Strings.nullToEmpty(r.getCoreType()));
      jsonObject.addProperty(COUNT_KEY, String.valueOf(r.getRecordsPublished()));
      jsonObject.addProperty(LAST_PUBLISHED_KEY, Strings.nullToEmpty(DATE_FORMAT.format(r.getLastPublished())));
      jsonObject.addProperty(GBIF_UUID_KEY, (r.getKey() == null) ? "" : r.getKey().toString());
      jsonObject.addProperty(EML_KEY, cfg.getResourceEmlUrl(r.getShortname()));
      jsonObject.addProperty(DWCA_KEY, cfg.getResourceArchiveUrl(r.getShortname()));
      jsonArray.add(jsonObject);
    }
    return jsonArray.toString();
  }
}
