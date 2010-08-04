package org.gbif.ipt.action;

import org.apache.commons.lang.StringUtils;

public class POSTAction extends BaseAction {
  protected boolean delete = false;
  protected boolean notFound = false;

  /**
   * Override this method if you need to delete entities based on the id value after the PARAM interceptor is called
   * 
   * @return
   */
  public String delete() {
    return SUCCESS;
  }

  @Override
  public String execute() throws Exception {
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
    }
    return INPUT;
  }

  public boolean isDelete() {
    return delete;
  }

  /**
   * Override this method if you need to persist entities after the PARAM interceptor is called
   * 
   * @return
   * @throws Exception
   */
  public String save() throws Exception {
    return SUCCESS;
  }

  public void setDelete(String delete) {
    this.delete = StringUtils.trimToNull(delete) != null;
  }

  @Override
  public void validate() {
    // only validate on form submissions
    if (isHttpPost()) {
      validateHttpPostOnly();
    }
  }

  /**
   * Validation method to be overridden when only http post, i.e. form submissions, should be validated
   * and not any get requests.
   */
  public void validateHttpPostOnly() {
    // dont do any validation out of the box
  }
}
