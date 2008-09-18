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

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.GChartBuilder;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.gchartjava.GeographicalArea;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OccResourceManagerHibernate extends DatasourceBasedResourceManagerHibernate<OccurrenceResource> implements OccResourceManager{
	private static final int MAX_CHART_DATA = 20;

	@Autowired
	protected AppConfig cfg;
	
	protected static GChartBuilder gpb = new GChartBuilder();
	public OccResourceManagerHibernate() {
		super(OccurrenceResource.class);
	}

	private Long sumData(List<StatsCount> data){
		Long sum = 0l;
		for (StatsCount stat : data){
			sum += stat.getCount();
		}
		return sum;
	}
	
	private List<StatsCount> getDataMap(List<Object[]> occBySth){
		List<StatsCount> data = new ArrayList<StatsCount>();
        for (Object[] row : occBySth){
        	Long id = (Long) row[0];
        	Object value = row[1];
        	Long count = (Long) row[2];
        	String label = null;
        	if (value!=null){
				label = value.toString();
        	}
        	if (StringUtils.trimToNull(label)==null){
        		label = "?";
        	}
        	data.add(new StatsCount(id, label, value, count));
        }
        // sort data
        Collections.sort(data);
        return data;
	}

	
	/**
	 * Select most frequent MAX_CHART_DATA data entries and group all other as a single #other# entry
	 * @param data
	 * @return
	 */
	private List<StatsCount> limitDataForChart(List<StatsCount> data) {
		if (data.size()>MAX_CHART_DATA){
			List<StatsCount> exceedingData = data.subList(MAX_CHART_DATA, data.size()-1);
			Long cnt = 0l;
			for (StatsCount stat : exceedingData){
				cnt+=stat.getCount();
			}
			StatsCount other = new StatsCount(null, GChartBuilder.OTHER_LABEL, GChartBuilder.OTHER_LABEL, cnt);
			List<StatsCount> limitedData = new ArrayList<StatsCount>();
			limitedData.add(other);
			limitedData.addAll(data.subList(0, MAX_CHART_DATA-1));
			return limitedData;
		}
		return data;
	}
	
	
	
	
	public List<StatsCount> occByBasisOfRecord(Long resourceId) {
		// get data from db
        List<Object[]> occBySth = getSession().createQuery("select null, dwc.basisOfRecord, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.basisOfRecord")
						    	.setParameter("resourceId", resourceId)
						    	.list();
        return getDataMap(occBySth);
	}
	public String occByBasisOfRecordPieUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByBasisOfRecord(resourceId);
		return occByBasisOfRecordPieUrl(data, width, height, title);
	}

	public String occByBasisOfRecordPieUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By BasisOfRecord";
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data, sumData(data));
	}

	
	
	public List<StatsCount> occByCountry(Long resourceId) {
		return occByRegion(resourceId, RegionType.Country);
	}
	public String occByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height) {
		List<StatsCount> data = occByCountry(resourceId);
		return occByCountryMapUrl(area, data, width, height);
	}
	public String occByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height) {
        // get chartmap string
		return gpb.generateMapChartUrl(width, height, data, area);
	}
	public GeographicalArea getMapArea(String area){		
		GeographicalArea a;
		try {
			a = GeographicalArea.valueOf(area.toUpperCase());
		} catch (IllegalArgumentException e) {
			a = GeographicalArea.WORLD;
		}
		return a;		
	}
	
	/* (non-Javadoc)
	 * @see org.gbif.provider.service.OccResourceManager#speciesByCountry(java.lang.Long)
	 */
	public List<StatsCount> taxaByCountry(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select r.id, r.label, count(stat)   from OccStatByRegionAndTaxon stat, Region r   where stat.resource.id = :resourceId and stat.region=r  group by stat.region")
		    	.setParameter("resourceId", resourceId)
		    	.list();
        return getDataMap(occBySth);
	}
	public String taxaByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height) {
		List<StatsCount> data = taxaByCountry(resourceId);
		return taxaByCountryMapUrl(area, data, width, height);
	}
	public String taxaByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height) {
        // get chartmap string
		return gpb.generateMapChartUrl(width, height, data, area);
	}

	
	
	public List<StatsCount> occByDateColected(Long resourceId) {
		// get data from db
        List<Object[]> occBySth = getSession().createQuery("select null, year(dwc.dateCollected), count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by year(dwc.dateCollected)")
						    	.setParameter("resourceId", resourceId)
						    	.list();
        return getDataMap(occBySth);
	}
	public String occByDateColectedUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByDateColected(resourceId);
		return occByDateColectedUrl(data, width, height, title);
	}

	public String occByDateColectedUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By DateCollected";
		}
        // get chart string
		String chartUrl = gpb.generateChronoChartUrl(width, height, titleText, data);
		return chartUrl;
	}

	
	
	public List<StatsCount> occByHost(Long resourceId, HostType ht) {
		String hql = String.format("select null, dwc.%s, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.%s", ht.columnName, ht.columnName);
        List<Object[]> occBySth = getSession().createQuery(hql).setParameter("resourceId", resourceId).list();
        return getDataMap(occBySth);
	}
	public String occByHostPieUrl(Long resourceId, HostType ht, int width, int height, boolean title) {
		List<StatsCount> data = occByHost(resourceId, ht);
		return occByHostPieUrl(data, ht, width, height, title);
	}

	public String occByHostPieUrl(List<StatsCount> data, HostType ht, int width, int height, boolean title) {
		assert(ht!=null);
		String titleText = null;
		if (title){
			titleText = "Occurrences By "+ht.toString();
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data, sumData(data));
	}
	
	
	
	public List<StatsCount> occByRegion(Long resourceId, RegionType region) {
		String hql;
		List<Object[]> occBySth;
		// only select certain rank
		hql = String.format("select r.id, r.label, sum(r2.occTotal)   from Region r, Region r2   where r.resource.id=:resourceId  and r.type=:type  and r2.lft>=r.lft and r2.rgt<=r.rgt  group by r");		
		occBySth = getSession().createQuery(hql)
			.setParameter("resourceId", resourceId)
			.setParameter("type", region)
			.list();
        return getDataMap(occBySth);
	}

	public String occByRegionPieUrl(Long resourceId, RegionType region, int width, int height, boolean title) {
		List<StatsCount> data = occByRegion(resourceId, region);
		return occByRegionPieUrl(data, region, width, height, title);
	}

	public String occByRegionPieUrl(List<StatsCount> data, RegionType region, int width, int height, boolean title) {
		assert(region!=null);
		String titleText = null;
		if (title){
			titleText = "Occurrences By "+region.toString();
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data, sumData(data));
	}
	
	
	
	public List<StatsCount> occByTaxon(Long resourceId, Rank rank) {
		String hql = "";
		List<Object[]> occBySth;
		if (rank== null || rank.equals(Rank.TerminalTaxon)){
			// count all terminal taxa. No matter what rank. Higher, non terminal taxa have occ_count=0, so we can include them without problem
			hql = String.format("select t.id, t.fullname, sum(t2.occTotal)   from Taxon t, Taxon t2   where t.resource.id=:resourceId  and t2.lft>=t.lft and t2.rgt<=t.rgt  group by t");		
	        occBySth = getSession().createQuery(hql)
	        	.setParameter("resourceId", resourceId)
	        	.list();
		}else{
			// only select certain rank
			hql = String.format("select t.id, t.fullname, sum(t2.occTotal)   from Taxon t, Taxon t2   where t.resource.id=:resourceId  and t.dwcRank=:rank  and t2.lft>=t.lft and t2.rgt<=t.rgt  group by t");		
			occBySth = getSession().createQuery(hql)
				.setParameter("resourceId", resourceId)
				.setParameter("rank", rank)
				.list();
		}
        return getDataMap(occBySth);
	}
	public String occByTaxonPieUrl(Long resourceId, Rank rank, int width, int height, boolean title) {
		List<StatsCount> data = occByTaxon(resourceId, rank);
		return occByTaxonPieUrl(data, rank, width, height, title);
	}
	public String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width, int height, boolean title) {
		assert(rank!=null);
		String titleText = null;
		if (title){
			titleText = "Occurrences By "+rank.toString();
		}
        // get chart string
		data=limitDataForChart(data);
		return gpb.generatePieChartUrl(width, height, titleText, data, sumData(data));
	}

}
