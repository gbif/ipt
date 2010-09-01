package org.gbif.ipt.struts2;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

import org.apache.log4j.Logger;
//import org.displaytag.localization.I18nResourceProvider;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * A basic text provider for internationalised messages that can replace the native struts2 one. It uses only a single
 * bundle name to speed up the lookup which increases performance of page rendering with many text blocks by sometimes
 * more than 100%
 * 
 * @author markus
 */

public class SimpleTextProvider { //implements I18nResourceProvider {
  protected static Logger log = Logger.getLogger(SimpleTextProvider.class);
  private static final String defaultBundle = "ApplicationResources";
  private Set<String> baseBundleNames = new HashSet<String>();

  public SimpleTextProvider() {
    super();
    baseBundleNames.add(defaultBundle);
  }

  /**
   * Finds the given resorce bundle by it's name.
   * <p/>
   * Will use <code>Thread.currentThread().getContextClassLoader()</code> as the classloader.
   * 
   * @param aBundleName the name of the bundle (usually it's FQN classname).
   * @param locale the locale.
   * @return the bundle, <tt>MissingResourceException</tt> if not found.
   */
  public ResourceBundle findResourceBundle(String aBundleName, Locale locale) {
    return ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader());
  }

  public String findText(ResourceBundle bundle, String aTextName, String defaultMessage, Object[] args) {
    try {
      String message = bundle.getString(aTextName);
      String text;
      try {
        text = MessageFormat.format(message, args);
      } catch (IllegalArgumentException e) {
        // message and arguments dont match?
        log.debug(e);
        text = message;
      }
      return text;
    } catch (MissingResourceException e) {
      // return default message
    }
    return defaultMessage != null ? defaultMessage : aTextName;
  }

  /*
   * (non-Javadoc)
   * @see org.displaytag.localization.I18nResourceProvider#getResource(java.lang.String, java.lang.String,
   * javax.servlet.jsp.tagext.Tag, javax.servlet.jsp.PageContext)
   */
  public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext context) {
    return null;
  }

  /**
   * Gets a message based on a key using the supplied args, as defined in {@link java.text.MessageFormat}, or, if the
   * message is not found, a supplied default value is returned. Instead of using the value stack in the ActionContext
   * this version of the getText() method uses the provided value stack.
   * 
   * @param key the resource bundle key that is to be searched for
   * @param defaultValue the default value which will be returned if no message is found. If null the key name will be
   *        used instead
   * @param args a list args to be used in a {@link java.text.MessageFormat} message
   * @param stack the value stack to use for finding the text
   * @return the message as found in the resource bundle, or defaultValue if none is found
   */
  public String getText(LocaleProvider localeProvider, String key, String defaultValue, List args) {
    Object[] argsArray = ((args != null) ? args.toArray() : null);
    return getText(localeProvider, key, defaultValue, argsArray);
  }

  public String getText(LocaleProvider localeProvider, String key, String defaultValue, Object[] args) {
    Locale locale = localeProvider.getLocale();
    String text = null;
    for (String resName : baseBundleNames) {
      ResourceBundle bundle = findResourceBundle(resName, locale);
      text = findText(bundle, key, defaultValue, args);
      if (text != null) {
        break;
      }
    }
    return text;
  }

  public ResourceBundle getTexts(Locale locale) {
    return findResourceBundle(defaultBundle, locale);
  }

  public ResourceBundle getTexts(String bundleName, Locale locale) {
    return findResourceBundle(bundleName, locale);
  }

  public void setBaseBundleNames(Set<String> baseBundleNames) {
    this.baseBundleNames = baseBundleNames;
    log.debug("Using base resource bundle names " + baseBundleNames);
  }

  public void setDefaultLocale(String defaultLocale) {
    Locale newLocale = LocalizedTextUtil.localeFromString(defaultLocale, null);
    if (newLocale != null) {
      Locale.setDefault(newLocale);
      log.info("Setting default VM locale to " + newLocale);
    }
  }
}
