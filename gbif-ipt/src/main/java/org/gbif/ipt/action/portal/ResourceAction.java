package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.metadata.eml.Eml;

import com.google.inject.Inject;

public class ResourceAction extends BaseAction {
  @Inject
  private ResourceManager resourceManager;
  private Resource resource;
  private Eml eml;

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

  public Resource getResource() {
    return resource;
  }

}
