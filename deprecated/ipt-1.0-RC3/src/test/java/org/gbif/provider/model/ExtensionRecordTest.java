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
package org.gbif.provider.model;

import org.gbif.provider.model.dto.ExtensionRecord;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionRecordTest {

  @Test
  public void testIterator() {
    ExtensionRecord extRec = new ExtensionRecord(5324L, 1L);
    extRec.setPropertyValue(new ExtensionProperty("alberto:the:great"), "hallo");
    extRec.setPropertyValue(new ExtensionProperty("alberto:the:medium"),
        "servus");
    extRec.setPropertyValue(new ExtensionProperty("alberto:the:small"),
        "bonjour");

    for (ExtensionProperty p : extRec) {
      // System.out.println(p);
    }
  }

}
