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
package org.gbif.provider.upload;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.task.Task;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO: Documentation.
 * 
 */
public class OccUploadTest extends ContextAwareTestBase {
  @Autowired
  @Qualifier("occUploadTask")
  private Task<UploadEvent> uploadTask;
  @Autowired
  private OccResourceManager occResourceManager;

  @Test
  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  public void testUpload() throws Exception {
    OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
    res.setNumTaxa(0);
    res.setNumFamilies(0);
    res.setNumCountries(0);
    res.setBbox(new BBox());
    occResourceManager.save(res);
    occResourceManager.flush();
    res = null;
    uploadTask.init(Constants.TEST_OCC_RESOURCE_ID);
    UploadEvent event = uploadTask.call();
    res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
    assertEquals(894, res.getNumTaxa());
    assertEquals(45, res.getNumFamilies());
    assertEquals(1, res.getNumCountries());
    assertEquals("36.538,26.851 38.107,35.152", res.getBbox().toStringShort(3));

    // assertEquals(1533, event.getRecordsAdded());
    // assertEquals(0, event.getRecordsChanged());
    assertEquals(1533, event.getRecordsDeleted());
    assertEquals(1533, event.getRecordsUploaded());
    assertEquals(0, event.getRecordsErroneous());
  }

}
