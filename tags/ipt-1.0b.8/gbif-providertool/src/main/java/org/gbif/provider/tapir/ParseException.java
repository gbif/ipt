package org.gbif.provider.tapir;

public class ParseException extends TapirException {
	public ParseException() {
		super("Parsing exception");
	}
	public ParseException(String message) {
		super(message);
	}
	public ParseException(String message, Exception e) {
		super(message, e);
	}

}
