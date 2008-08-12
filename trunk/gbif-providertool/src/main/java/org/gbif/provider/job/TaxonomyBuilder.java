package org.gbif.provider.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.appfuse.model.User;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.util.JSONUtils;
import org.hibernate.ScrollableResults;


public class TaxonomyBuilder implements org.gbif.provider.job.Job {
	public static final int SOURCE_TYPE_ID = 2;
	protected static final Log log = LogFactory.getLog(TaxonomyBuilder.class);
	protected static I18nLog logdb = I18nLogFactory.getLog(TaxonomyBuilder.class);

	public static final String RESOURCE_ID = "resourceId";
	public static final String USER_ID = "userId";

	protected DarwinCoreManager darwinCoreManager;
	protected ResourceManager<OccurrenceResource> occResourceManager;
	protected TaxonManager taxonManager;

	private TaxonomyBuilder(ResourceManager<OccurrenceResource> occResourceManager, DarwinCoreManager darwinCoreManager, TaxonManager taxonManager) {
		super();
		this.darwinCoreManager = darwinCoreManager;
		this.occResourceManager = occResourceManager;
		this.taxonManager = taxonManager;
	}

	public int getSourceType() {
		return SOURCE_TYPE_ID;
	}

	public static Map<String, Object> getSeed(Long resourceId, Long userId){
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put(RESOURCE_ID, resourceId);
		seed.put(USER_ID, userId);
		return seed;
	}

	
	public static Job newTaxonomyJob(Resource resource, User user, int repeatInDays){
		// create job data
		Map<String, Object> seed = getSeed(resource.getId(), user.getId());
		// create upload job
		Job job = new Job();
		job.setJobClassName(TaxonomyBuilder.class.getName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setRepeatInDays(repeatInDays);
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setRunningGroup(JobUtils.getJobGroup(resource));
		job.setName("Taxonomy builder");
		job.setDescription("Build taxonomy from Darwin Core records in resource "+resource.getTitle());
		return job;				
	}
	
	public void launch(Map<String, Object> seed) throws Exception {
		Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
		try{
			Long userId = Long.valueOf(seed.get(USER_ID).toString());
			MDC.put(I18nDatabaseAppender.MDC_USER, userId);
		} catch (NumberFormatException e) {
			String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
			logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
		}
		MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));

		// set sourceId to jobID
		Integer sourceId = null;
		if (seed.get(Launchable.JOB_ID) != null && !seed.get(Launchable.JOB_ID).equals("null")){
			try{
				sourceId = Integer.valueOf(seed.get(Launchable.JOB_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, sourceId);
			} catch (NumberFormatException e) {
				String[] params = {Launchable.JOB_ID, seed.toString()};
				logdb.warn("{0} in seed is no Integer {1}", params, e);
			}
		}
		MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, getSourceType());

		// get resource
		OccurrenceResource resource = occResourceManager.get(resourceId);
		
		// create unique, naturally sorted taxa from dwc records
		SortedSet<DwcTaxon> taxonomy = extractTaxonomy(resourceId, true);
		
		// assign nested set indices and save taxonomic hierarchy
		calcNestedSetIndices(taxonomy, true);
		
		// persist taxon statistics
		calcStats(resource, taxonomy);
	}
	

	public SortedSet<DwcTaxon> extractTaxonomy(Long resourceId, boolean persist){
		log.info("Generating taxonomy from occurrence records for resource %s"+resourceId);
		// create taxa from dwc
		SortedMap<DwcTaxon, DwcTaxon> taxa = new TreeMap<DwcTaxon, DwcTaxon>();
		ScrollableResults dwcRecords = darwinCoreManager.scrollResource(resourceId);
		
		boolean hasNext = true;
//		while (hasNext = dwcRecords.next()){
		for (DarwinCore dwc : darwinCoreManager.getAll(resourceId)){
//			DarwinCore dwc = (DarwinCore) dwcRecords.get()[0];
			if (dwc == null){
				continue;
			}
			DwcTaxon dt = DwcTaxon.newDwcTaxon(dwc);
			Taxon tax;
			if (taxa.containsKey(dt)){
				// taxon exists already. use persistent one for dwc
				tax = taxa.get(dt).getTaxon();				
				// dt is a terminal taxon. The saved one might not, so make sure terminal is set true
				taxa.get(dt).setTerminal(true);
			}else{
				// try to "insert" the entire taxonomic hierarchy
				tax = dt.getTaxon();
				Taxon parent = null;
				for (DwcTaxon explodedDTaxon : DwcTaxon.explodeTaxon(dt)){
					if (! taxa.containsKey(explodedDTaxon)){
						Taxon explodedTaxon = explodedDTaxon.getTaxon();
						// link into hierarchy if this is not a root taxon
						explodedTaxon.setParent(parent);
						if (persist){
							explodedTaxon = taxonManager.save(explodedTaxon);
						}
						taxa.put(explodedDTaxon, explodedDTaxon);				
						parent = explodedTaxon; 
					}else{
						// use existing taxon as parent
						parent = taxa.get(explodedDTaxon).getTaxon(); 
					}
				}
			}
			if (persist){
				dwc.setTaxon(tax);
				darwinCoreManager.save(dwc);
			}
		}
		// convert to sorted set
		SortedSet<DwcTaxon> taxonomy = new TreeSet<DwcTaxon>(taxa.values());
		return taxonomy;
	}
	
	public void calcNestedSetIndices(SortedSet<DwcTaxon> taxonomy, boolean persist){
		log.info("Calculating nested set indices for taxonomy with %s taxa"+taxonomy.size());
		Stack<Taxon> parentStack = new Stack<Taxon>();
		Long idx = 0l;
		for (DwcTaxon dt : taxonomy){
			Taxon t = dt.getTaxon();
			// last taxon on the stack is not the parent. 
			// Get last taxon from stack, set rgt index and compare again
			if (t.getRank().toLowerCase().startsWith("fam")){
				log.debug("process family "+t.getFullname());
			}
			// process right values for taxa on stack. But only ...
			// if stack has parents at all and if new taxon is either 
			// a) a root taxon (parent==null)
			// b) or the last stack taxon is not the parent of this taxon
			while (parentStack.size()>0 && (t.getParent() == null || !t.getParent().equals(parentStack.peek()))){
				Taxon nonParent = parentStack.pop();
				nonParent.setRgt(idx++);				
				if (persist){
					taxonManager.save(nonParent);
				}
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
			if (persist){
				taxonManager.save(t);
			}
		}
	}

	private void calcStats(OccurrenceResource resource,	SortedSet<DwcTaxon> taxonomy) {
		// init stats map		
		Map<Rank, Integer> stats = new HashMap<Rank, Integer>();
		int numTerminal=0;
		stats.put(null, 0);
		for (Rank r : Rank.ALL_RANKS){
			stats.put(r, 0);
		}
		// aggregate stats
		for (DwcTaxon dt : taxonomy){
			Taxon t = dt.getTaxon();
			Integer i = stats.get(dt.getDwcRank());
			i++;
			if (dt.isTerminal()){
				numTerminal++;			}
		}
		// store stats
		resource.setNumTerminalTaxa(numTerminal);
		resource.setNumSpecies(stats.get(Rank.Species));
		resource.setNumGenera(stats.get(Rank.Genus));
		resource.setNumFamilies(stats.get(Rank.Family));
		resource.setNumOrders(stats.get(Rank.Order));
		resource.setNumClasses(stats.get(Rank.Class));
		resource.setNumPhyla(stats.get(Rank.Phylum));
		resource.setNumKingdoms(stats.get(Rank.Kingdom));
		occResourceManager.save(resource);
	}
}
