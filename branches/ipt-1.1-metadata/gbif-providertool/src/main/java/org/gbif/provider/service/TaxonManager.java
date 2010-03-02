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

import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface TaxonManager extends CoreRecordManager<Taxon>,
    TreeNodeManager<Taxon, Rank> {
  /**
   * Create an annotations for each ambigous name, i.e. multiple taxa with the
   * same ScientificName & taxonAccordingTo
   * 
   * @param resourceId
   * @return
   */
  int annotateAmbigousNames(Long resourceId);

  int countAccepted(Long resourceId);

  // stats counting
  int countSynonyms(Long resourceId);

  /**
   * get accepted taxa matching a rank string and with an optional optional
   * highest root taxon If a root taxon is submitted, only descendants of this
   * taxon are returned matching the rank.
   * 
   * @param resourceId
   * @param taxonId the optional root taxon. can be null
   * @param rank the war rank string to look for
   * @return
   */
  List<Taxon> getByRank(Long resourceId, Long taxonId, String rank);

  /**
   * get accepted taxa by their nomenclatural or taxonomical status and optional
   * highest root taxon. If a root taxon is submitted, only descendants of this
   * taxon are returned matching the status.
   * 
   * @param resourceId
   * @param taxonId the optional root taxon
   * @param st the kind of status, nomenclatural or taxonomic
   * @param status the status to match
   * @return
   */
  List<Taxon> getByStatus(Long resourceId, Long taxonId, StatusType st,
      String status);

  /**
   * count all accepted taxa grouped by rank
   * 
   * @param taxonId
   * @return
   */
  List<StatsCount> getRankStats(Long taxonId);

  List<Taxon> getSynonyms(Long taxonId);

  void lookupAcceptedTaxa(Long resourceId);

  void lookupBasionymTaxa(Long resourceId);

  void lookupParentTaxa(Long resourceId);
}
