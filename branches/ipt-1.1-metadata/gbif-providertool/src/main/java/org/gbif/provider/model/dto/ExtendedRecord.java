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
package org.gbif.provider.model.dto;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class ExtendedRecord {
  private final CoreRecord core;
  private final Map<Extension, List<ExtensionRecord>> extensionRecords = new HashMap<Extension, List<ExtensionRecord>>();

  public ExtendedRecord(CoreRecord core) {
    super();
    this.core = core;
  }

  public void addExtensionRecord(ExtensionRecord extensionRecord) {
    List<ExtensionRecord> eRecList;
    if (!this.extensionRecords.containsKey(extensionRecord.getExtension())) {
      eRecList = new ArrayList<ExtensionRecord>();
      this.extensionRecords.put(extensionRecord.getExtension(), eRecList);
    } else {
      eRecList = this.extensionRecords.get(extensionRecord.getExtension());
    }
    eRecList.add(extensionRecord);
  }

  public void addExtensionRecords(List<ExtensionRecord> extensionRecords) {
    for (ExtensionRecord eRec : extensionRecords) {
      addExtensionRecord(eRec);
    }
  }

  public void clear() {
    extensionRecords.clear();
  }

  public CoreRecord getCore() {
    return core;
  }

  public List<ExtensionRecord> getExtensionRecords(Extension extension) {
    return extensionRecords.get(extension);
  }

  public List<Extension> getExtensions() {
    return new ArrayList<Extension>(extensionRecords.keySet());
  }

  public boolean hasExtension(Extension extension) {
    return extensionRecords.containsKey(extension);
  }

  public boolean isEmpty() {
    return extensionRecords.isEmpty();
  }

  public int size() {
    int size = 0;
    for (Extension ext : extensionRecords.keySet()) {
      size += extensionRecords.get(ext).size();
    }
    return size;
  }
}
