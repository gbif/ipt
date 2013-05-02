package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private List<Resource> resources = new ArrayList<Resource>();

  private ResourceManager resourceManager;
  private VocabulariesManager vocabManager;
  private boolean registrationAllowed = false;
  private Map<String, String> types;

  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() {
    resources = resourceManager.list(getCurrentUser());

    // Dataset core type list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    types = new LinkedHashMap<String, String>();
    types.put("", getText("manage.resource.create.coreType.selection"));
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);

    return SUCCESS;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public boolean isRegistrationAllowed() {
    return registrationAllowed;
  }

  /**
   * Get map of resource types to populate resource type selection.
   *
   * @return map of resource types
   */
  public Map<String, String> getTypes() {
    return types;
  }

  /**
   * method for dealing with the action for a locked resource.
   * Does nothing but the regular home plus an error message
   */
  public String locked() {
    addActionError(getText("manage.home.resource.locked"));
    return execute();
  }

}
