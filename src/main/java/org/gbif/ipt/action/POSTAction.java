package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class POSTAction extends BaseAction {

  protected boolean delete = false;
  protected boolean notFound = false;
  protected boolean validate = true;
  protected String defaultResult = INPUT;

  @Inject
  public POSTAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager) {
    super(textProvider, cfg, registrationManager);
  }

  /**
   * Override this method if you need to delete entities based on the id value after the PARAM interceptor is called.
   */
  public String delete() throws Exception {
    return SUCCESS;
  }

  @Override
  public String execute() throws Exception {
    // if notFound was set to true during prepare() the supplied id parameter didn't exist - return a 404!
    if (notFound) {
      return NOT_FOUND;
    }
    // if this is a GET request we request the INPUT form
    if (isHttpPost()) {
      // if its a POST we either save or delete
      // supplied default methods which be overridden
      String result = delete ? delete() : save();
      // check again if notFound was set
      // this also allows the load() or delete() method to set the flag
      return notFound ? NOT_FOUND : result;
    }
    return defaultResult;
  }

  public boolean isDelete() {
    return delete;
  }

  /**
   * Override this method if you need to persist entities after the PARAM interceptor is called.
   */
  public String save() throws Exception {
    return SUCCESS;
  }

  public void setDelete(String delete) {
    this.delete = StringUtils.trimToNull(delete) != null;
    if (this.delete) {
      validate = false;
    }
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  @Override
  public void validate() {
    // only validate on form submissions
    if (isHttpPost() && validate) {
      validateHttpPostOnly();
    }
  }

  /**
   * Validation method to be overridden when only http post, i.e. form submissions, should be validated
   * and not any get requests.
   */
  public void validateHttpPostOnly() {
    // don't do any validation out of the box
  }
}
