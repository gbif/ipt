package org.gbif.ipt.action.manage;

import com.google.inject.Inject;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;

public class EmlAction extends ManagerBaseAction  {

  private File emlFile;

  @Inject
  public EmlAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                        ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  public void setEmlFile(File emlFile) {
    this.emlFile = emlFile;
  }

  public String replaceEml() {
    try {
      resourceManager.replaceEml(resource, emlFile);
      addActionMessage(getText("manage.overview.success.replace.eml"));
      return SUCCESS;
    }
    catch(ImportException e) {
      addActionError(getText("manage.overview.failed.replace.eml"));
      return ERROR;
    }
  }
}
