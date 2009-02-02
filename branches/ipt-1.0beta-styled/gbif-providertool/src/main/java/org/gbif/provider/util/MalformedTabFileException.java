package org.gbif.provider.util;

public class MalformedTabFileException extends Exception {
	public MalformedTabFileException() {
		super();
	}

	public MalformedTabFileException(String message) {
		super(message);
	}

	public MalformedTabFileException(Exception e) {
		super(e);
	}

	public MalformedTabFileException(String message, Exception e) {
		super(message, e);
	}

}
