package org.gbif.provider.tapir;

public abstract class TapirException extends Exception{
	private String message;
	
	public TapirException(String message) {
		super();
		this.message = message;
	}

	public String getTapirMessage(){
		return message;
	}
}
