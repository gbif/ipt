package org.gbif.provider.job;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.ScrollableResults;


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
	public SortedSet<Region> extractHierarchy(Long resourceId, boolean persist){
		log.info("Extracting geographic hierarchy from occurrence records for resource "+resourceId);
		// create regions from dwc
		SortedMap<DwcRegion, Region> regions = new TreeMap<DwcRegion, Region>();
		ScrollableResults dwcRecords = darwinCoreManager.scrollResource(resourceId);
		boolean hasNext = true;
		while (hasNext = dwcRecords.next()){
			DarwinCore dwc = (DarwinCore) dwcRecords.get()[0];
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
