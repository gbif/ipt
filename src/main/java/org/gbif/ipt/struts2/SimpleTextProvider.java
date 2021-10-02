/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.struts2;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.LocaleProvider;

/**
 * A basic text provider for internationalised messages that can replace the native struts2 one. It uses only a single
 * bundle name to speed up the lookup which increases performance of page rendering with many text blocks by sometimes
 * more than 100%.
 */
public class SimpleTextProvider {

  private static final Logger LOG = LogManager.getLogger(SimpleTextProvider.class);
  private static final String DEFAULT_BUNDLE = "ApplicationResources";
  private final Set<String> baseBundleNames = new HashSet<String>();

  public SimpleTextProvider() {
    baseBundleNames.add(DEFAULT_BUNDLE);
  }

  /**
   * Finds the given resource bundle by it's name and locale.
   * <br/>
   * Will use <code>Thread.currentThread().getContextClassLoader()</code> as the classloader.
   *
   * @param aBundleName the name of the bundle (usually it's FQN classname).
   * @param locale      the locale.
   *
   * @return the bundle, defaulting to the English bundle if no match for locale found or if Exception occurred
   */
  public ResourceBundle findResourceBundle(String aBundleName, Locale locale) {
    Locale currentLocale = Locale.getDefault();
    try {
      // override default Locale in case incoming locale isn't matched - see ResourceBundle.getFallbackLocale()
      Locale.setDefault(Locale.ENGLISH);
      return ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader());
    } catch (Exception e) {
      return ResourceBundle.getBundle(aBundleName, Locale.ENGLISH, Thread.currentThread().getContextClassLoader());
    } finally {
      Locale.setDefault(currentLocale);
    }
  }

  public String findText(ResourceBundle bundle, String aTextName, String defaultMessage, Object[] args) {
    try {
      String message = bundle.getString(aTextName);
      String text;
      try {
        text = MessageFormat.format(message, args);
      } catch (IllegalArgumentException e) {
        // message and arguments dont match?
        LOG.debug(e);
        text = message;
      }
      return text;
    } catch (MissingResourceException e) {
      // return default message
    }
    return defaultMessage != null ? defaultMessage : aTextName;
  }

  /**
   * Gets a message based on a key using the supplied args, as defined in {@link MessageFormat}, or, if the
   * message is not found, a supplied default value is returned. Instead of using the value stack in the ActionContext
   * this version of the getText() method uses the provided value stack.
   * 
   * @param localeProvider LocaleProvider
   * @param key the resource bundle key that is to be searched for
   * @param defaultValue the default value which will be returned if no message is found. If null the key name will be
   *        used instead
   * @param args a list args to be used in a {@link MessageFormat} message
   * @return the message as found in the resource bundle, or defaultValue if none is found
   */
  public String getText(LocaleProvider localeProvider, String key, String defaultValue, List args) {
    Object[] argsArray = args != null ? args.toArray() : null;
    return getText(localeProvider, key, defaultValue, argsArray);
  }

  public String getText(LocaleProvider localeProvider, String key, String defaultValue, Object[] args) {
    // Locale, defaulting to English if it cannot be determined
    Locale locale = (localeProvider.getLocale() == null) ? Locale.ENGLISH : localeProvider.getLocale();
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
    return findResourceBundle(DEFAULT_BUNDLE, locale);
  }

  public ResourceBundle getTexts(String bundleName, Locale locale) {
    return findResourceBundle(bundleName, locale);
  }
}
