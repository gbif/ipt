package org.gbif.provider.localization;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.I18nWebworkAdapter;
import org.gbif.provider.webapp.action.BaseAction;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public class SimpleTextProvider implements I18nResourceProvider{
    protected static Log log = LogFactory.getLog(SimpleTextProvider.class);
	private String baseBundleName = "ApplicationResources";
	private boolean useSimpleProvider = true;
	
	public void setDefaultLocale(String defaultLocale) {
		Locale newLocale = LocalizedTextUtil.localeFromString(defaultLocale, null);
		if (newLocale != null){
			Locale.setDefault(newLocale);
			log.info("Setting default VM locale to "+newLocale);
		}
	}

	public void setBaseBundleName(String baseBundleName) {
		this.baseBundleName = baseBundleName;
		log.debug("Using base resource bundle name "+baseBundleName);
	}
	
	public void setUseSimpleProvider(boolean useSimpleProvider) {
		this.useSimpleProvider = useSimpleProvider;
		BaseAction.useSimpleTextProvider=useSimpleProvider;
		if (useSimpleProvider){
			log.debug("Enabling SimpleTextProvider for struts actions");
		}else{
			log.debug("Using standard struts2 TextProvider for struts actions");
		}
	}

	/**
     * Gets a message based on a key using the supplied args, as defined in
     * {@link java.text.MessageFormat}, or, if the message is not found, a supplied
     * default value is returned. Instead of using the value stack in the ActionContext
     * this version of the getText() method uses the provided value stack.
     *
     * @param key    the resource bundle key that is to be searched for
     * @param defaultValue the default value which will be returned if no message is found. If null the key name will be used instead
     * @param args         a list args to be used in a {@link java.text.MessageFormat} message
     * @param stack        the value stack to use for finding the text
     * @return the message as found in the resource bundle, or defaultValue if none is found
     */
    public String getText(LocaleProvider localeProvider, String key, String defaultValue, List args) {
        Object[] argsArray = ((args != null) ? args.toArray() : null);
        return getText(localeProvider, key, defaultValue, argsArray);
    }
    
    public String getText(LocaleProvider localeProvider, String key, String defaultValue, Object[] args) {
        Locale locale = localeProvider.getLocale();
        ResourceBundle bundle=findResourceBundle(baseBundleName, locale);
        return findText(bundle, key, defaultValue, args);
    }	
	
    /**
     * Finds the given resorce bundle by it's name.
     * <p/>
     * Will use <code>Thread.currentThread().getContextClassLoader()</code> as the classloader.
     * 
     * @param aBundleName  the name of the bundle (usually it's FQN classname).
     * @param locale       the locale.
     * @return  the bundle, <tt>MissingResourceException</tt> if not found.
     */
    private ResourceBundle findResourceBundle(String aBundleName, Locale locale) {
        return ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader());
    }

    
    private String findText(ResourceBundle bundle, String aTextName, String defaultMessage, Object[] args) {
        try {
			String message = bundle.getString(aTextName);
			String text;
			try {
				text = MessageFormat.format(message, args);
			} catch (IllegalArgumentException e) {
				// message and arguments dont match?
				text=message;
			} 
			return text;
		} catch (MissingResourceException e) {
			// return default message
		}
    	return defaultMessage != null ? defaultMessage : aTextName;
    }
    
    
	public ResourceBundle getTexts(Locale locale) {
		return findResourceBundle(baseBundleName, locale);
	}

	public ResourceBundle getTexts(String bundleName, Locale locale) {
		return findResourceBundle(bundleName, locale);
	}

	
	/* (non-Javadoc)
	 * @see org.displaytag.localization.I18nResourceProvider#getResource(java.lang.String, java.lang.String, javax.servlet.jsp.tagext.Tag, javax.servlet.jsp.PageContext)
	 */
	public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext context) {
		return null;
	}
}
