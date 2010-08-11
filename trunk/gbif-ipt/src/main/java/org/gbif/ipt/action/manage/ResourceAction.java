package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.ResourceConfiguration;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

public class ResourceAction extends POSTAction {
  @Inject
  private ResourceManager resourceManager;
  private String r;

  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;

  @Override
  public String delete() {
    // TODO Auto-generated method stub
    return super.delete();
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public String getR() {
    return r;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
  }

  @Override
  public String save() {
    if (id != null) {
      try {
        ResourceConfiguration config = resourceManager.create(id, getCurrentUser());
        ms.load(getCurrentUser(), config);
      } catch (AlreadyExistingException e) {
        addFieldError("id", "Resource exists already");
      }
    }
    return SUCCESS;
  }

  public void setMs(ResourceManagerSession ms) {
    this.ms = ms;
  }

  public void setR(String r) {
    this.r = r;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (id == null || id.length() < 3) {
        addFieldError("id", "Shortname with at least 3 characters required");
      }
    }
  }

}
