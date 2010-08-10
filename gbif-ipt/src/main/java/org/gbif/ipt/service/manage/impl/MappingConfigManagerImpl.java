/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.MappingConfiguration;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.manage.MappingConfigManager;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author markus
 * 
 */
public class MappingConfigManagerImpl extends BaseManager implements MappingConfigManager {
  public static final String PERSISTENCE_FILE = "config.xml";
  private final XStream xstream = new XStream();
  private final UserEmailConverter userConverter;
  private final ExtensionRowTypeConverter extensionConverter;

  @Inject
  public MappingConfigManagerImpl(UserEmailConverter userConverter, ExtensionRowTypeConverter extensionConverter) {
    super();
    this.userConverter = userConverter;
    this.extensionConverter = extensionConverter;
    defineXstreamMapping();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#add(org.gbif.ipt.model.ResourceConfiguration, java.io.File)
   */
  public void add(MappingConfiguration config, File file) throws IOException {
    // anaylze using the dwca reader
    Archive arch = ArchiveFactory.openArchive(file);
  }

  private void defineXstreamMapping() {
    xstream.alias("config", MappingConfiguration.class);
    xstream.omitField(MappingConfiguration.class, "resource");
    // persist only emails for users
    xstream.registerConverter(userConverter);
    // persist only rowtype for extensions
    xstream.registerConverter(extensionConverter);
    xstream.alias("user", User.class);
    xstream.alias("filesource", FileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#delete(org.gbif.ipt.model.Source.FileSource)
   */
  public void delete(FileSource source) throws IOException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#get(org.gbif.ipt.model.Resource)
   */
  public MappingConfiguration get(Resource resource) {
    MappingConfiguration config = null;
    File cfgFile = dataDir.resourceFile(resource, PERSISTENCE_FILE);
    if (!cfgFile.exists()) {
      config = new MappingConfiguration();
      config.setResource(resource);
      log.debug("Created new mapping configuration for " + resource);
    } else {
      InputStream input;
      try {
        input = new FileInputStream(cfgFile);
        config = (MappingConfiguration) xstream.fromXML(input);
        config.setResource(resource);
        log.debug("Loaded mapping configuration for " + resource);
      } catch (FileNotFoundException e) {
        log.error("Cannot load mapping configuration for " + resource, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot load mapping configuration for " + resource
            + ": " + e.getMessage());
      }
    }
    return config;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#peek(org.gbif.ipt.model.Source)
   */
  public List<String[]> peek(Source source) {
    // TODO Auto-generated method stub
    return new ArrayList<String[]>();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#save(org.gbif.ipt.model.ResourceConfiguration)
   */
  public void save(MappingConfiguration config) throws IOException {
    File cfgFile = dataDir.resourceFile(config.getResource(), PERSISTENCE_FILE);
    try {
      // persist data
      Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
      xstream.toXML(config, writer);
      log.debug("Saved " + config);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant write mapping configuration");
    }
  }

}
