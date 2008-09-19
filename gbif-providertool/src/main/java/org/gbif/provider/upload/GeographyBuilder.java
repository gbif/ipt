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
import org.gbif.provider.model.OccurrenceResource;
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
		
		Set<Region> result = close(loadResource());
		occResourceManager.save(loadResource());

		return result;
	}



	public DarwinCore processRecord(DarwinCore dwc) {
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



	public void statsPerRecord(DarwinCore dwc) throws InterruptedException {
		Region region=dwc.getRegion();
		if (region !=null){
			region.countOcc(dwc);
		}
	}
	
		
	@Override
	protected void setFinalStats(OccurrenceResource resource) {
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
		resource.setNumCountries(stats.get(RegionType.Country));
		resource.setNumRegions(nodes.size());
		resource.setNumTerminalRegions(terminalNodes.size());
		// debug only
		for (RegionType r : RegionType.ALL_REGIONS){
			log.info(String.format("Found %s %s regions in resource %s", stats.get(r), r, resource.getId()));
		}
		
		log.info(String.format("Extracted %s geographic distinct regions from resource %s", nodes.size(), resource.getId()));
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
