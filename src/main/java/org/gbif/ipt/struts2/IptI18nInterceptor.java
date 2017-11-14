package org.gbif.ipt.struts2;

import com.google.common.collect.Sets;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.I18nInterceptor;

import java.util.Locale;
import java.util.Set;

/**
 * An interceptor that ensures that all Locales supported by the IPT can be handled properly. Needed because
 * Struts2 i18n interceptor only handles Locales supported by the JRE, which is Locale.getAvailableLocales().
 */
public class IptI18nInterceptor extends I18nInterceptor {
  private static final Logger LOG = Logger.getLogger(SimpleTextProvider.class);
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
  private static final Locale DEFAULT = Locale.ENGLISH;

  @Override
  protected Locale getLocaleFromParam(Object requestedLocale) {
    Locale locale = null;
    if (requestedLocale != null) {
      locale = (requestedLocale instanceof Locale) ? (Locale) requestedLocale
        : LocalizedTextUtil.localeFromString(requestedLocale.toString(), null);
      if (locale == null || !IPT_SUPPORTED_LOCALES.contains(locale)) {
        locale = DEFAULT;
        LOG.debug("Use default locale: " + locale.getLanguage());
      } else if (LOG.isDebugEnabled()) {
        LOG.debug("Applied request locale: " + locale.getLanguage());
      }
    }

    return locale;
  }
}
