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

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileReader;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Import source for relational databases that maps a sql resultset into
 * CoreRecords and allows to iterate over them.
 * 
 */
public class FileImportSource extends ImportSourceBase {
  private TabFileReader reader;
  private String[] currentLine;
  // key=header column name
  private final Map<String, Integer> headerMap = new HashMap<String, Integer>();
  @Autowired
  private SourceInspectionManager sourceInspectionManager;

  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Long getResourceId() {
    return resourceId;
  }

  public boolean hasNext() {
    return reader.hasNext();
  }

  @Override
  public void init(DataResource resource, ExtensionMapping view)
      throws ImportSourceException {
    super.init(resource, view);

    if (!(view.getSource() instanceof SourceFile)) {
      throw new IllegalArgumentException(
          "View needs to have a source of type SourceFile");
    }
    SourceFile src = (SourceFile) view.getSource();
    // try to setup FileReader
    try {
      Integer i = 0;
      for (String h : sourceInspectionManager.getHeader(src)) {
        this.headerMap.put(h, i);
        i++;
      }
      this.reader = new TabFileReader(AppConfig.getResourceSourceFile(
          resource.getId(), src.getFilename()), !src.hasHeaders());
    } catch (Exception e) {
      throw new ImportSourceException("Cant read source file "
          + src.getFilename(), e);
    }
  }

  public Iterator<ImportRecord> iterator() {
    return this;
  }

  public ImportRecord next() {
    ImportRecord row = null;
    if (hasNext()) {
      currentLine = reader.next();
      try {
        row = new ImportRecord(resourceId, getCurrentValue(coreIdColumn));
        // TODO: the mapping that takes place here should probably be done with
        // a separate mapping class
        //
        if (guidColumn != null) {
          row.setGuid(getCurrentValue(guidColumn));
        }
        if (linkColumn != null) {
          if (linkTemplate != null
              && linkTemplate.contains(ExtensionMapping.TEMPLATE_ID_PLACEHOLDER)) {
            row.setLink(linkTemplate.replace(
                ExtensionMapping.TEMPLATE_ID_PLACEHOLDER,
                StringUtils.trimToEmpty(getCurrentValue(linkColumn))));
          } else {
            row.setLink(getCurrentValue(linkColumn));
          }
        }
        for (PropertyMapping pm : properties) {
          if (StringUtils.trimToNull(pm.getColumn()) != null) {
            String column = pm.getColumn();
            String val = getCurrentValue(column);
            // lookup value in term mapping map
            if (vocMap.containsKey(column)) {
              if (vocMap.get(column).containsKey(val)) {
                val = vocMap.get(column).get(val);
              }
            }
            row.setPropertyValue(pm.getProperty(), val);
          } else if (StringUtils.trimToNull(pm.getValue()) != null) {
            row.setPropertyValue(pm.getProperty(), pm.getValue());
          }
        }
      } catch (Exception e) {
        log.warn("FileImportSource empty row because of error", e);
        row = null;
      }
    }
    return row;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private String getCurrentValue(String columnName) {
    if (headerMap.containsKey(columnName)) {
      return escapeRawValue(currentLine[headerMap.get(columnName)]);
    } else {
      return null;
    }
  }

}
