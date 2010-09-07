package org.gbif.ipt.service;

import java.io.IOException;

/**
 * Exception thrown when a source (file or sql) cannot be read
 * 
 * @author markus
 * 
 */
public class SourceException extends IOException {

  public SourceException(String message) {
    super(message);
  }

  public SourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public SourceException(Throwable cause) {
    super(cause);
  }

}
