package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.ResourceSupport;

import com.google.inject.Inject;

public class CreateResourceAction extends POSTAction {
  public enum RESOURCE_SOURCE {
    blank, dwca, metadata
  }

  @Inject
  private ResourceManager resourceManager;
  private Resource resource = new Resource();
  private String url;
  private String url2;
  private String type;
  private ResourceSupport validator = new ResourceSupport();

  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;

  public Resource getResource() {
    return resource;
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getUrl2() {
    return url2;
  }

  @Override
  public String save() {
    try {
      Resource res = resourceManager.create(id, getCurrentUser());
      ms.load(getCurrentUser(), res);
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", "Resource exists already");
    }
    return SUCCESS;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUrl2(String url2) {
    this.url2 = url2;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      validator.validate(this, resource);
    }
  }

}
