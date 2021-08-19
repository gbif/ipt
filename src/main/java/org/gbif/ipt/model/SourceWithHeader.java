package org.gbif.ipt.model;

/**
 * Interface for sources that may have header rows.
 */
public interface SourceWithHeader {

  /**
   * @return number of lines to be ignored
   */
  int getIgnoreHeaderLines();
}
