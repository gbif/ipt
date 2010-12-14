package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

public class HomeAction extends BaseAction {
  private List<Resource> resources;
  @Inject
  private ResourceManager resourceManager;

  @Override
  public String execute() {
    resources = resourceManager.list(PublicationStatus.PUBLIC);
    resources.addAll(resourceManager.list(PublicationStatus.REGISTERED));
    // sort alphabetically
    Collections.sort(resources);
    return SUCCESS;
  }

  public List<Resource> getResources() {
    return resources;
  }
}
