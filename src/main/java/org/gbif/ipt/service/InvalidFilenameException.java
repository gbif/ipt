package org.gbif.ipt.service;

/**
 * Exception thrown when uploading a file whose name contains an illegal character.
 */
public class InvalidFilenameException extends Exception {

  public InvalidFilenameException(String message) {
    super(message);
  }
}
