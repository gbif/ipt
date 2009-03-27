package org.gbif.provider.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.util.StatsUtils;
import org.gbif.provider.util.ZipUtil;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ChecklistResourceManagerHibernate extends DataResourceManagerHibernate<ChecklistResource> implements ChecklistResourceManager{
	private static final String TCS_ARCHIVE_FILENAME="tcsArchive.rdf";
	private static final String TCS_TEMPLATE="/WEB-INF/pages/tapir/model/tcsDataset.ftl";
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	private ExtensionPropertyManager extensionPropertyManager;
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private Configuration freemarkerCfg;
	
	public ChecklistResourceManagerHibernate() {
		super(ChecklistResource.class);
	}

	public List<StatsCount> taxByTaxon(Long resourceId, Rank rank) {
		String hql = "";
		List<Object[]> taxBySth;
		if (rank== null || rank.equals(Rank.TerminalTaxon)){
			// count all terminal taxa. No matter what rank. Higher, non terminal taxa have occ_count=0, so we can include them without problem
			hql = String.format("select t.id, t.label, 1   from Taxon t   where t.resource.id=:resourceId and t.type=:rank");		
	        taxBySth = getSession().createQuery(hql)
	        	.setParameter("resourceId", resourceId)
				.setParameter("rank", Rank.TerminalTaxon)
	        	.list();
		}else{
			// only select certain rank
			hql = String.format("select t.id, t.label, count(t2)   from Taxon t, Taxon t2   where t.resource.id=:resourceId and t2.resource.id=:resourceId  and t.type=:rank  and t2.lft>=t.lft and t2.rgt<=t.rgt  group by t");		
			taxBySth = getSession().createQuery(hql)
				.setParameter("resourceId", resourceId)
				.setParameter("rank", rank)
				.list();
		}
        return StatsUtils.getDataMap(taxBySth);
	}
	public String taxByTaxonPieUrl(Long resourceId, Rank rank, int width, int height, boolean title) {
		List<StatsCount> data = taxByTaxon(resourceId, rank);
		return taxByTaxonPieUrl(data, rank, width, height, title);
	}
	public String taxByTaxonPieUrl(List<StatsCount> data, Rank rank, int width, int height, boolean title) {
		assert(rank!=null);
		String titleText = null;
		if (title){
			titleText = "Terminal Taxa By "+rank.toString();
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data);
	}

	
	


	public List<StatsCount> taxByRank(Long resourceId) {
		String hql = "";
		List<Object[]> taxBySth;
		// count all terminal taxa. No matter what rank. Higher, non terminal taxa have occ_count=0, so we can include them without problem
		hql = String.format("select t.rank, count(t)   from Taxon t   where t.resource.id=:resourceId  group by t.rank");		
        taxBySth = getSession().createQuery(hql)
        	.setParameter("resourceId", resourceId)
        	.list();
        return StatsUtils.getDataMap(taxBySth);
	}
	public String taxByRankPieUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = taxByRank(resourceId);
		return taxByRankPieUrl(data, width, height, title);
	}
	public String taxByRankPieUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Taxa By rank";
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data);
	}

	
	
	public List<StatsCount> taxByStatus(Long resourceId, StatusType type) {
		List<Object[]> taxBySth;
		String hql = String.format("select t.%s, count(t)  from Taxon t  where t.resource.id=:resourceId and t.lft=t.rgt-1  group by t.%s", type.columnName, type.columnName);		
		taxBySth = getSession().createQuery(hql)
			.setParameter("resourceId", resourceId)
			.list();
        return StatsUtils.getDataMap(taxBySth);
	}
	public String taxByStatusPieUrl(Long resourceId, StatusType type, int width, int height, boolean title) {
		List<StatsCount> data = taxByStatus(resourceId, type);
		return taxByStatusPieUrl(data, type, width, height, title);
	}
	public String taxByStatusPieUrl(List<StatsCount> data, StatusType type, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Terminal Taxa By "+type.toString();
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data);
	}

	public ChecklistResource setResourceStats(ChecklistResource resource) {
		log.debug("Setting checklist resource stats");
		Long resourceId = resource.getId();
		super.setResourceStats(resource);
		// checklist specific
		for (ExtensionMapping em : resource.getExtensionMappings()){
			if (em.getExtension().getId().equals(Constants.COMMON_NAME_EXTENSION_ID)){
				resource.setNumCommonNames(extensionRecordManager.count(em.getExtension(), resourceId));
				ExtensionProperty property = extensionPropertyManager.get(Constants.COMMON_NAME_LANGUAGE_PROPERTY_ID);
				resource.setNumCommonNameLanguages(extensionRecordManager.countDistinct(property, resourceId));				
			}
			if (em.getExtension().getId().equals(Constants.DISTRIBUTION_EXTENSION_ID)){
				resource.setNumDistributions(extensionRecordManager.count(em.getExtension(), resourceId));
				ExtensionProperty property = extensionPropertyManager.get(Constants.DISTRIBUTION_REGION_PROPERTY_ID);
				resource.setNumDistributionRegions(extensionRecordManager.countDistinct(property, resourceId));
			}
		}
		// save stats
//		this.save(resource);
		return resource;
	}

	public File writeTcsArchive(Long resourceId) throws IOException{
		File archive = cfg.getArchiveTcsFile(resourceId);
		File tcs = cfg.getResourceDataFile(resourceId, TCS_ARCHIVE_FILENAME);
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tcs,false),"UTF8"));
		// taxa=List<Taxon>
		Template temp = freemarkerCfg.getTemplate(TCS_TEMPLATE);  
		Map<String, Object> data = new HashMap<String, Object>();

//		// namespace registry
//		NamespaceRegistry nsr = new NamespaceRegistry(); 
//		data.put("nsr", nsr);
//		
		// taxa
		ScrollableResults results = taxonManager.scrollResource(resourceId);
		int batchSize=10;
		int i = 0;
		int batch=0;
		Boolean header=true;
		Boolean footer=false;		
		while(!footer){
			i=0;
			batch++;
			data.put("header", header);			
			List<Taxon> taxa = new ArrayList<Taxon>(); 
			footer=true;
			while(results.next() && i<batchSize){
				footer=false;
				Taxon taxon = (Taxon) results.get(0);
				taxa.add(taxon);
				if (results.isLast()){
					footer=true;
				}
				i++;
			}
			data.put("taxa", taxa);
			data.put("footer", footer);
			// append taxa batch to file
			try {
				temp.process(data, out);
			} catch (TemplateException e) {
				log.error("TCS template error", e);
			}
			out.flush();  
			// next batch without headers
			header=false;
			log.debug("TCS archive batch "+batch+" written");
		}
		ZipUtil.zipFile(tcs, archive);
		return archive;
	}

}
