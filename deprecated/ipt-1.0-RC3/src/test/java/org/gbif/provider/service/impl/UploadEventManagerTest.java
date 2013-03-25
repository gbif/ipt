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

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class UploadEventManagerTest extends ContextAwareTestBase {
  protected UploadEventManager uploadEventManager;

  public void setUploadEventManager(UploadEventManager uploadEventManager) {
    this.uploadEventManager = uploadEventManager;
  }

  @Test
  public void testGetGoogleChartData() {
    System.out.println(uploadEventManager.getGoogleChartData(1L, 450, 200));
    System.out.println(uploadEventManager.getGoogleChartData(321L, 450, 200));
  }

  @Test
  public void testGetUploadEventsByResource() {
    try {
      List<UploadEvent> events = uploadEventManager.getUploadEventsByResource(Constants.TEST_OCC_RESOURCE_ID);
      for (UploadEvent ev : events) {
        logger.debug(ev);
      }
    } catch (ObjectRetrievalFailureException e) {
      logger.debug(e.getMessage());
    }
  }
}
