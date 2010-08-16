package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.ResourceConfiguration;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.impl.SourceManagerImpl;

import com.google.inject.ImplementedBy;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
   * @param file the source file to be added to this resource config
   * @throws ImportException if the file cant be copied or read
   */
  public FileSource add(ResourceConfiguration config, File file) throws ImportException;

  /**
   * Checks if a source is readable and analyzes its file size, number of rows and other source properties which will be
   * updated.
   * For SQL sources the database connection and number of avilable columns will be checked.
   * 
   * @param source
   */
  public void analyze(Source source);

  public boolean delete(ResourceConfiguration config, Source source);

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
  public int importArchive(ResourceConfiguration config, File file, boolean overwriteEml) throws ImportException;

  public List<String[]> peek(Source source);

}
