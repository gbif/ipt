/**
 * 
 */
package org.gbif.logging.webapp.action.model;

import java.util.Date;

import org.gbif.logging.log.model.ExceptionDetail;

/**
 * Lightweight DTO for streaming log events for the rendering in the view
 * @author timrobertson
 */
public class LogEventDTO {
	protected long id;
	protected int level;
	protected String message;
	protected Date timestamp;
	protected ExceptionDetail exceptionDetail;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public ExceptionDetail getExceptionDetail() {
		return exceptionDetail;
	}
	public void setExceptionDetail(ExceptionDetail exceptionDetails) {
		this.exceptionDetail = exceptionDetails;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}
