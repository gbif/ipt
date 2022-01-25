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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.I18nInterceptor;

/**
 * An interceptor that ensures that all Locales supported by the IPT can be handled properly. Needed because
 * Struts2 i18n interceptor only handles Locales supported by the JRE, which is Locale.getAvailableLocales().
 */
public class IptI18nInterceptor extends I18nInterceptor {

  private static final Logger LOG = LogManager.getLogger(IptI18nInterceptor.class);

  private static final Set<Locale> IPT_SUPPORTED_LOCALES;

  static {
    IPT_SUPPORTED_LOCALES = new HashSet<>();
    IPT_SUPPORTED_LOCALES.add(Locale.UK); // Used to ensure a day-month-year order in formatted dates.
    IPT_SUPPORTED_LOCALES.add(Locale.FRENCH);
    IPT_SUPPORTED_LOCALES.add(Locale.CHINESE);
    IPT_SUPPORTED_LOCALES.add(Locale.JAPANESE);
    IPT_SUPPORTED_LOCALES.add(new Locale("es"));
    IPT_SUPPORTED_LOCALES.add(new Locale("pt"));
    IPT_SUPPORTED_LOCALES.add(new Locale("ru"));
    IPT_SUPPORTED_LOCALES.add(new Locale("fa"));
  }

  @Override
  protected Locale getLocaleFromParam(Object requestedLocale) {
    Locale locale = null;
    try {
      if (requestedLocale != null) {
        locale = (requestedLocale instanceof Locale) ? (Locale) requestedLocale
            : LocaleUtils.toLocale(requestedLocale.toString());
        if (locale != null && LOG.isDebugEnabled()) {
          LOG.debug("Applied request locale: " + locale.getLanguage());
        }
      }
    } catch (IllegalArgumentException e) {
      LOG.debug("Invalid request locale: {}", requestedLocale);
      locale = Locale.getDefault();
    }
    if (Locale.ENGLISH.equals(locale)) {
      locale = Locale.UK;
    }
    if (locale != null && !IPT_SUPPORTED_LOCALES.contains(locale)) {
      locale = Locale.getDefault();
    }
    return locale;
  }
}
