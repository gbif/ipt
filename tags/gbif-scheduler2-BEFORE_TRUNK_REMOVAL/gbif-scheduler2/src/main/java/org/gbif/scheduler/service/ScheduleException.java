/**
 * 
 */
package org.gbif.scheduler.service;

/**
 * Indicates an "application logic" problem scheduling
 * - e.g. can't delete this job since it is currently running
 * Will not indicate errors in DB connection, IO etc
 * @author timrobertson
 */
public class ScheduleException extends Exception {
	private static final long serialVersionUID = -1454018712953341575L;

	public ScheduleException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ScheduleException(String arg0) {
		super(arg0);
	}
}
