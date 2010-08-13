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
    // TODO Auto-generated constructor stub
  }

  public ImportException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public ImportException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

}
