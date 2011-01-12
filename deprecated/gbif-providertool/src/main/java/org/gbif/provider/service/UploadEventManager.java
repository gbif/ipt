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
package org.gbif.provider.service;

import org.gbif.provider.model.UploadEvent;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface UploadEventManager extends
    GenericResourceRelatedManager<UploadEvent> {
  /**
   * Return a string that represents the upload event statistics for a given
   * resource so that it can be used with the Google Charts API.
   * 
   * @See http://code.google.com/apis/chart/#chart_data
   * @param resourceId
   * @return
   */
  String getGoogleChartData(Long resourceId, int width, int height);

  /**
   * Get all upload events for a given resource.
   * 
   * @param resourceId
   * @return
   */
  List<UploadEvent> getUploadEventsByResource(Long resourceId);

}
