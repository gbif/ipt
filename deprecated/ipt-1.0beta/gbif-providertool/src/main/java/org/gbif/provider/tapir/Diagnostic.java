package org.gbif.provider.tapir;

import java.util.Date;

public class Diagnostic {
	private final Severity severity;
	private final Date time;
	private final String text;
	
	public Diagnostic(Severity severity, Date time, String text) {
		super();
		this.text = text;
		this.severity = severity;
		this.time = time;
	}

	public Severity getSeverity() {
		return severity;
	}

	public Date getTime() {
		return time;
	}

	public String getText() {
		return text;
	}
	
}
