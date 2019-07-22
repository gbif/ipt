package org.gbif.ipt.struts2;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.I18nInterceptor;

import java.util.Locale;
import java.util.Set;

/**
 * An interceptor that ensures that all Locales supported by the IPT can be handled properly. Needed because
 * Struts2 i18n interceptor only handles Locales supported by the JRE, which is Locale.getAvailableLocales().
 */
public class IptI18nInterceptor extends I18nInterceptor {
  private static final Logger LOG = LogManager.getLogger(IptI18nInterceptor.class);
  private static final Set<Locale> IPT_SUPPORTED_LOCALES = Sets.newHashSet(
      Locale.ENGLISH,
      Locale.FRENCH,
      Locale.CHINESE,
      Locale.JAPANESE,
      new Locale("es"),
      new Locale("pt"),
      new Locale("ru"),
      new Locale("fa")
  );

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
    if (locale != null && !IPT_SUPPORTED_LOCALES.contains(locale)) {
      locale = Locale.getDefault();
    }
    return locale;
  }
}
