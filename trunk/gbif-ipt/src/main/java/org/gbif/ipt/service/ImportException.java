package org.gbif.ipt.service;

/**
 * Exception thrown when adding a file or importing a dwc archive has failed
 * 
 * @author markus
 * 
 */
public class ImportException extends Exception {

  public ImportException(String message) {
    super(message);
  }

  public ImportException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportException(Throwable cause) {
    super(cause);
  }

}
