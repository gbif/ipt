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
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly=false)
public class TaxonomyBuilder extends TaskBase implements RecordPostProcessor<DarwinCore, Set<Taxon>> {
	public static final int SOURCE_TYPE_ID = 2;
	private static final Log log = LogFactory.getLog(TaxonomyBuilder.class);
	private static I18nLog logdb = I18nLogFactory.getLog(TaxonomyBuilder.class);

	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	private TaxonManager taxonManager;
	// results
	private Map<Integer, Taxon> dwcTaxonHashMap;


	
	/* Run the taxonomy extraction stand alone with its own darwin core iterator.
	 * Removes previously existing taxonomy, extracts a new one, sets nested list indices, persists it and updates all darwin core records
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Set<Taxon> call() throws Exception {
		initLogging(SOURCE_TYPE_ID);

		// remove previously existing taxa
		prepare();
		
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
		
		return close();
	}
	
	
	/* Processes a single, raw darwin core record and assigns it a proper, normalised Taxon which is persisted.
	 * The darwin core record itself is not saved here, just updated!
	 * (non-Javadoc)
	 * @see org.gbif.provider.upload.RecordPostProcessor#processRecord(org.gbif.provider.model.CoreRecord)
	 */
	public DarwinCore processRecord(DarwinCore dwc) {
		if (dwc == null){
			log.debug("DarwinCore NULL record ignored for building the taxonomy");
			return dwc;
		}
		else if (dwc.getTax()==null || dwc.getLoc()==null){
			log.warn(String.format("DarwinCore record %s without mandatory tax/loc component ignored for building the taxonomy", dwc.getId()));
			return dwc;
		}
		DwcTaxon dt = DwcTaxon.newDwcTaxon(dwc);

		Taxon tax=null;
		if (dwcTaxonHashMap.containsKey(dt.hashCode())){
			// taxon exists already. use persistent one for dwc
			tax = dwcTaxonHashMap.get(dt.hashCode());				
		}else{
			// try to "insert" the entire taxonomic hierarchy
			Taxon parent = null;
			for (DwcTaxon explodedTaxon : DwcTaxon.explodeTaxon(dt)){
				if (! dwcTaxonHashMap.containsKey(explodedTaxon.hashCode())){
					tax = explodedTaxon.getTaxon();
					// link into hierarchy if this is not a root taxon
					tax.setParent(parent);
					tax = taxonManager.save(tax);
					dwcTaxonHashMap.put(explodedTaxon.hashCode(), tax);				
					parent = tax; 
				}else{
					// use existing taxon as parent
					parent = dwcTaxonHashMap.get(explodedTaxon.hashCode()); 
				}
			}
		}
		dwc.setTaxon(tax);
		return dwc;
	}

	/* Finalizes the taxonomy extraction run via the processRecord handler.
	 * Creates a SortedSet of DwcTaxa, calculates + persists nested list indices and resource stats
	 * Should be called when the external iterator for the DarwinCore handler is finished.
	 * (non-Javadoc)
	 * @see org.gbif.provider.upload.RecordPostProcessor#close()
	 */
	public Set<Taxon> close(){
		// convert to sorted set
		SortedSet<Taxon> taxonomy = new TreeSet<Taxon>(dwcTaxonHashMap.values());
		
		// assign nested set indices and save taxonomic hierarchy
		calcNestedSetIndices(taxonomy);
		
		// persist sorted taxa starting with parentless root taxa
		for (Taxon t : taxonomy){
			taxonManager.save(t);
		}

		// persist taxon statistics in resource
		calcStats(taxonomy);
		occResourceManager.save(getResource());
		
		return taxonomy;
	}
	
	private void calcNestedSetIndices(SortedSet<Taxon> taxonomy){
		log.info(String.format("Calculating nested set indices for taxonomy with %s taxa", taxonomy.size()));
		Stack<Taxon> parentStack = new Stack<Taxon>();
		Long idx = 0l;
		for (Taxon t : taxonomy){
			if (t.getDwcRank() == null){
				// dont do nothing special
			}
			else if (t.getDwcRank().equals(Rank.Family)){
				log.debug("process family "+t.getFullname());
			}
			else if (t.getDwcRank().equals(Rank.Order)){
				log.debug("process order "+t.getFullname());
			}
			else if (t.getDwcRank().equals(Rank.Class)){
				log.debug("process class "+t.getFullname());
			}
			else if (t.getDwcRank().equals(Rank.Kingdom)){
				log.debug("process kingdom "+t.getFullname());
			}
			// process right values for taxa on stack. But only ...
			// if stack has parents at all and if new taxon is either 
			// a) a root taxon (parent==null)
			// b) or the last stack taxon is not the parent of this taxon
			while (parentStack.size()>0 && (t.getParent() == null || !t.getParent().equals(parentStack.peek()))){
				// last taxon on the stack is not the parent. 
				// Get last taxon from stack, set rgt index and compare again
				Taxon nonParent = parentStack.pop();
				nonParent.setRgt(idx++);				
				taxonManager.save(nonParent);
			}
			// the last taxon on stack is the parent or stack is empty. 
			// Next taxon might be a child, so dont set rgt index yet, but put onto stack
			t.setLft(idx++);
			parentStack.push(t);
			
			// flush to database from time to time
			if (idx % 2000 == 0){
				darwinCoreManager.flush();
			}
			
		}
		// finally empty the stack, assign rgt value and persist
		for (Taxon t : parentStack){
			t.setRgt(idx++);				
			taxonManager.save(t);
		}
	}

	private void calcStats(SortedSet<Taxon> taxonomy) {
		// init stats map		
		Map<Rank, Integer> stats = new HashMap<Rank, Integer>();
		stats.put(null, 0);
		for (Rank r : Rank.ALL_RANKS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (Taxon t : taxonomy){
			stats.put(t.getDwcRank(), stats.get(t.getDwcRank())+1);
		}
		// debug only
		for (Rank r : Rank.ALL_RANKS){
			log.info(String.format("Found %s %s taxa in resource %s", stats.get(r), r, getResourceId()));
		}
		log.info(String.format("Found %s distinct taxa in resource %s", taxonomy.size(), getResourceId()));
		// store stats
		getResource().setNumSpecies(stats.get(Rank.Species));
		getResource().setNumGenera(stats.get(Rank.Genus));
		getResource().setNumFamilies(stats.get(Rank.Family));
		getResource().setNumOrders(stats.get(Rank.Order));
		getResource().setNumClasses(stats.get(Rank.Class));
		getResource().setNumPhyla(stats.get(Rank.Phylum));
		getResource().setNumKingdoms(stats.get(Rank.Kingdom));
	}

	public void prepare() {
		dwcTaxonHashMap = new HashMap<Integer, Taxon>();
		log.info("Removing previously existing taxonomy from resource "+getResourceId());
		taxonManager.deleteAll(getResource());		
	}


	public String status() {
		return String.format("%s taxa", dwcTaxonHashMap.size());
	}

}
