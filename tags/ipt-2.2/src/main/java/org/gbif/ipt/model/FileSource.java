package org.gbif.ipt.model;

import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A file based data source for the IPT.
 */
public interface FileSource extends Source {

  File getFile();

  void setFile(File file);

  Date getLastModified();

  void setLastModified(Date lastModified);

  long getFileSize();

  int getRows();

  String getPreferredFileSuffix();

  /**
   * @return list of skipped, empty rows
   */
  Set<Integer> analyze() throws IOException;

  ClosableReportingIterator<String[]> rowIterator();

  List<String> columns();
}
