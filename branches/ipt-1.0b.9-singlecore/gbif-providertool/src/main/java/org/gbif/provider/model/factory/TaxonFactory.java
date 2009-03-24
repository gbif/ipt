package org.gbif.provider.model.factory;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.CacheMap;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonFactory extends ModelBaseFactory<Taxon>{
	private CacheMap<String, Rank> rankCache = new CacheMap<String, Rank>(250);
	@Autowired
	private ThesaurusManager thesaurusManager;

	public Taxon build(DarwinCore dwc) {
		return build(dwc, Rank.TerminalTaxon);
	}

	public Taxon build(DarwinCore dwc, Rank rank) {
		if (dwc==null){
			return null;
		}
		Taxon tax = Taxon.newInstance(dwc.getResource());
		tax.setMpath(dwc.getTaxonomyPath(rank));
		tax.setScientificName(dwc.getHigherTaxonName(rank));
		tax.setTaxonAccordingTo(dwc.getTaxonAccordingTo());
		tax.setNomenclaturalCode(dwc.getNomenclaturalCode());
		if (rank.compareTo(Rank.TerminalTaxon)>=0){
			// this is the lowest taxon given by dwc scientific name
			if (dwc.getTaxonID()!=null){
				tax.setGuid(dwc.getTaxonID());
			}
			tax.setTaxonRank(dwc.getTaxonRank());
			tax.setDwcRank(lookupRank(dwc.getTaxonRank()));
			//TODO: this is a workaround for keeping links and localid in the taxon class. 
			// Can this be removed at some point and served from darwin core alone?
			tax.setLink(dwc.getLink());
			tax.setLocalId(dwc.getLocalId());
			tax.setModified(dwc.getModified());
			// other copied taxon properties
			tax.setSpecificEpithet(dwc.getSpecificEpithet());
			tax.setInfraspecificEpithet(dwc.getInfraspecificEpithet());
			tax.setScientificNameAuthorship(dwc.getScientificNameAuthorship());
			tax.setNamePublishedIn(dwc.getNamePublishedIn());
			tax.setTaxonomicStatus(dwc.getTaxonomicStatus());
			tax.setNomenclaturalStatus(dwc.getNomenclaturalStatus());
			
		}else{
			// a higher extracted taxon
			tax.setTaxonRank(rank.toString());
			tax.setDwcRank(rank);
		}

		return tax;
	}

	private Rank lookupRank(String taxonRank) {
		Rank dwcRank = null; 
		if (rankCache.containsKey(taxonRank)){
			dwcRank = rankCache.get(taxonRank);
		}else{
			// query thesaurus to find a matching rank
			ThesaurusConcept rank = thesaurusManager.getConcept(Rank.URI, taxonRank);
			if (rank != null){
				dwcRank = Rank.getByUri(rank.getUri());
			}
			// also keep NULL ranks in cache
			rankCache.put(taxonRank, dwcRank);
		}
		return dwcRank;				
	}

}
