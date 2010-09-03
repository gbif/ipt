package org.gbif.ipt.task;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

public class TaskMessage {
	public final Level level;
	public final String message;
	public final String[] params;
	
	public TaskMessage(Level level, String message) {
		super();
		this.level = level;
		this.message = message;
		this.params = new String[0];
	}

	public TaskMessage(Level level, String message, String[] params) {
		super();
		this.level = level;
		this.message = message;
		this.params = params;
	}
	
}
