package org.gbif.provider.tapir;

public abstract class TapirException extends Exception{
	private String message;
	
	public TapirException() {
		super();
	}
	public TapirException(String message) {
		super(message);
		this.message = message;
	}

	public TapirException(String message, Exception e) {
		super(message, e);
		this.message = message;
	}

	public String getTapirMessage(){
		return message;
	}
}
