package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.StatsUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ChecklistResourceManagerHibernate extends DataResourceManagerHibernate<ChecklistResource> implements ChecklistResourceManager{
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	private ExtensionPropertyManager extensionPropertyManager;
	
	public ChecklistResourceManagerHibernate() {
		super(ChecklistResource.class);
	}

	public List<StatsCount> taxByTaxon(Long resourceId, Rank rank) {
		String hql = "";
		List<Object[]> taxBySth;
		if (rank== null || rank.equals(Rank.TerminalTaxon)){
			// count all terminal taxa. No matter what rank. Higher, non terminal taxa have occ_count=0, so we can include them without problem
			hql = String.format("select t.id, t.scientificName, 1   from Taxon t   where t.resource.id=:resourceId and t.dwcRank=:rank");		
	        taxBySth = getSession().createQuery(hql)
	        	.setParameter("resourceId", resourceId)
				.setParameter("rank", Rank.TerminalTaxon)
	        	.list();
		}else{
			// only select certain rank
			hql = String.format("select t.id, t.scientificName, count(t2)   from Taxon t, Taxon t2   where t.resource.id=:resourceId and t2.resource.id=:resourceId  and t.dwcRank=:rank  and t2.lft>=t.lft and t2.rgt<=t.rgt  group by t");		
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
		for (ViewExtensionMapping em : resource.getExtensionMappings()){
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
		return this.save(resource);
	}

}
