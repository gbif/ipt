package org.gbif.ipt.model;

/**
 * A data source for mappings, exposing a record/row iterator through the SourceManager implementation.
 */
public interface Source {

  int getColumns();

  String getDateFormat();

  String getEncoding();

  String getName();

  Resource getResource();

  boolean isFileSource();

  boolean isExcelSource();

  boolean isSqlSource();

  boolean isReadable();

  void setColumns(int columns);

  void setDateFormat(String dateFormat);

  void setEncoding(String encoding);

  void setName(String name);

  void setReadable(boolean readable);

  void setResource(Resource resource);
}
