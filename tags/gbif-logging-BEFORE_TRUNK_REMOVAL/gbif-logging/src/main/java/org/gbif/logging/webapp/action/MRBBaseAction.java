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
	
}
