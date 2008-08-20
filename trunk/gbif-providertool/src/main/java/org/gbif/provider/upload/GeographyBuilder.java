package org.gbif.provider.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Extracts the different geographic regions out of a darwin core record (i.e. country, state, continent, etc) and assembles them into a hierarchy
 * In addition this also creates a map of the number of distinct taxa per country base on the dwc.getTaxon(). So make sure the TaxonomyBuilder runs BEFORE this builder runs.
 * @See TaxonomyBuilder 
 * @author markus
 *
 */
@Transactional(readOnly=false)
public class GeographyBuilder extends TaskBase implements RecordPostProcessor<DarwinCore, Set<Region>>  {
	public static final int SOURCE_TYPE_ID = 3;

	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	private RegionManager regionManager;
	// results
	// remember distinct number of taxa (=set below) per country (=map key below) too
	private Map<String, Set<Taxon>> taxaByCountry = new HashMap<String, Set<Taxon>>();
	private SortedMap<DwcRegion, Region> regions = new TreeMap<DwcRegion, Region>();

	
	public SortedSet<Region> call() throws Exception {
		init(SOURCE_TYPE_ID);
		
		// create regions from dwc
		ScrollableResults dwcRecords = darwinCoreManager.scrollResource(getResourceId());
		boolean hasNext = true;
		while (hasNext = dwcRecords.next()){
			DarwinCore dwc = (DarwinCore) dwcRecords.get()[0];
			processRecord(dwc);
			darwinCoreManager.save(dwc);
		}
		SortedSet<Region> result = close();
		List<Region> regs = new ArrayList<Region>(result);
//		darwinCoreManager.debugSession();
		return result;
	}

	public SortedSet<Region> close() {
		calcStats();
		occResourceManager.save(getResource());
		
		// convert to sorted set of Regions
		SortedSet<Region> hierarchy = new TreeSet<Region>(regions.values());
		return hierarchy;
		
	}

	public DarwinCore processRecord(DarwinCore dwc) {
		// update taxa by country
		if (! taxaByCountry.containsKey(dwc.getCountry())){
			taxaByCountry.put(dwc.getCountry(), new HashSet<Taxon>());
		}
		Taxon t1 = dwc.getTaxon();
		if (t1 != null){
			taxaByCountry.get(dwc.getCountry()).add(t1);
		}
		
		// extract regions
		DwcRegion dwcReg = DwcRegion.newDwcRegion(dwc);
		Region reg = null;
		if (regions.containsKey(dwcReg)){
			// region exists already. use persistent one for dwc
			reg = regions.get(dwcReg);				
		}else{
			// try to "insert" the entire taxonomic hierarchy
			Region parent = null;
			for (DwcRegion explodedDRegion : DwcRegion.explodeRegions(dwcReg)){
				if (! regions.containsKey(explodedDRegion)){
					reg = explodedDRegion.getRegion();
					// link into hierarchy if this is not a root region, e.g. continent
					reg.setParent(parent);
					reg = regionManager.save(reg);
					regions.put(explodedDRegion, reg);				
					parent = reg; 
				}else{
					// use existing taxon as parent
					parent = regions.get(explodedDRegion); 
				}
			}
		}
		dwc.setRegion(reg);
		return dwc;
	}


	
		
	private void calcStats() {
		// init stats map		
		Map<RegionType, Integer> stats = new HashMap<RegionType, Integer>();
		stats.put(null, 0);
		for (RegionType r : RegionType.ALL_REGIONS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (Region dt : regions.values()){
			Integer i = stats.get(dt.getRank());
			i++;
		}
		// store stats in resource
		getResource().setNumCountries(stats.get(RegionType.Country));
		// debug only
		for (RegionType r : RegionType.ALL_REGIONS){
			log.info(String.format("Found %s %s regions in resource %s", stats.get(r), r, getResourceId()));
		}
		
		// store taxaByCountry in resource. Convert set of taxa into
		Map<String,Long> numTaxaByCountry = new HashMap<String, Long>();
		for (String country : taxaByCountry.keySet()){
			numTaxaByCountry.put(country, Long.valueOf(taxaByCountry.get(country).size()));
		}
		getResource().setNumTaxaByCountry(numTaxaByCountry);
		log.info(String.format("Extracted %s geographic distinct regions from resource %s", regions.size(), getResourceId()));		
	}


	public String status() {
		throw new NotImplementedException("TBD");
	}
	
}
