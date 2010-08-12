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
import org.gbif.ipt.model.ResourceConfiguration;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author markus
 * 
 */
public class SourceManagerImpl extends BaseManager implements SourceManager {

  @Inject
  public SourceManagerImpl() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#add(org.gbif.ipt.model.ResourceConfiguration, java.io.File)
   */
  public void add(ResourceConfiguration config, File file) throws IOException {
    // anaylze using the dwca reader
    Archive arch = ArchiveFactory.openArchive(file);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#add(org.gbif.ipt.model.ResourceConfiguration,
   * org.gbif.ipt.model.Source.SqlSource)
   */
  public void add(ResourceConfiguration config, SqlSource source) throws IOException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.MappingConfigManager#delete(org.gbif.ipt.model.Source.FileSource)
   */
  public void delete(FileSource source) throws IOException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#peek(org.gbif.ipt.model.Source)
   */
  public List<String[]> peek(Source source) {
    // TODO Auto-generated method stub
    return new ArrayList<String[]>();
  }

}
