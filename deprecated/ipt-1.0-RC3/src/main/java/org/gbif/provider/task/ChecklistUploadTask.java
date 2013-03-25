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
package org.gbif.provider.task;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.TaxonManager;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 * Tha main task responsible for uploading raw data into the cache and doing
 * simple preprocessing while iterating through the ImportSource. Any further
 * secondary postprocessing is done with the help of a second post processing
 * task that this task will automatically schedule once its done.
 * 
 */
public class ChecklistUploadTask extends ImportTask<ChecklistResource> {
  public static final int TASK_TYPE_ID = 7;
  // resource stats
  private final ChecklistResourceManager checklistResourceManager;

  @Autowired
  private ChecklistUploadTask(TaxonManager taxonManager,
      ChecklistResourceManager checklistResourceManager) {
    super(checklistResourceManager);
    this.checklistResourceManager = checklistResourceManager;
  }

  public int taskTypeId() {
    return TASK_TYPE_ID;
  }

  @Override
  protected void closeHandler(ChecklistResource resource) {
    currentActivity = "Building resource stats";
    checklistResourceManager.setResourceStats(resource);

    currentActivity = "Creating TCS data archive";
    try {
      File tcs = checklistResourceManager.writeTcsArchive(getResourceId());
    } catch (IOException e) {
      log.error("Couldnt write TCS archive", e);
      this.annotationManager.annotateResource(resource,
          "Could not write TCS archive. IOException");
    }
  }

  @Override
  protected void extensionRecordHandler(ExtensionRecord extRec) {
    // nothing to do
  }

  @Override
  protected void prepareHandler(ChecklistResource resource) {
    // nothing to do
  }

  @Override
  protected void recordHandler(DarwinCore record) {
  }

  @Override
  protected String statusHandler() {
    return "";
  }

}
