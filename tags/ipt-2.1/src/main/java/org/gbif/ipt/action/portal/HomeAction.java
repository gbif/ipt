package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private final ResourceManager resourceManager;
  private final VocabulariesManager vocabManager;

  private List<Resource> resources;
  private Map<String, String> types;
  private Map<String, String> datasetSubtypes;


  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    resources = resourceManager.list(PublicationStatus.PUBLIC);
    resources.addAll(resourceManager.list(PublicationStatus.REGISTERED));
    // sort alphabetically
    Collections.sort(resources);

    // Dataset core type list, derived from XML vocabulary
    types = new LinkedHashMap<String, String>();
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);

    // Dataset Subtypes list, derived from XML vocabulary
    datasetSubtypes = new LinkedHashMap<String, String>();
    datasetSubtypes.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, getLocaleLanguage(), false));
    datasetSubtypes = MapUtils.getMapWithLowercaseKeys(datasetSubtypes);
  }

  /**
   * A list of all public or registered resources.
   * 
   * @return a list of resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * A map of dataset types keys to internationalized values.
   * 
   * @return map of dataset subtypes
   */
  public Map<String, String> getTypes() {
    return types;
  }

  /**
   * A map of dataset subtypes keys to internationalized values.
   * 
   * @return map of dataset subtypes
   */
  public Map<String, String> getDatasetSubtypes() {
    return datasetSubtypes;
  }
}
