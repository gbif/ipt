package org.gbif.ipt.action;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ws.util.XSSUtil;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

/**
 * The base of all IPT actions. This handles conditions such as menu items, a custom text provider, sessions, currently
 * logged in user, and hosting organization information.
 */
public class BaseAction extends ActionSupport implements SessionAware, Preparable, ServletRequestAware {

  // logging
  private static final Logger LOG = Logger.getLogger(BaseAction.class);

  private static final long serialVersionUID = -2330991910834399442L;
  public static final String NOT_MODIFIED = "304";
  public static final String NOT_FOUND = "404";
  public static final String NOT_ALLOWED = "401";
  public static final String NOT_ALLOWED_MANAGER = "401-manager";
  public static final String HOME = "home";
  public static final String LOCKED = "locked";
  public static final String NOT_AVAILABLE = "410";


  protected List<String> warnings = new ArrayList<String>();
  protected Map<String, Object> session;
  protected HttpServletRequest req;
  // a generic identifier for loading an object BEFORE the param interceptor sets values
  protected String id;

  protected SimpleTextProvider textProvider;
  protected AppConfig cfg;
  protected RegistrationManager registrationManager;

  @Inject
  public BaseAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager) {
    this.textProvider = textProvider;
    this.cfg = cfg;
    this.registrationManager = registrationManager;
  }

  /**
   * Adds an exception message, if not null, to the action warnings.
   *
   * @param e the exception from which the message is taken
   */
  protected void addActionExceptionWarning(Exception e) {
    String msg = e.getMessage();
    if (msg != null) {
      warnings.add(msg);
    }
  }

  /**
   * Adds a warning similar to the action errors to the user UI, but does not interact with the validation aware
   * workflow interceptor, therefore no changes to the result name of the action are expected.
   * This is the way to present user warnings/errors others than for form validation.
   * If you want form validation with the workflow interceptor, please {@link #addActionError(String)} instead.
   */
  public void addActionWarning(String anErrorMessage) {
    warnings.add(anErrorMessage);
  }

  public void addActionWarning(String anErrorMessage, Exception e) {
    warnings.add(anErrorMessage);
    addActionExceptionWarning(e);
  }

  /**
   * Return a list of action warning strings.
   *
   * @return list of action warning strings.
   */
  public List<String> getActionWarnings() {
    return warnings;
  }

  /**
   * Easy access to the configured application root for simple use in templates.
   */
  public String getBase() {
    return getBaseURL();
  }

  public String getBaseURL() {
    return cfg.getBaseUrl();
  }

  /**
   * @return the requested URL using the configured base url including all query parameters but a potentially existing request_locale parameter.
   * Returns baseURL in case of errors reconstructing the correct URL.
   */
  public String getRequestURL() {
    try {
      return UriBuilder.fromUri(getBaseURL())
          .path(Strings.nullToEmpty(req.getServletPath()))
          .path(Strings.nullToEmpty(req.getPathInfo()))
          .replaceQuery(req.getQueryString())
          .replaceQueryParam("request_locale")
          .build().toString();
    } catch (RuntimeException e) {
      LOG.warn("Failed to reconstruct requestURL from " + req.getRequestURL(), e);
    }
    return getBaseURL();
  }

  public AppConfig getCfg() {
    return cfg;
  }

  /**
   * Return the currently logged in (session) user.
   *
   * @return the currently logged in (session) user or null if not logged in
   */
  public User getCurrentUser() {
    User u = null;
    try {
      u = (User) session.get(Constants.SESSION_USER);
    } catch (Exception e) {
      LOG.debug("A problem occurred retrieving current user. This can happen if the session is not yet opened");
    }
    return u;
  }

  public String getId() {
    return id;
  }

  /**
   * Return the locale language code.
   * Struts2 manages the locale in the session param WW_TRANS_I18N_LOCALE via the i18n interceptor.
   *
   * @return locale language code, defaulting to "en" if locale was null or if locale did not match a local
   * ResourceBundle
   */
  public String getLocaleLanguage() {
    if (getLocale() != null) {
      String requestedLocale = Strings.emptyToNull(getLocale().getLanguage());
      if (requestedLocale != null && !XSSUtil.containsXSS(requestedLocale)) {
        ResourceBundle resourceBundle = textProvider.getTexts(new Locale(requestedLocale));
        return resourceBundle.getLocale().getLanguage();
      }
    }
    return Locale.ENGLISH.getLanguage();
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

  public String getTextWithDynamicArgs(String key, String... args) {
    return textProvider.getText(this, key, null, args);
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public boolean isAdminRights() {
    User user = getCurrentUser();
    if (user != null && user.hasAdminRights()) {
      return true;
    }
    return false;
  }

  protected boolean isHttpPost() {
    if (req.getMethod().equalsIgnoreCase("post")) {
      return true;
    }
    return false;
  }

  /**
   * Determine whether some user is logged in or not.
   *
   * @return true if some user is logged in or false otherwise
   */
  public boolean isLoggedIn() {
    return getCurrentUser() != null;
  }

  public boolean isManagerRights() {
    User user = getCurrentUser();
    if (user != null && user.hasManagerRights()) {
      return true;
    }
    return false;
  }

  /**
   * @param name cookie name
   * @return the request cookie or null if no cookie with that name exists
   */
  public Cookie getCookie(String name) {
    for (Cookie c : req.getCookies()) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Override this method if you need to load entities based on the id value before the PARAM interceptor is called.
   * You can also use this method to prepare a new, empty instance in case no id was provided. If the id parameter
   * alone
   * is not sufficient to load your entities, you can access the request object directly like we do here and read any
   * other parameter you need to prepare the action for the param phase.
   */
  public void prepare() {
    // see if an id was provided in the request.
    // we dont use the PARAM - PREPARE - PARAM interceptor stack
    // so we investigate the request object directly BEFORE the param interceptor is called
    // this allows us to load any existing instances that should be modified
    id = StringUtils.trimToNull(req.getParameter("id"));
  }

  public void setServletRequest(HttpServletRequest req) {
    this.req = req;
  }

  public void setSession(Map<String, Object> session) {
    this.session = session;
    // always keep sth in the session otherwise the session is not maintained and e.g. the message redirect interceptor
    // doesnt work
    if (session.isEmpty()) {
      session.put("-", true);
    }
  }

  /**
   * Utility to compare 2 objects for comparison when both converted to strings useful to compare if a submitted value
   * is the same as the persisted value.
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

  /**
   * @return the registered IPT instance, or null if the IPT hasn't been registered yet.
   */
  public Ipt getRegisteredIpt() {
    return registrationManager.getIpt();
  }

  /**
   * @return default organisation named "No organisation" or null if it doesn't exist
   */
  @Nullable
  public Organisation getDefaultOrganisation() {
    Organisation noOrganisation = registrationManager.get(Constants.DEFAULT_ORG_KEY);
    return (noOrganisation == null) ? null : noOrganisation;
  }

}
