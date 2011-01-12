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
package org.gbif.provider.model.factory;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.CacheMap;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonFactory extends ModelBaseFactory<Taxon> {
  private final CacheMap<String, Rank> rankCache = new CacheMap<String, Rank>(
      250);
  @Autowired
  private ThesaurusManager thesaurusManager;

  public Taxon build(DarwinCore dwc) {
    return build(dwc, Rank.TerminalTaxon);
  }

  public Taxon build(DarwinCore dwc, Rank rank) {
    if (dwc == null) {
      return null;
    }
    Taxon tax = Taxon.newInstance(dwc.getResource());
    tax.setMpath(dwc.getTaxonomyPath(rank));
    tax.setScientificName(dwc.getHigherTaxonName(rank));
    tax.setNomenclaturalCode(dwc.getNomenclaturalCode());
    if (rank.compareTo(Rank.TerminalTaxon) >= 0) {
      // this is the lowest taxon given by dwc scientific name
      if (dwc.getTaxonID() != null) {
        tax.setGuid(dwc.getTaxonID());
      }
      tax.setTaxonRank(dwc.getTaxonRank());
      tax.setDwcRank(lookupRank(dwc.getTaxonRank()));
      tax.setSourceId(dwc.getSourceId());
      tax.setDateModified(dwc.getDateModified());
      // the following props can be removed at some point and served from darwin
      // core alone joined via resource&sourceId
      tax.setLink(dwc.getLink());
      tax.setSpecificEpithet(dwc.getSpecificEpithet());
      tax.setInfraspecificEpithet(dwc.getInfraspecificEpithet());
      tax.setScientificNameAuthorship(dwc.getScientificNameAuthorship());
      tax.setNamePublishedIn(dwc.getNamePublishedIn());
      tax.setTaxonomicStatus(dwc.getTaxonomicStatus());
      tax.setNomenclaturalStatus(dwc.getNomenclaturalStatus());
    } else {
      // a higher extracted taxon
      tax.setTaxonRank(rank.toString());
      tax.setDwcRank(rank);
    }

    return tax;
  }

  private Rank lookupRank(String taxonRank) {
    Rank dwcRank = null;
    if (rankCache.containsKey(taxonRank)) {
      dwcRank = rankCache.get(taxonRank);
    } else {
      // query thesaurus to find a matching rank
      ThesaurusConcept rank = thesaurusManager.getConcept(Rank.URI, taxonRank);
      if (rank != null) {
        dwcRank = Rank.getByUri(rank.getUri());
      }
      // also keep NULL ranks in cache
      rankCache.put(taxonRank, dwcRank);
    }
    return dwcRank;
  }

}
