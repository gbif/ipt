package org.gbif.provider.upload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
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
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly=false)
public class TaxonomyBuilder extends NestedSetBuilderBase<Taxon> implements RecordPostProcessor<DarwinCore, Set<Taxon>> {
	public static final int SOURCE_TYPE_ID = 2;
	private static final Set<Rank> LOG_TYPES = new HashSet<Rank>();
	@Autowired
	private DarwinCoreManager darwinCoreManager;

	
	protected TaxonomyBuilder(TaxonManager taxonManager) {
		super(taxonManager);
	}

	
	/* Run the taxonomy extraction stand alone with its own darwin core iterator.
	 * Removes previously existing taxonomy, extracts a new one, sets nested list indices, persists it and updates all darwin core records
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Set<Taxon> call() throws Exception {
		initLogging(SOURCE_TYPE_ID);

		// remove previously existing taxa
		prepare();
		
		try{
			// create unique, naturally sorted taxa from dwc records
			log.info("Generating taxonomy from occurrence records for resource "+getResourceId());
			// create taxa from dwc
			ScrollableResults records = darwinCoreManager.scrollResource(getResourceId());
			
			boolean hasNext = true;
			while (hasNext = records.next()){
				DarwinCore dwc = (DarwinCore) records.get()[0];
				processRecord(dwc);			
				darwinCoreManager.save(dwc);
			}
		}catch (InterruptedException e){
			// remove partial messy data
			log.warn("Taxonomy Builder was cancelled. Clear partial data.");
			prepare();
			throw e;
		}
		
		return close(loadResource());
	}
	
	
	/* Processes a single, raw darwin core record and assigns it a proper, normalised Taxon which is persisted.
	 * The darwin core record itself is not saved here, just updated!
	 * (non-Javadoc)
	 * @see org.gbif.provider.upload.RecordPostProcessor#processRecord(org.gbif.provider.model.CoreRecord)
	 */
	public DarwinCore processRecord(DarwinCore dwc) throws InterruptedException {
		// check thread/task cancellation
		if (Thread.currentThread().isInterrupted()){
			throw new InterruptedException("Taxonomy builder was interrupted externally");
		}
		if (dwc == null){
			log.debug("DarwinCore NULL record ignored for building the taxonomy");
			return dwc;
		}
		DwcTaxon dt = DwcTaxon.newDwcTaxon(dwc);

		Taxon tax=null;
		if (nodes.containsKey(dt.hashCode())){
			// taxon exists already. use persistent one for dwc
			tax = nodes.get(dt.hashCode());
		}else{
			// try to "insert" the entire taxonomic hierarchy
			Taxon parent = null;
			for (DwcTaxon explodedTaxon : DwcTaxon.explodeTaxon(dt)){
				if (! nodes.containsKey(explodedTaxon.hashCode())){
					tax = explodedTaxon.getTaxon();
					// link into hierarchy if this is not a root taxon
					tax.setParent(parent);
					tax = nodeManager.save(tax);
					nodes.put(explodedTaxon.hashCode(), tax);				
					parent = tax; 
				}else{
					// use existing taxon as parent
					parent = nodes.get(explodedTaxon.hashCode()); 
				}
			}
		}
		terminalNodes.add(tax);
		dwc.setTaxon(tax);
		return dwc;
	}

	
	public void statsPerRecord(DarwinCore dwc) throws InterruptedException {
		Taxon tax=dwc.getTaxon();
		if (tax !=null){
			tax.countOcc(dwc);
		}
	}

	
	@Override
	protected void setFinalStats(OccurrenceResource resource) {
		// init stats map		
		Map<Rank, Integer> stats = new HashMap<Rank, Integer>();
		stats.put(null, 0);
		for (Rank r : Rank.ALL_RANKS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (Taxon t : nodes.values()){
			stats.put(t.getDwcRank(), stats.get(t.getDwcRank())+1);
		}
		// debug only
		for (Rank r : Rank.ALL_RANKS){
			log.info(String.format("Found %s %s taxa in resource %s", stats.get(r), r, resource.getId()));
		}
		log.info(String.format("Found %s distinct taxa in resource %s", nodes.size(), resource.getId()));
		// store stats
		resource.setNumTaxa(nodes.size());
		resource.setNumTerminalTaxa(terminalNodes.size());
		resource.setNumGenera(stats.get(Rank.Genus));
		resource.setNumFamilies(stats.get(Rank.Family));
		resource.setNumOrders(stats.get(Rank.Order));
		resource.setNumClasses(stats.get(Rank.Class));
		resource.setNumPhyla(stats.get(Rank.Phylum));
		resource.setNumKingdoms(stats.get(Rank.Kingdom));
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
		return String.format("%s taxa", nodes.size());
	}

}
