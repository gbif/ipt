package org.gbif.provider.service;

public class RegistryException extends Exception {

	public RegistryException() {
		super();
	}
	public RegistryException(String string) {
		super(string);
	}
	public RegistryException(String string, Exception e) {
		super(string, e);
	}

}
