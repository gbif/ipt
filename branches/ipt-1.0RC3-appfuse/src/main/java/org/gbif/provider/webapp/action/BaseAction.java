/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.webapp.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.gbif.provider.localization.SimpleTextProvider;
import org.gbif.provider.model.LabelValue;
import org.gbif.provider.model.User;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.MailEngine;
import org.gbif.provider.service.RoleManager;
import org.gbif.provider.service.UserManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * Base action that all UI actions should extend.
 * This base action provides convenience methods and provides the IPT configuration singleton.
 * 
 */
public class BaseAction extends ActionSupport {
	public static boolean useSimpleTextProvider = true;
  public static final String OCCURRENCE = ExtensionType.Occurrence.alias;
  public static final String CHECKLIST = ExtensionType.Checklist.alias;
  public static final String METADATA = ExtensionType.Metadata.alias;
  public static final String RECORD404 = "record404";
  public static final String RESOURCE404 = "resource404";
  private static final long serialVersionUID = 3525445612504421307L;
  /**
   * Constant for cancel result String
   */
  public static final String CANCEL = "cancel";

  /**
   * Transient log to prevent session synchronization issues - children can use instance for logging.
   */
  protected transient final Log log = LogFactory.getLog(getClass());
  @Autowired
  protected AppConfig cfg;
  @Autowired
  protected UserManager userManager;
  @Autowired
  protected RoleManager roleManager;
  @Autowired
  protected MailEngine mailEngine;
  @Autowired
  protected SimpleTextProvider textProvider;
  
  /**
   * A message pre-populated with default data
   */
  @Autowired
  protected SimpleMailMessage mailMessage;
  /**
   * Velocity template to use for e-mailing
   */
  protected String templateName;
  /**
   * Indicator if the user clicked cancel
   */
  protected String cancel;
  /**
   * Indicator for the page the user came from.
   */
  protected String from;
  /**
   * Set to "delete" when a "delete" request parameter is passed in
   */
  protected String delete;
  /**
   * Set to "save" when a "save" request parameter is passed in
   */
  protected String save;
  /**
   * Simple method that returns "cancel" result
   * @return "cancel"
   */
  public String cancel() {
      return CANCEL;
  }

/**
   * Save the message in the session, appending if messages already exist
   * @param msg the message to put in the session
   */
  @SuppressWarnings("unchecked")
  protected void saveMessage(String msg) {
      List messages = (List) getRequest().getSession().getAttribute("messages");
      if (messages == null) {
          messages = new ArrayList();
      }
      messages.add(msg);
      getRequest().getSession().setAttribute("messages", messages);
  }

  /**
   * Convenience method to get the request
   * @return current request
   */
  protected HttpServletRequest getRequest() {
      return ServletActionContext.getRequest();
  }

  /**
   * Convenience method to get the response
   * @return current response
   */
  protected HttpServletResponse getResponse() {
      return ServletActionContext.getResponse();
  }

  /**
   * Convenience method to get the session. This will create a session if one doesn't exist.
   * @return the session from the request (request.getSession()).
   */
  protected HttpSession getSession() {
      return getRequest().getSession();
  }

  /**
   * Convenience method to send e-mail to users
   * @param user the user to send to
   * @param msg the message to send
   * @param url the URL to the application (or where ever you'd like to send them)
   */
  protected void sendUserMessage(User user, String msg, String url) {
      if (log.isDebugEnabled()) {
          log.debug("sending e-mail to user [" + user.getEmail() + "]...");
      }

      mailMessage.setTo(user.getFullName() + "<" + user.getEmail() + ">");

      Map<String, Object> model = new HashMap<String, Object>();
      model.put("user", user);
      // TODO: figure out how to get bundle specified in struts.xml
      // model.put("bundle", getTexts());
      model.put("message", msg);
      model.put("applicationURL", url);
      mailEngine.sendMessage(mailMessage, templateName, model);
  }

  public void setMailMessage(SimpleMailMessage mailMessage) {
      this.mailMessage = mailMessage;
  }

  public void setTemplateName(String templateName) {
      this.templateName = templateName;
  }

  /**
   * Convenience method for setting a "from" parameter to indicate the previous page.
   * @param from indicator for the originating page
   */
  public void setFrom(String from) {
      this.from = from;
  }

  public void setSave(String save) {
      this.save = save;
  }
  
  
  
