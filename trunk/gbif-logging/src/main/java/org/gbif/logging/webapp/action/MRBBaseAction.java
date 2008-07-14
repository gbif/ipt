/**
 * 
 */
package org.gbif.logging.webapp.action;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.appfuse.webapp.action.BaseAction;
import org.gbif.logging.util.LocalizedTextUtil;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * A base action that will override all the TextProvider methods to 
 * use MergedResourceBundle
 * 
 * Note: this ignores the valuestack
 * 
 * @author timrobertson
 */
public class MRBBaseAction extends BaseAction {
	private static final long serialVersionUID = -6052037136059295502L;

	protected static LocalizedTextUtil localizedTextUtil = new LocalizedTextUtil();
	
	@Override
	public String getText(String key, String defaultValue, List args, ValueStack stack) {
		String[] values = new String[args.size()];
		for (int i=0; i<args.size(); i++) {
			values[i] = (String)args.get(i);
		}
		return localizedTextUtil.findText(key, values, getLocale(), defaultValue);
	}

	@Override
	public String getText(String textName, List args) {
		return getText(textName, "", args, null);
	}

	@Override
	public String getText(String textName, String defaultValue, List args) {
		return getText(textName, defaultValue, args, null);
	}

	@Override
	public String getText(String textName, String defaultValue, String obj) {
		String[] values = new String[]{obj};
		return getText(textName, defaultValue, values, null);
	}

	@Override
	public String getText(String key, String defaultValue, String[] args, ValueStack stack) {
		return localizedTextUtil.findText(key, args, getLocale(), defaultValue);
	}

	@Override
	public String getText(String key, String defaultValue, String[] args) {
		return localizedTextUtil.findText(key, args, getLocale(), defaultValue);
	}

	@Override
	public String getText(String textName, String defaultValue) {
		return localizedTextUtil.findText(textName, null, getLocale(), defaultValue);
	}

	@Override
	public String getText(String key, String[] args) {
		return localizedTextUtil.findText(key, args, getLocale(), "");
	}

	@Override
	public String getText(String textName) {
		return localizedTextUtil.findText(textName, null, getLocale(), "");
	}

	@Override
	public ResourceBundle getTexts() {
		return localizedTextUtil.getResourceBundle();
	}

	/**
	 * bundleName is ignored
	 */
	@Override
	public ResourceBundle getTexts(String bundleName) {
		return localizedTextUtil.getResourceBundle();
	}}
