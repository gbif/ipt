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

package org.gbif.ipt.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import freemarker.cache.FileTemplateLoader;

/**
 * @author markus
 * 
 */
public class DataDirTemplateLoader extends FileTemplateLoader {
  private Logger log = Logger.getLogger(DataDirTemplateLoader.class);

  public DataDirTemplateLoader(File baseDir) throws IOException {
    super(baseDir);
  }

  @Override
  public Object findTemplateSource(String name) throws IOException {
    // only find templates with a datadir:: prefix
    if (name != null && name.startsWith("datadir::")) {
      name = name.substring(9);
      return super.findTemplateSource(name);
    }
    return null;
  }
}