  public User getCurrentUser() {
    final SecurityContext secureContext = SecurityContextHolder.getContext();
    // secure context will be null when running unit tests so leave userId as
    // null
    if (secureContext != null) {
      final Authentication auth = (SecurityContextHolder.getContext()).getAuthentication();
      if (auth.getPrincipal() instanceof UserDetails) {
        final User user = (User) auth.getPrincipal();
        return user;
      }
    }
    return null;
  }


  public String getLocaleLanguage() {
	  // struts2 manages the locale in the session param WW_TRANS_I18N_LOCALE via the i18n interceptor
      return getLocale().getLanguage();
  }

  
public boolean isAdminUser() {
    User user = getCurrentUser();
    for (LabelValue val : user.getRoleList()) {
      if (val.getValue().equalsIgnoreCase(Constants.ADMIN_ROLE)) {
        return true;
      }
    }
    return false;
  }

  public void setCancel(String cancel) {
    this.cancel = cancel;
  }

  public void setDelete(String delete) {
    this.delete = delete;
  }

  protected List<String> splitMultiValueParameter(String value) {
    if (value == null) {
      return new ArrayList<String>();
    }
    String[] paras = StringUtils.split(value, ", ");
    return Arrays.asList(paras);
  }

  /** Localizes the values of a given map using the ActionContexts locale.
   * Make sure you dont call this method in constructors as it depends on the instance of the action as the locale provider!
   * @param map To get the i18n values for
   * @return i18n results not sorted in anyway
   */
  protected Map<String, String> translateI18nMap(Map<String, String> map) {
    return translateI18nMap(map, false);
  }

  /** Localizes the values of a given map using the ActionContexts locale.
   * Make sure you dont call this method in constructors as it depends on the instance of the action as the locale provider!
   * @param map to i18n'alise
   * @param sortByValues if true, then this will sort the results alphabetically
   *          on the i18n name (useful for drop downs...)
   * @return The map which may be sorted on the values
   */
  protected Map<String, String> translateI18nMap(Map<String, String> map,
      boolean sortByValues) {
    for (String key : map.keySet()) {
      String i18Key = map.get(key);
      map.put(key, getText(i18Key));
    }
    if (!sortByValues) {
      return map;
    } else {
      // build a list that we will then sort by the values
      List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>();
      list.addAll(map.entrySet());

      // Sort the list using an annonymous inner class implementing Comparator
      // for the compare method
      Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
        public int compare(Map.Entry<String, String> entry,
            Map.Entry<String, String> entry1) {
          return entry.getValue().compareTo(entry1.getValue());
        }
      });

      // Clear the map
      map.clear();
      map = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> entry : list) {
        map.put(entry.getKey(), entry.getValue());
      }

      return map;
    }
  }
  
  	public AppConfig getCfg() {
	    return cfg;
	}

  	
  	
  	
	@Override
	public String getText(String key, List args) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, null, args);
		}
		return super.getText(key, args);
	}

	@Override
	public String getText(String key, String defaultValue, List args, ValueStack stack) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, args);
		}
		return super.getText(key, defaultValue, args, stack);
	}

	@Override
	public String getText(String key, String defaultValue, List args) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, args);
		}
		return super.getText(key, defaultValue, args);
	}

	@Override
	public String getText(String key, String defaultValue, String obj) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, new String[0]);
		}
		return super.getText(key, defaultValue, obj);
	}

	@Override
	public String getText(String key, String defaultValue, String[] args, ValueStack stack) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, args);
		}
		return super.getText(key, defaultValue, args, stack);
	}

	@Override
	public String getText(String key, String defaultValue, String[] args) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, args);
		}
		return super.getText(key, defaultValue, args);
	}

	@Override
	public String getText(String key, String defaultValue) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, defaultValue, new String[0]);
		}
		return super.getText(key, defaultValue);
	}

	@Override
	public String getText(String key, String[] args) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, null, args);
		}
		return super.getText(key, args);
	}

	@Override
	public String getText(String key) {
		if (useSimpleTextProvider){
			return textProvider.getText(this, key, null, new String[0]);
		}
		return super.getText(key);
	}

	@Override
	public ResourceBundle getTexts() {
		if (useSimpleTextProvider){
			return textProvider.getTexts(getLocale());
		}
		return super.getTexts();
	}

	@Override
	public ResourceBundle getTexts(String bundleName) {
		if (useSimpleTextProvider){
			return textProvider.getTexts(bundleName, getLocale());
		}
		return super.getTexts(bundleName);
	}
  	
  	
}
