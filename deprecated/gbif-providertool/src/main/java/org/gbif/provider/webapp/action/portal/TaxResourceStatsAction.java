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
package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.ChecklistResourceManager;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * ActionClass to generate the data for a single occurrence resource statistic
 * with chart image and data Can be parameterized with: "zoom" : return the
 * largest image possible if true. Defaults to false "title" : set title in
 * image? Defaults to false
 */
public class TaxResourceStatsAction extends
    ResourceStatsBaseAction<ChecklistResource> implements Preparable {
  private final ChecklistResourceManager checklistResourceManager;

  @Autowired
  public TaxResourceStatsAction(
      ChecklistResourceManager checklistResourceManager) {
    this.resourceManager = checklistResourceManager;
    this.checklistResourceManager = checklistResourceManager;
  }

  public String statsByRank() {
    recordAction = "taxListByRank";
    if (!useCachedImage(ImageType.ChartByRank)) {
      data = checklistResourceManager.taxByRank(resourceId);
      String url = checklistResourceManager.taxByRankPieUrl(data, width,
          height, title);
      cacheImage(ImageType.ChartByRank, url);
    }
    return PIE_RESULT;
  }

  public String statsByStatus() {
    recordAction = "taxListByStatus";
    types = StatusType.values();
    if (!useCachedImage(ImageType.ChartByStatus)) {
      StatusType st = StatusType.getByInt(type);
      data = checklistResourceManager.taxByStatus(resourceId, st);
      String url = checklistResourceManager.taxByStatusPieUrl(data, st, width,
          height, title);
      cacheImage(ImageType.ChartByStatus, url);
    }
    return PIE_RESULT;
  }

  public String statsByTaxon() {
    recordAction = "taxDetail";
    types = Rank.COMMON_RANKS.toArray(new Object[1]);

    if (!useCachedImage(ImageType.ChartByTaxon)) {
      Rank rnk = Rank.getByInt(type);
      data = checklistResourceManager.taxByTaxon(resourceId, rnk);
      String url = checklistResourceManager.taxByTaxonPieUrl(data, rnk, width,
          height, title);
      cacheImage(ImageType.ChartByTaxon, url);
    }
    return PIE_RESULT;
  }

}