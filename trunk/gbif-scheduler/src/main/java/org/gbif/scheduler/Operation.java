/**
 * 
 */
package org.gbif.scheduler;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author timrobertson
 */
public class Operation {
	private String i18nKey;
	private String method;
	
	public Operation(String i18nKey, String method) {
		this.i18nKey = i18nKey;
		this.method = method;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String key) {
		i18nKey = key;
	}

	public String getMethod() {
		return method;
	}

	public void setmethod(String method) {
		this.method = method;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("I18nKey", this.getI18nKey())
				.append("method", this.method).toString();
	}

	
}
