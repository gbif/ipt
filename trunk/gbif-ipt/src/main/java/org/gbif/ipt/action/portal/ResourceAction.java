package org.gbif.ipt.action.portal;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.metadata.eml.Eml;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.inject.Inject;

public class ResourceAction extends PortalBaseAction {

  private VocabulariesManager vocabManager;
  private List<Resource> resources;
  private Integer page = 1;

  @Inject
  public ResourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Return the size of the DwC-A file.
   */
  public String getDwcaFormattedSize() {
    return FileUtils.formatSize(resourceManager.getDwcaSize(resource), 0);
  }

  public Eml getEml() {
    return resource.getEml();
  }

  /**
   * Return the size of the EML file.
   */
  public String getEmlFormattedSize() {
    return FileUtils.formatSize(resourceManager.getEmlSize(resource), 0);
  }

  public Ipt getIpt() {
    if (registrationManager.getIpt() == null) {
      return new Ipt();
    }
    return registrationManager.getIpt();
  }

  public Map<String, String> getRanks() {
    return vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().getLanguage(), false);
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Return the RTF size file format.
   */
  public String getRtfFormattedSize() {
    return FileUtils.formatSize(resourceManager.getRtfSize(resource), 0);
  }

  public boolean isRtfFileExisting() {
    return resourceManager.isRtfExisting(resource.getShortname());
  }

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }
}
