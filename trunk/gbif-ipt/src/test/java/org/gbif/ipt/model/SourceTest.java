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

package org.gbif.ipt.model;

import org.gbif.ipt.model.Source.FileSource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author markus
 * 
 */
public class SourceTest {
  @Test
  public void testName() {
    Source src = new FileSource();
    src.setName("Peter");
    assertEquals("peter", src.getName());

    src.setName(" Peter nice");
    assertEquals("peternice", src.getName());

    src.setName("verNAcÜl.:s");
    assertEquals("vernacül", src.getName());

    src.setName("veraculars.txt");
    assertEquals("veraculars", src.getName());

    src.setName("veraculars.my.txt");
    assertEquals("veracularsmy", src.getName());
  }
}
