package org.gbif.ipt.model;

import org.gbif.utils.file.ClosableReportingIterator;

/**
 * Interface for sources that can be iterated over rows.
 */
public interface RowIterable {

  /**
   * Produces iterator to iterate over source's rows
   *
   * @return row iterator
   */
  ClosableReportingIterator<String[]> rowIterator();
}
