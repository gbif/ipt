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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.ChecklistResourceManager;
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
public class ChecklistUploadTest extends ContextAwareTestBase {
  @Autowired
  @Qualifier("checklistUploadTask")
  private Task<UploadEvent> uploadTask;
  @Autowired
  private ChecklistResourceManager checklistResourceManager;

  @Test
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public void testUpload() throws Exception {
    ChecklistResource res = checklistResourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
    res.setNumTaxa(0);
    checklistResourceManager.save(res);
    checklistResourceManager.flush();

    uploadTask.init(Constants.TEST_CHECKLIST_RESOURCE_ID);
    UploadEvent event = uploadTask.call();
    res = (ChecklistResource) event.getResource();
    checklistResourceManager.save(res);

    res = checklistResourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
    assertEquals(42, res.getNumTaxa());
    assertEquals(6, res.getNumAccepted());
    assertEquals(36, res.getNumSynonyms());
    assertEquals(1, res.getNumFamilies());
    assertEquals(19, res.getNumSpecies());
    assertEquals(2, res.getNumTerminalTaxa());

    assertEquals(42, event.getRecordsUploaded());
    assertEquals(0, event.getRecordsErroneous());
    // assertEquals(0, event.getRecordsChanged());
    // assertEquals(42, event.getRecordsAdded());
    // assertEquals(42, event.getRecordsDeleted());
  }

}
