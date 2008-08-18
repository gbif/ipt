package org.gbif.provider.job;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.ScrollableResults;


/**
 * Extracts the different geographic regions out of a darwin core record (i.e. country, state, continent, etc) and assembles them into a hierarchy
 * In addition this also creates a map of the number of distinct taxa per country base on the dwc.getTaxon(). So make sure the TaxonomyBuilder runs BEFORE this builder runs.
 * @See TaxonomyBuilder 
 * @author markus
 *
 */
public class GeographyBuilder extends HierarchyBuilderBase<Region>  {
	public static final int SOURCE_TYPE_ID = 3;
	public static final String JOB_NAME = "Geography builder";
	private static final String JOB_DESCRIPTION = "Build geographic region hierarchy from Darwin Core records in resource %s";

	public GeographyBuilder(ResourceManager<OccurrenceResource> occResourceManager,	DarwinCoreManager darwinCoreManager, TreeNodeManager<Region> treeNodeManager) {
		super(occResourceManager, darwinCoreManager, treeNodeManager, JOB_NAME, JOB_DESCRIPTION);
	}

	public int getSourceType() {
		return SOURCE_TYPE_ID;
	}
	
	@Override
	public SortedSet<Region> extractHierarchy(OccurrenceResource resource, boolean persist){
		
		log.info("Extracting geographic hierarchy from occurrence records for resource "+resource.getId());
		// remember distinct number of taxa (=set below) per country (=map key below) too
		Map<String, Set<Taxon>> taxaByCountry = new HashMap<String, Set<Taxon>>();
		// create regions from dwc
		SortedMap<DwcRegion, Region> regions = new TreeMap<DwcRegion, Region>();
		ScrollableResults dwcRecords = darwinCoreManager.scrollResource(resource.getId());
		boolean hasNext = true;
		while (hasNext = dwcRecords.next()){
			DarwinCore dwc = (DarwinCore) dwcRecords.get()[0];
			// update taxa by country
			if (! taxaByCountry.containsKey(dwc.getCountry())){
				taxaByCountry.put(dwc.getCountry(), new HashSet<Taxon>());
			}
			taxaByCountry.get(dwc.getCountry()).add(dwc.getTaxon());
			
			// extract regions
			DwcRegion dwcReg = DwcRegion.newDwcRegion(dwc);
			Region reg;
			if (regions.containsKey(dwcReg)){
				// region exists already. use persistent one for dwc
				reg = regions.get(dwcReg);				
			}else{
				// try to "insert" the entire taxonomic hierarchy
				reg = dwcReg.getRegion();
				Region parent = null;
				for (DwcRegion explodedDRegion : DwcRegion.explodeRegions(dwcReg)){
					if (! regions.containsKey(explodedDRegion)){
						Region explodedRegion = explodedDRegion.getRegion();
						// link into hierarchy if this is not a root region, e.g. continent
						explodedRegion.setParent(parent);
						if (persist){
							explodedRegion = treeNodeManager.save(explodedRegion);
						}
						regions.put(explodedDRegion, explodedRegion);				
						parent = explodedRegion; 
					}else{
						// use existing taxon as parent
						parent = regions.get(explodedDRegion); 
					}
				}
			}
			if (persist){
				dwc.setRegion(reg);
				darwinCoreManager.save(dwc);
			}
		}
		
		// store taxaByCountry in resource. Convert set of taxa into
		Map<String,Long> numTaxaByCountry = new HashMap<String, Long>();
		for (String country : taxaByCountry.keySet()){
			numTaxaByCountry.put(country, Long.valueOf(taxaByCountry.get(country).size()));
		}
		resource.setNumTaxaByCountry(numTaxaByCountry);
		
		// convert to sorted set
		SortedSet<Region> hierarchy = new TreeSet<Region>(regions.values());
		return hierarchy;
	}

	@Override
	protected void calcStats(OccurrenceResource resource,	SortedSet<Region> geography, boolean persist) {
		// init stats map		
		Map<RegionType, Integer> stats = new HashMap<RegionType, Integer>();
		stats.put(null, 0);
		for (RegionType r : RegionType.ALL_REGIONS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (Region dt : geography){
			Integer i = stats.get(dt.getRank());
			i++;
		}
		// store stats
		resource.setNumCountries(stats.get(RegionType.Country));
		occResourceManager.save(resource);
	}
}
