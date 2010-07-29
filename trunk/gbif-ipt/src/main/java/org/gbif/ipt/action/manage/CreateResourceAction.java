package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

public class CreateResourceAction extends POSTAction {
  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  @Inject
  private ResourceManager resourceManager;
  private String shortname;
  private String existing;

  public String getExisting() {
    return existing;
  }

  public String getShortname() {
    return shortname;
  }

  @Override
  public String save() {
    try {
      Resource res = resourceManager.create(shortname, getCurrentUser());
      ms.load(getCurrentUser(), res);
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", "Resource exists already");
    }
    return SUCCESS;
  }

  public void setExisting(String existing) {
    this.existing = existing;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (shortname == null || shortname.length() < 3) {
        addFieldError("shortname", "Short resource name must be unique and at least 3 characters long");
      }
    }
  }

}
