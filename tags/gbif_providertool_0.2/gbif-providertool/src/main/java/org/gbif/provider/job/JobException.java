package org.gbif.provider.job;

/**
 * General exception for all jobs that will result in a rollback of the entire job executed
 * @author markus
 *
 */
public class JobException extends Exception {
	public JobException() {
		super();
	}

	public JobException(String message) {
		super(message);
	}
	
	public JobException(Exception e) {
		super(e);
	}
}
