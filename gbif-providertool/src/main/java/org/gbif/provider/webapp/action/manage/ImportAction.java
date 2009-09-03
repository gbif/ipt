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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class ImportAction extends BaseDataResourceAction implements Preparable {
  private static final String BUSY = "resource-busy";
  private static final String READY = "resource-ready";
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private UploadEventManager uploadEventManager;
  private String status;
  private boolean busy = false;
  private List<UploadEvent> uploadEvents;
  private String gChartData;

  @Override
  public String cancel() {
    cacheManager.cancelUpload(resourceId);
    saveMessage(getText("upload.cancelled"));
    return SUCCESS;
  }

  @Override
  public String execute() {
    // load resource
    super.prepare();
    if (resource == null) {
      return RESOURCE404;
    }
    // create GoogleChart string
    gChartData = uploadEventManager.getGoogleChartData(resourceId, 400, 200);
    return SUCCESS;
  }

  public String getGChartData() {
    return gChartData;
  }

  public String getStatus() {
    return status;
  }

  public List<UploadEvent> getUploadEvents() {
    return uploadEvents;
  }

  public String history() {
    uploadEvents = uploadEventManager.getUploadEventsByResource(resourceId);
    return SUCCESS;
  }

  public boolean isBusy() {
    return busy;
  }

  @Override
  public void prepare() {
    if (resourceId != null) {
      busy = cacheManager.isBusy(resourceId);
    }
  }

  public String status() {
    super.prepare();
    if (resource == null) {
      return RESOURCE404;
    }
    status = cacheManager.getUploadStatus(resourceId);
    if (busy) {
      return BUSY;
    } else {
      return READY;
    }
  }

  public String upload() {
    // run task in different thread
    cacheManager.runUpload(resourceId);
    return SUCCESS;
  }

}
