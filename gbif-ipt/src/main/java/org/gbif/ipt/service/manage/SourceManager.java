package org.gbif.ipt.service.manage;

import org.gbif.utils.file.ClosableIterator;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.manage.impl.SourceManagerImpl;

import com.google.inject.ImplementedBy;
import com.google.inject.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This interface details ALL methods associated with the main resource entity.
 * 
 * The manager keeps a map of the basic metadata and authorisation information in memory, but further details like the
 * full EML or mapping configuration is stored in files and loaded into manager sessions when needed.
 * 
 * @See ResourceManagerSession
 * 
 * @author markus
 */
@ImplementedBy(SourceManagerImpl.class)
public interface SourceManager {

  /**
   * Adds one text file as a file source to a resource configuration.
   * 
   * The file will be analyzed to detect the character encoding and delimiters if not given explicitly in a dwc-a.
   * 
   * @param config the resource configuration to be added to
   * @param file the source file to be added to this resource
   * @param sourceName the preferred sourcename. If null the filename will be used
   * @throws ImportException if the file cant be copied or read
   */
  public FileSource add(Resource resource, File file, @Nullable String sourceName) throws ImportException;

  /**
   * Checks if a source is readable and analyzes its file size, number of rows and other source properties which will be
   * updated. A full analysis might take some time in particular for sql source, so one should use the the quick full=false one as much as possible.
   * 
   * For SQL sources the database connection and number of avilable columns will be checked.
   * 
   * @param source the source to analyze
   * @return problem message if source is not readable
   */
  public String analyze(Source source);

  /**
   * @param source
   * @return list of column names
   */
  public List<String> columns(Source source);

  public boolean delete(Resource resource, Source source);

  /**
   * Imports a darwin core archive (simplest = 1 text file with a header row) to a resource configuration.
   * 
   * When adding a darwin core archive all data files will be added as file sources and also the existing column
   * mappings will be preserved if the concept terms and extension rowtypes are known to this IPT.
   * 
   * All files will analyzed to detect the character encoding and delimiters if not given explicitly in a dwc-a.
   * 
   * @param config the resource configuration to be added to
   * @param file the archive file to be imported. A text file or darwin core archive, either plain or compressed
   * @param overwriteEml if true the metadata found in the archive will overwrite any existing metadata, otherwise it
   *        will be ignored
   * @throws IOException
   */
  public int importArchive(Resource resource, File file, boolean overwriteEml) throws ImportException;

  /**
   * Retrieves a set of unique string values used in a given column of a source.
   * The maximum number of distinct values can be restricted.
   * 
   * @param source
   * @param column column to inspect, zero based numbering as used in the dwc archives
   * @param maxValues maximun number of distinct values to return. If zero or negative all values will be retrieved.
   * @param maxRows maximum number of rows to inspect. If zero or negative all rows will be scanned.
   * @return unique values found in the column
   * @throws Exception
   */
  public Set<String> inspectColumn(Source source, int column, int maxValues, int maxRows) throws SourceException;

  /**
   * @param source
   * @param rows number of rows to return
   * @return sample rows from the dataset
   */
  public List<String[]> peek(Source source, int rows);

  /**
   * @param src
   * @return a closable row iterator for a source
   * @throws SourceException
   */
  public ClosableIterator<String[]> rowIterator(Source src) throws SourceException;

}
