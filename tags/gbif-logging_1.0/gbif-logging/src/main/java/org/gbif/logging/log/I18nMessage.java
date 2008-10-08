/**
 * 
 */
package org.gbif.logging.log;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author timrobertson
 */
public class I18nMessage {
	protected String messageKey;
	protected String[] messageParameters;
	
	public I18nMessage(String messageKey, String[] messageParameters) {
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
	}
	public I18nMessage(String messageKey) {
		this.messageKey = messageKey;
	}
	public String getMessageKey() {
		return messageKey;
	}
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
	public String[] getMessageParameters() {
		return messageParameters;
	}
	public void setMessageParameters(String[] messageParameters) {
		this.messageParameters = messageParameters;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("messageKey", this.messageKey)
				.append("messageParameters", this.messageParameters).toString();
	}
}
