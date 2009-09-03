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

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.TermMapping;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.TermMappingManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResourceTestBase;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public class TermMappingManagerTest extends ResourceTestBase {
  @Autowired
  private SourceInspectionManager sourceInspectionManager;
  @Autowired
  private ExtensionManager extensionManager;
  @Autowired
  private ExtensionPropertyManager extensionPropertyManager;
  @Autowired
  private TermMappingManager termMappingManager;

  @Test
  @Transactional(propagation = Propagation.REQUIRED)
  public void testDistinctTerms() {
    setupOccResource();
    ExtensionProperty prop = extensionPropertyManager.getCorePropertyByQualName(Constants.SCIENTIFIC_NAME_QUALNAME);
    try {
      Set<String> terms = sourceInspectionManager.getDistinctValues(
          resource.getCoreMapping().getSource(),
          resource.getCoreMapping().getMappedProperty(prop).getColumn());
      assertTrue(terms.size() > 500);
      // List<String> t2 = new ArrayList<String>(terms);
      // Collections.sort(t2);
      // System.out.println(t2);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testGetMappingMap() {
    Map<String, String> terms = termMappingManager.getMappingMap(7L);
  }

  @Test
  public void testTermMappings() {
    List<TermMapping> terms = termMappingManager.getTermMappings(7L);
  }

}
