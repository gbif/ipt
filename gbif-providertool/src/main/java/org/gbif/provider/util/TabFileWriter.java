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
package org.gbif.provider.util;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Record;
import org.gbif.provider.model.dto.ExtensionRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class TabFileWriter {
  public static final String ID_COLUMN_NAME = "dc:identifier";
  public static final String LINK_COLUMN_NAME = "dc:source";
  public static final String MODIFIED_COLUMN_NAME = "dc:modified";

  public static String valToString(Object val) {
    String str = "";
    if (val != null) {
      str = val.toString();
      str = str.replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll(
          "\\r", "\\\\r");
    }
    return str;
  }

  private final BufferedWriter writer;
  private final List<ExtensionProperty> header;
  private final File file;

  private final String guidPropertyName;

  public TabFileWriter(File file, ExtensionMapping view) throws IOException {
    this.header = view.getMappedProperties();
    this.file = file;
    writer = new BufferedWriter(new FileWriter(file));
    // write headers to tabfile
    writer.write(valToString(ID_COLUMN_NAME));
    writer.append("\t");
    writer.write(valToString(LINK_COLUMN_NAME));
    writer.append("\t");
    writer.write(valToString(MODIFIED_COLUMN_NAME));
    writer.append("\t");
    guidPropertyName = view.getResource().getDwcGuidPropertyName();
    writer.write(valToString("dwc:" + guidPropertyName));
    writePropertyHeader();
  }

  public void close() throws IOException {
    writer.flush();
    writer.close();
  }

  public File getFile() {
    return file;
  }

  /**
   * write a line of tab seperated map values identified by the column header
   * name given as the map key In order specified by the header list
   * 
   * @param ln
   * @throws IOException
   */
  public void write(CoreRecord rec) throws IOException {
    writer.write(valToString(rec.getCoreId()));
    writer.append("\t");
    writer.write(valToString(rec.getLink()));
    writer.append("\t");
    writer.write(valToString(rec.getDateModified()));
    writer.append("\t");
    writer.write(valToString(rec.getGuid()));
    writeRecordProperties(rec);
  }

  public void write(ExtensionRecord rec) throws IOException {
    writer.write(valToString(rec.getCoreId()));
    writeRecordProperties(rec);
  }

  private void writePropertyHeader() throws IOException {
    for (ExtensionProperty prop : header) {
      if (prop.getName().equals(guidPropertyName)) {
        continue;
      }
      writer.append("\t");
      writer.write(prop.getName());
    }
    writer.newLine();
  }

  private void writeRecordProperties(Record rec) throws IOException {
    for (ExtensionProperty prop : header) {
      if (prop.getName().equals(guidPropertyName)) {
        continue;
      }
      writer.append("\t");
      writer.write(valToString(rec.getPropertyValue(prop)));
    }
    writer.newLine();
  }

}
