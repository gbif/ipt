/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.datasource.impl;

import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;

/**
 * TODO: Documentation.
 * 
 */
public class ImportSourceFactoryImpl implements ImportSourceFactory {

  public ImportSource newInstance(DataResource resource, ExtensionMapping view)
      throws ImportSourceException {
    ImportSource src;
    if (resource == null || resource.getId() == null || view == null
        || view.getCoreIdColumn() == null) {
      throw new IllegalArgumentException();
    }
    if (view.isMappedToFile()) {
      src = newFileImportSource();
    } else {
      src = newSqlImportSource();
    }
    // init with resource, view
    src.init(resource, view);
    return src;
  }

  /**
   * overridden by spring to inject prototype beans
   * 
   * @return
   */
  protected FileImportSource newFileImportSource() {
    return null;
  }

  /**
   * overridden by spring to inject prototype beans
   * 
   * @return
   */
  protected SqlImportSource newSqlImportSource() {
    return null;
  }
}
