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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ChecklistResourceManagerHibernateTest extends ContextAwareTestBase {
  @Autowired
  private ChecklistResourceManager resourceManager;

  @Test
  public void testStats() {
    ChecklistResource res = resourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
    resourceManager.setResourceStats(res);
    assertTrue(res.getNumTaxa() == 42);
    assertTrue(res.getNumGenera() == 2);
  }
}
