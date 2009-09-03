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
package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public enum Rank {

  Kingdom("kingdom", "http://rs.tdwg.org/ontology/voc/TaxonRank#Kingdom"), Phylum(
      "phylum", "http://rs.tdwg.org/ontology/voc/TaxonRank#Phylum"), Class(
      "classs", "http://rs.tdwg.org/ontology/voc/TaxonRank#Class"), Order(
      "order", "http://rs.tdwg.org/ontology/voc/TaxonRank#Order"), Family(
      "family", "http://rs.tdwg.org/ontology/voc/TaxonRank#Family"), Genus(
      "genus", "http://rs.tdwg.org/ontology/voc/TaxonRank#Genus"), Species(
      null, "http://rs.tdwg.org/ontology/voc/TaxonRank#Species"), SubSpecies(
      null, "http://rs.tdwg.org/ontology/voc/TaxonRank#SubSpecies"), Variety(
      null, "http://rs.tdwg.org/ontology/voc/TaxonRank#Variety"), InfraSpecies(
      null, "http://rs.tdwg.org/ontology/voc/TaxonRank#InfraSpecies"), TerminalTaxon(
      "scientific_name", "");

  public static final String URI = "http://rs.tdwg.org/ontology/voc/TaxonRank";
  public static final List<Rank> DARWIN_CORE_HIGHER_RANKS;
  public static final List<Rank> COMMON_RANKS;

  static {
    List<Rank> dwcRanks = new ArrayList<Rank>();
    dwcRanks.add(Kingdom);
    dwcRanks.add(Phylum);
    dwcRanks.add(Class);
    dwcRanks.add(Order);
    dwcRanks.add(Family);
    dwcRanks.add(Genus);
    DARWIN_CORE_HIGHER_RANKS = Collections.unmodifiableList(dwcRanks);
  }

  static {
    List<Rank> ranks = new ArrayList<Rank>();
    ranks.add(Kingdom);
    ranks.add(Phylum);
    ranks.add(Class);
    ranks.add(Order);
    ranks.add(Family);
    ranks.add(Genus);
    ranks.add(Species);
    ranks.add(SubSpecies);
    ranks.add(Variety);
    COMMON_RANKS = Collections.unmodifiableList(ranks);
  }

  public static Rank getByInt(int i) {
    for (Rank r : Rank.values()) {
      if (r.ordinal() == i) {
        return r;
      }
    }
    return null;
  }

  public static Rank getByUri(String identifier) {
    for (Rank r : Rank.values()) {
      if (r.uri.equalsIgnoreCase(identifier)) {
        return r;
      }
    }
    return null;
  }

  public String columnName;

  public String uri;

  private Rank(String colName, String identifier) {
    columnName = colName;
    this.uri = identifier;
  }

  public String getColumnName() {
    return columnName;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public String toString() {
    return name();
  }
}
