package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.MappingConfiguration;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.service.manage.impl.MappingConfigManagerImpl;

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
@ImplementedBy(MappingConfigManagerImpl.class)
public interface MappingConfigManager {

  /**
   * Adds either a text file or darwin core archive to a resource configuration.
   * 
   * For a single text file a new file source will be created.
   * 
   * When adding a darwin core archive all data files will be added as file sources and also the existing coumn mappings
   * will be preserved if the concept terms and extensipn rowtypes are known to this IPT.
   * 
   * All files will analyzed to detect the character encoding and delimiters if not given explicitly in a dwc-a.
   * 
   * @param config the resource configuration to be added to
   * @param file the source file to be added to this resource config. A text file or darwin core archive, either plain
   *        or compressed
   * @throws IOException
   */
  public void add(MappingConfiguration config, File file) throws IOException;

  public void delete(FileSource source) throws IOException;

  public MappingConfiguration get(Resource resource);

  public List<String[]> peek(Source source);

  public void save(MappingConfiguration config) throws IOException;

}
