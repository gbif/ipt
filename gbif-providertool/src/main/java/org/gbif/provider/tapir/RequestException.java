package org.gbif.provider.tapir;

public class RequestException extends TapirException {
	public RequestException() {
		super("Invalid request with illegal arguments");
	}
	public RequestException(String message) {
		super(message);
	}
	public RequestException(String message, Exception e) {
		super(message, e);
	}

}
