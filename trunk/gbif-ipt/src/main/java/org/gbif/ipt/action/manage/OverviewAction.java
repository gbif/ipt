package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

public class OverviewAction extends BaseAction {
  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  @Inject
  private ResourceManager resourceManager;

  public ResourceManagerSession getMs() {
    return ms;
  }

  public void setMs(ResourceManagerSession ms) {
    this.ms = ms;
  }

}
