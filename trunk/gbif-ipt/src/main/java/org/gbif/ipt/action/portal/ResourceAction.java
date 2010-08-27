package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.metadata.eml.Eml;

import com.google.inject.Inject;

import java.util.List;

public class ResourceAction extends BaseAction {
  @Inject
  private ResourceManager resourceManager;
  @Inject
  private RegistrationManager registrationManager;
  private Resource resource;
  private Eml eml;
  private List<Resource> resources;
  private Integer page = 1;

  @Override
  public String execute() throws Exception {
    resource = resourceManager.get(id);
    if (resource == null || PublicationStatus.PRIVATE == resource.getStatus()) {
      return NOT_FOUND;
    }
    eml = resourceManager.getEml(resource);
    return SUCCESS;
  }

  public Eml getEml() {
    return eml;
  }

  public Ipt getIpt() {
    if (registrationManager.getIpt() == null) {
      return new Ipt();
    }
    return registrationManager.getIpt();
  }

  public Resource getResource() {
    return resource;
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }
}
