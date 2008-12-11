package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.geo.GeoserverUtils;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.GChartBuilder;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.gchartjava.GeographicalArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ChecklistResourceManagerHibernate extends DataResourceManagerHibernate<ChecklistResource> implements ChecklistResourceManager{

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
        return getDataMap(taxBySth);
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

	
	
	
	public List<StatsCount> taxByStatus(Long resource_id, HostType ht) {
		return new ArrayList<StatsCount>();
	}

	public String taxByStatusPieUrl(List<StatsCount> data, HostType ht, int width, int height, boolean title) {
		return "";
	}

}
