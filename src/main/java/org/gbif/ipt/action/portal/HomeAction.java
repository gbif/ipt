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
import org.gbif.ipt.utils.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
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

  /**
   * Ensure resource table shows properties coming from last published versions of resources where applicable (e.g.
   * resource title should be the title assigned to last published version, not the title of current
   * (unpublished) version that may still be under editing.
   */
  @Override
  public void prepare() {
    super.prepare();
    resources = Lists.newArrayList();

    for (Resource resource : resourceManager.listPublishedPublicVersions()) {
      // reconstruct the last published public version
      BigDecimal v = resource.getLastPublishedVersionsVersion();
      String shortname = resource.getShortname();
      File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, v);
      // try/catch block flags resources missing mandatory metadata (published using IPT prior to v2.2)
      try {
        Resource publishedPublicVersion = ResourceUtils
          .reconstructVersion(v, resource.getShortname(), resource.getAssignedDoi(), resource.getOrganisation(),
            resource.findVersionHistory(v), versionEmlFile, resource.getKey());

        // set properties only existing on current (unpublished) version
        Resource current = resourceManager.get(shortname);
        publishedPublicVersion.setModified(current.getModified());
        publishedPublicVersion.setNextPublished(current.getNextPublished());
        publishedPublicVersion.setCoreType(current.getCoreType());
        publishedPublicVersion.setSubtype(current.getSubtype());
        // was last published version later registered but never republished? Fix for issue #1319
        if (!publishedPublicVersion.isRegistered() && current.isRegistered() && current.getOrganisation() != null) {
          publishedPublicVersion.setStatus(PublicationStatus.REGISTERED);
          publishedPublicVersion.setOrganisation(current.getOrganisation());
        }
        resources.add(publishedPublicVersion);
      } catch (IllegalArgumentException e) {
        // only expected to happen for extremely out-of-date resources published using IPT prior to v.2.2
        addActionWarning(resource.getTitleAndShortname() + " failed to load. To fix this problem, try publishing this resource again.");
      }
    }

    // sort alphabetically (A to Z)
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
   * A list of last published versions for all public or registered resources.
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
