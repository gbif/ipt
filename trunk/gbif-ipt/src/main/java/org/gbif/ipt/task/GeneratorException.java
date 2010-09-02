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
    // TODO Auto-generated constructor stub
  }

  public GeneratorException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public GeneratorException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

}
