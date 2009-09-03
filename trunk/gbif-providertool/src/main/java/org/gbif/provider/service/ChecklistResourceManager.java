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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface ChecklistResourceManager extends
    GenericResourceManager<ChecklistResource> {

  ChecklistResource setResourceStats(ChecklistResource resource);

  List<StatsCount> taxByRank(Long resourceId);

  String taxByRankPieUrl(List<StatsCount> data, int width, int height,
      boolean title);

  List<StatsCount> taxByStatus(Long resourceId, StatusType type);

  String taxByStatusPieUrl(List<StatsCount> data, StatusType type, int width,
      int height, boolean title);

  List<StatsCount> taxByTaxon(Long resourceId, Rank rnk);

  String taxByTaxonPieUrl(List<StatsCount> data, Rank rnk, int width,
      int height, boolean title);

  File writeTcsArchive(Long resourceId) throws IOException;

}
