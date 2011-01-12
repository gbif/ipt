package org.gbif.provider.webapp.interceptor;

import java.util.Locale;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.I18nInterceptor;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public class LocaleInterceptor extends I18nInterceptor{
	private Locale defaultLocale = Locale.ENGLISH;
	
	private LocaleInterceptor() {
        if (log.isDebugEnabled()) {
            log.debug("new LocaleInterceptor()");
        }
	}

	public void setDefaultLocale(String locale) {
		defaultLocale = LocalizedTextUtil.localeFromString(locale, defaultLocale);
        if (log.isDebugEnabled()) {
            log.debug("Setting new I18nInterceptor default locale to "+locale);
        }
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("intercept '"
                    + invocation.getProxy().getNamespace() + "/"
                    + invocation.getProxy().getActionName() + "' { ");
        }
        //get requested locale
        Map params = invocation.getInvocationContext().getParameters();
        Object requested_locale = params.remove(parameterName);
        if (requested_locale != null && requested_locale.getClass().isArray()
                && ((Object[]) requested_locale).length == 1) {
            requested_locale = ((Object[]) requested_locale)[0];
        }

        if (log.isDebugEnabled()) {
            log.debug("requested_locale=" + requested_locale);
        }

        //save it in session
        Map session = invocation.getInvocationContext().getSession();
        if (session != null) {
            if (requested_locale != null) {
            	// new reqiested locale
                Locale locale = (requested_locale instanceof Locale) ?
                        (Locale) requested_locale : LocalizedTextUtil.localeFromString(requested_locale.toString(), null);
                if (log.isDebugEnabled()) {
                    log.debug("store locale=" + locale);
                }

                if (locale != null) {
                    session.put(attributeName, locale);
                }
            }else if (!session.containsKey(attributeName)){
            	// none in session yet, use default IPT locale
                if (log.isDebugEnabled()) {
                    log.debug("store default locale=" + defaultLocale);
                }
                session.put(attributeName, defaultLocale);
            }

            //set locale for action
            Object locale = session.get(attributeName);
            if (locale != null && locale instanceof Locale) {
                if (log.isDebugEnabled()) {
                    log.debug("apply locale=" + locale);
                }

                saveLocale(invocation, (Locale)locale);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("before Locale=" + invocation.getStack().findValue("locale"));
        }

        final String result = invocation.invoke();
        if (log.isDebugEnabled()) {
            log.debug("after Locale=" + invocation.getStack().findValue("locale"));
        }

        if (log.isDebugEnabled()) {
            log.debug("intercept } ");
        }

        return result;
    }


}
