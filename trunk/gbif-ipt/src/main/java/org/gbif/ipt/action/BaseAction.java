/**
 * 
 */
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.User;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.util.ValueStack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.SessionAware;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The base of all IPT actions This handles conditions such as menu items, a custom text provider, sessions, currently
 * logged in user
 * 
 * @author tim
 */
public class BaseAction extends ActionSupport implements Action, SessionAware {
  private static final long serialVersionUID = -2330991910834399442L;
  public static final String NOT_FOUND = "404";
  public static final String NOT_ALLOWED = "401";
  public static final String NOT_ALLOWED_MANAGER = "401-manager";
  public static final String NOT_IMPLEMENTED = "notImplemented";
  /**
   * Occassionally Struts2 complains with it's own logging which seems like a Struts2 issue
   */
  protected static Log log = LogFactory.getLog(BaseAction.class);

  protected Map<String, Object> session;
  @Inject
  protected SimpleTextProvider textProvider;
  @Inject
  protected AppConfig cfg;
  @Inject
  protected DataDir dataDir;

  /**
   * Easy access to the configured application root for simple use in templates
   * 
   * @return
   */
  public String getBase() {
    return cfg.getBaseURL();
  }

  public String getBaseURL() {
    return cfg.getBaseURL();
  }

  public AppConfig getCfg() {
    return cfg;
  }

  /**
   * @return the currently logged in (session) user or null if not logged in
   */
  public User getCurrentUser() {
    User u = null;
    try {
      u = (User) session.get(Constants.SESSION_USER);
    } catch (Exception e) {
      // swallow. if session is not yet opened we get an exception here...
    }
    return u;
  }

  // ////////////////////////////////////////////////////////////////
  // CUSTOM SIMPLE TEXT PROVIDER FOR MUCH FASTER LOOKUPS !!!
  // this increases page rendering with lots of <@s:text> tags by nearly 100%
  // ////////////////////////////////////////////////////////////////
  public String getLocaleLanguage() {
    // struts2 manages the locale in the session param WW_TRANS_I18N_LOCALE via
    // the i18n interceptor
    return getLocale().getLanguage();
  }

  @Override
  public String getText(String key) {
    return textProvider.getText(this, key, null, new String[0]);
  }

  @Override
  public String getText(String key, List args) {
    return textProvider.getText(this, key, null, args);
  }

  @Override
  public String getText(String key, String defaultValue) {
    return textProvider.getText(this, key, defaultValue, new String[0]);
  }

  @Override
  public String getText(String key, String defaultValue, List args) {
    return textProvider.getText(this, key, defaultValue, args);
  }

  @Override
  public String getText(String key, String defaultValue, List args, ValueStack stack) {
    return textProvider.getText(this, key, defaultValue, args);
  }

  @Override
  public String getText(String key, String defaultValue, String obj) {
    return textProvider.getText(this, key, defaultValue, new String[0]);
  }

  @Override
  public String getText(String key, String defaultValue, String[] args) {
    return textProvider.getText(this, key, defaultValue, args);
  }

  @Override
  public String getText(String key, String defaultValue, String[] args, ValueStack stack) {
    return textProvider.getText(this, key, defaultValue, args);
  }

  @Override
  public String getText(String key, String[] args) {
    return textProvider.getText(this, key, null, args);
  }

  @Override
  public ResourceBundle getTexts() {
    return textProvider.getTexts(getLocale());
  }

  @Override
  public ResourceBundle getTexts(String bundleName) {
    return textProvider.getTexts(bundleName, getLocale());
  }

  public boolean isAdminRights() {
    User user = getCurrentUser();
    if (user != null && user.hasAdminRights()) {
      return true;
    }
    return false;
  }

  /**
   * @return true if some user is logged in or false otherwise
   */
  public boolean isLoggedIn() {
    return getCurrentUser() == null ? false : true;
  }

  public boolean isManagerRights() {
    User user = getCurrentUser();
    if (user != null && user.hasManagerRights()) {
      return true;
    }
    return false;
  }

  public void setSession(Map<String, Object> session) {
    this.session = session;
  }

  /**
   * Utility to compare 2 objects for comparison when both converted to strings useful to compare if a submitted value
   * is the same as the persisted value
   * 
   * @return true only if o1.equals(o2)
   */
  protected boolean stringEquals(Object o1, Object o2) {
    // both null
    if (o1 == null && o2 == null) {
      return true;
    }
    if (o1 != null && o2 != null) {
      return o1.toString().equals(o2.toString());
    }
    return false;
  }
}
