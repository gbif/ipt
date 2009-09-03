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
package org.gbif.provider.datasource;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;

import java.util.Iterator;

/**
 * TODO: Documentation.
 * 
 */
public interface ImportSource extends Iterator<ImportRecord>,
    Iterable<ImportRecord> {
  void close();

  void init(DataResource resource, ExtensionMapping view)
      throws ImportSourceException;
}
