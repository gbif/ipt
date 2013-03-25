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
package org.gbif.provider.service.impl;

import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.ResourceTestBase;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class FullTextSearchManagerLuceneTest extends ResourceTestBase {
  @Autowired
  private FullTextSearchManager fullTextSearchManager;

  @Test
  public void setResourceMetadata() throws Exception {
    fullTextSearchManager.buildResourceIndex();
    List<String> resourceIDs = fullTextSearchManager.search("subalpi*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("Subalpine*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("belt*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("belt");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("mountains*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("Toros*");
    assertTrue(resourceIDs.size() >= 1);
    // resourceIDs = fullTextSearchManager.search("Sites loc*");
    // assertTrue(resourceIDs.size()>=1);
    resourceIDs = fullTextSearchManager.search("Frei*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("frei*");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("Sites*");
    assertTrue(resourceIDs.size() >= 1);
  }

  @Test
  public void testFullTextMetadata() throws Exception {
    fullTextSearchManager.buildResourceIndex();
    List<String> resourceIDs = fullTextSearchManager.search("Pontaurus");
    assertTrue(resourceIDs.size() >= 1);
    resourceIDs = fullTextSearchManager.search("Berlin");
    assertTrue(resourceIDs.size() >= 1);
  }

}
