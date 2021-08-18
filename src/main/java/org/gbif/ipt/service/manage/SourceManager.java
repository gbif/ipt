package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.manage.impl.SourceManagerImpl;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;

/**
 * This interface details all methods associated with a source entity.
 */
@ImplementedBy(SourceManagerImpl.class)
public interface SourceManager {

  /**
   * Adds one text or excel file as a file source to a resource configuration.
   * The file will be analyzed to detect the character encoding and delimiters if not given explicitly in a dwc-a.
   *
   * @param resource   resource
   * @param file       the text or excel source file to be added to this resource
   * @param sourceName the preferred sourcename. If null the filename will be used
   *
   * @return file or excel source that has been added
   *
   * @throws ImportException if the file cant be copied or read
   * @throws InvalidFilenameException if the source filename contained illegal characters
   */
  FileSource add(Resource resource, File file, @Nullable String sourceName)
    throws ImportException, InvalidFilenameException;

  /**
   * Adds one text file as a URL source to a resource configuration.
   * The file will be analyzed to detect the character encoding and delimiters.
   *
   * @param resource   resource
   * @param url        the text source file (URL) to be added to this resource
   *
   * @return URL source that has been added
   *
   * @throws ImportException if the file can't be copied or read
   */
  UrlSource add(Resource resource, URI url) throws ImportException;

  /**
   * Checks if a source is readable and analyzes its file size, number of rows and other source properties which will
   * be updated. A full analysis might take some time in particular for sql source, so one should use the the quick
   * full=false one as much as possible. For SQL sources the database connection and number of avilable columns will be
   * checked.
   *
   * @param source the source to analyze
   *
   * @return problem message if source is not readable
   */
  String analyze(Source source);

  /**
   * Return list of source's column names.
   *
   * @param source source
   *
   * @return list of column names
   */
  List<String> columns(Source source);

  /**
   * Delete source from resource.
   *
   * @param resource resource
   * @param source   source
   *
   * @return true if the source was deleted
   */
  boolean delete(Resource resource, Source source);

  /**
   * Retrieves a set of unique string values used in a given column of a source.
   * The maximum number of distinct values can be restricted.
   *
   * @param source    source
   * @param column    column to inspect, zero based numbering as used in the dwc archives
   * @param maxValues maximun number of distinct values to return. If zero or negative all values will be retrieved.
   * @param maxRows   maximum number of rows to inspect. If zero or negative all rows will be scanned.
   *
   * @return unique values found in the column
   */
  Set<String> inspectColumn(Source source, int column, int maxValues, int maxRows) throws SourceException;

  /**
   * Return sample rows from the dataset.
   *
   * @param source source
   * @param rows   number of rows to return
   *
   * @return sample rows from the dataset
   */
  List<String[]> peek(Source source, int rows);

  /**
   * Create a ClosableReportingIterator iterator for a source.
   *
   * @param source source
   *
   * @return a ClosableReportingIterator for a source
   */
  ClosableReportingIterator<String[]> rowIterator(Source source) throws SourceException;

}
