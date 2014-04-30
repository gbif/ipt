package org.gbif.ipt.service;

/**
 * Exception thrown when the entity requested for modification/deletion doesn't exist.
 */
public class NotExistingException extends Exception {

  public NotExistingException(Class entityClass) {

  }
}
