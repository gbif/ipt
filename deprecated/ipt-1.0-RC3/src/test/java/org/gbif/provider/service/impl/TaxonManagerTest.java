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

import java.util.List;

import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonManagerTest /* extends ContextAwareTestBase */{
  @Autowired
  protected TaxonManager taxonManager;
  @Autowired
  protected ChecklistResourceManager checklistResourceManager;

  @Test
  public void testBuildNestedSet() {
    taxonManager.buildNestedSet(Constants.TEST_OCC_RESOURCE_ID);
    taxonManager.buildNestedSet(Constants.TEST_CHECKLIST_RESOURCE_ID);
  }

  @Test
  public void testLookup() {
    taxonManager.lookupParentTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
    taxonManager.lookupAcceptedTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
    taxonManager.lookupBasionymTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
  }

  @Test
  public void testRoots() {
    List<Taxon> rootTaxa = taxonManager
        .getRoots(Constants.TEST_OCC_RESOURCE_ID);
    // System.out.println(rootTaxa);
    // System.out.println(rootTaxa.size());
    // assertTrue(rootTaxa.size() == 1);

    rootTaxa = taxonManager.getRoots(Constants.TEST_CHECKLIST_RESOURCE_ID);
    // System.out.println(rootTaxa);
    // System.out.println(rootTaxa.size());
    // assertTrue(rootTaxa.size() == 1);
    Taxon t = rootTaxa.get(0);
    // assertTrue(t.getLft() == 1 && t.getRgt() == 12);
  }
}
