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

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * TODO: Documentation.
 * 
 */
public class H2InsertSpeed extends ContextAwareTestBase {
  @Autowired
  protected DarwinCoreManager darwinCoreManager;
  @Autowired
  private OccResourceManager occResourceManager;

  @Test
  public void testDwcInserts() {
    OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
    int i = 0;
    DarwinCore dwc;
    Date start = new Date();
    // while (i < 100) {
    // dwc = DarwinCore.newMock(res);
    // dwc = darwinCoreManager.save(dwc);
    // i++;
    // }
    darwinCoreManager.flush();
    Date end = new Date();
    System.out.println("Inserting " + i + " dwc records took "
        + (end.getTime() - start.getTime()) + " milliseconds.");
  }
}
