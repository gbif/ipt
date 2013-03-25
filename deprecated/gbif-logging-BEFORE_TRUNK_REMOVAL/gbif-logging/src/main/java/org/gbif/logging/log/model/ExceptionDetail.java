/**
 * 
 */
package org.gbif.logging.log.model;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author timrobertson
 *
 */
public class ExceptionDetail {
	protected String message;
	protected List<CauseDetail> causeDetails = new LinkedList<CauseDetail>();
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<CauseDetail> getCauseDetails() {
		return causeDetails;
	}
	public void setCauseDetails(List<CauseDetail> causeDetails) {
		this.causeDetails = causeDetails;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("causeDetails",
				this.causeDetails).append("message", this.message).toString();
	}
}
