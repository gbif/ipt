package org.gbif.provider.upload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * Extracts the different geographic regions out of a darwin core record (i.e. country, state, continent, etc) and assembles them into a hierarchy
 * In addition this also creates a map of the number of distinct taxa per country base on the dwc.getTaxon(). So make sure the TaxonomyBuilder runs BEFORE this builder runs.
 * @See TaxonomyBuilder 
 * @author markus
 *
 */
@Transactional(readOnly=false)
public class GeographyBuilder extends NestedSetBuilderBase<Region> implements RecordPostProcessor<DarwinCore, Set<Region>>  {
	public static final int SOURCE_TYPE_ID = 3;
	private static final Set<RegionType> LOG_TYPES = new HashSet<RegionType>();
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	// results
	// remember distinct number of taxa (=set below) per country (=map key below) too
	private Map<String, Set<Taxon>> taxaByCountry;

	
	public GeographyBuilder(RegionManager regionManager) {
		super(regionManager);
	}

	
	public Set<Region> call() throws Exception {
		
		initLogging(SOURCE_TYPE_ID);
	
		prepare();
		
		// create regions from dwc
		ScrollableResults dwcRecords = darwinCoreManager.scrollResource(getResourceId());
		boolean hasNext = true;
		while (hasNext = dwcRecords.next()){
			DarwinCore dwc = (DarwinCore) dwcRecords.get()[0];
			processRecord(dwc);
			darwinCoreManager.save(dwc);
		}
		
		return close();
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
		int hc = dwcReg.hashCode();
		if (nodes.containsKey(dwcReg.hashCode())){
			// region exists already. use persistent one for dwc
			reg = nodes.get(dwcReg.hashCode());				
		}else{
			// try to "insert" the entire taxonomic hierarchy
			Region parent = null;
			for (DwcRegion explodedRegion : DwcRegion.explodeRegions(dwcReg)){
				if (! nodes.containsKey(explodedRegion.hashCode())){
					reg = explodedRegion.getRegion();
					// link into hierarchy if this is not a root region, e.g. continent
					reg.setParent(parent);
					reg = nodeManager.save(reg);
					nodes.put(explodedRegion.hashCode(), reg);				
					parent = reg; 
				}else{
					// use existing taxon as parent
					parent = nodes.get(explodedRegion.hashCode()); 
				}
			}
		}
		terminalNodes.add(reg);
		dwc.setRegion(reg);
		return dwc;
	}


	
		
	@Override
	protected void setStats() {
		// init stats map		
		Map<RegionType, Integer> stats = new HashMap<RegionType, Integer>();
		stats.put(null, 0);
		for (RegionType r : RegionType.ALL_REGIONS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (Region dt : nodes.values()){
			stats.put(dt.getType(), stats.get(dt.getType())+1);
		}
		// store stats in resource
		getResource().setNumCountries(stats.get(RegionType.Country));
		getResource().setNumRegions(nodes.size());
		getResource().setNumTerminalRegions(terminalNodes.size());
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
		log.info(String.format("Extracted %s geographic distinct regions from resource %s", nodes.size(), getResourceId()));		
	}


	@Override
	public void prepare() {
		taxaByCountry = new HashMap<String, Set<Taxon>>();
		super.prepare();		
	}

	@Override
	boolean logType(Enum typ) {
		if (LOG_TYPES.contains(typ)){
			return true;
		}
		return false;
	}

	@Override
	public String status() {
		return String.format("%s regions", nodes.size());
	}
}
