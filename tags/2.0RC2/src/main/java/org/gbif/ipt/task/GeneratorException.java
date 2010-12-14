package org.gbif.ipt.task;

/**
 * Exception thrown when generating a dwc archive fails
 * 
 * @author markus
 * 
 */
public class GeneratorException extends Exception {

  public GeneratorException(String message) {
    super(message);
  }

  public GeneratorException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeneratorException(Throwable cause) {
    super(cause);
  }

}
