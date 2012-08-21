package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager) {
    super(textProvider, cfg, registrationManager);
  }

  @Override
  public String execute() {
    return SUCCESS;
  }
}
