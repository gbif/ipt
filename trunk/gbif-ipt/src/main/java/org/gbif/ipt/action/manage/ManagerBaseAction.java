package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class ManagerBaseAction extends POSTAction {

  protected ResourceManager resourceManager;
  protected Resource resource;

  @Inject
  public ManagerBaseAction(SimpleTextProvider textProvider, AppConfig cfg, ResourceManager resourceManager) {
    this.textProvider = textProvider;
    this.cfg = cfg;
    this.resourceManager = resourceManager;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    // look for resource parameter
    String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
    if (res == null) {
      // try session instead
      try {
        res = (String) session.get(Constants.SESSION_RESOURCE);
      } catch (Exception e) {
        // swallow. if session is not yet opened we get an exception here...
      }
    }
    resource = resourceManager.get(res);
    if (resource == null) {
      notFound = true;
    }
  }

  protected void saveResource() {
    resourceManager.save(resource);
  }


  public Resource getResource() {
    return resource;
  }


  public void setResource(Resource resource) {
    this.resource = resource;
  }

}
