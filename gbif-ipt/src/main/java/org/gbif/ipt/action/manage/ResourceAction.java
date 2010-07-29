package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

public class ResourceAction extends POSTAction {
  public enum RESOURCE_SOURCE {
    blank, dwca, metadata
  }

  @Inject
  private ResourceManager resourceManager;;
  private String r;

  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;

  @Override
  public String delete() {
    // TODO Auto-generated method stub
    return super.delete();
  }

  @Override
  public String execute() {
    // if notFound was set to true during prepare() the supplied id parameter didnt exist - return a 404!
    if (notFound) {
      return NOT_FOUND;
    }
    // if this is a GET request we request the INPUT form
    if (isHttpPost()) {
      // if its a POST we either save or delete
      // suplied default methods which be overridden
      String result;
      if (delete) {
        result = delete();
      } else {
        result = save();
      }
      // check again if notFound was set
      // this also allows the load() or delete() method to set the flag
      if (notFound) {
        return NOT_FOUND;
      } else {
        return result;
      }
    } else {
      if (r != null) {
        return SUCCESS;
      }
      return INPUT;
    }
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
        Resource res = resourceManager.create(id);
        ms.load(getCurrentUser(), res);
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
