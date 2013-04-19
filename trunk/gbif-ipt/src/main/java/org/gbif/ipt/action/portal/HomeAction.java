package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private List<Resource> resources;
  private ResourceManager resourceManager;

  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
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
  }

  public List<Resource> getResources() {
    return resources;
  }
}
