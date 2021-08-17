package org.gbif.ipt.service;

/**
 * Exception thrown when uploading a file which is not a text file (txt, tsv, csv).
 */
public class NotTextFileException extends Exception {

  public NotTextFileException(String message) {
    super(message);
  }
}
