package org.gbif.provider.datasource;

/**
 * Exception thrown when import source cant be created or read.
 * Abstracts IOException or SQLException or any other upcoming source problems
 * @author markus
 *
 */
public class ImportSourceException extends Exception {

	public ImportSourceException() {
		super();
	}

	public ImportSourceException(String message) {
		super(message);
	}

	public ImportSourceException(Exception e) {
		super(e);
	}

	public ImportSourceException(String message, Exception e) {
		super(message, e);
	}
}
