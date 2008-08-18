package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.GChartBuilder;
import org.hibernate.Query;

import com.googlecode.gchartjava.GeographicalArea;

public class OccResourceManagerHibernate extends DatasourceBasedResourceManagerHibernate<OccurrenceResource> implements OccResourceManager{
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
        	String label = (String) row[0];
        	if (label == null || label.trim().equals("")){
        		label = "?";
        	}
        	Long count = (Long) row[1];
        	data.add(new StatsCount(label, count));
        }
        // sort data
        Collections.sort(data);
        Collections.reverse(data);
        return data;
	}
	
	public List<StatsCount> occByBasisOfRecord(Long resourceId) {
		// get data from db
        List<Object[]> occBySth = getSession().createQuery("select dwc.basisOfRecord, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.basisOfRecord")
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
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
	}


	
	
	public List<StatsCount> occByCollection(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.collectionCode, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.collectionCode")
						    	.setParameter("resourceId", resourceId)
						    	.list();
        return getDataMap(occBySth);
	}
	public String occByCollectionPieUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByCollection(resourceId);
		return occByCollectionPieUrl(data, width, height, title);
	}

	public String occByCollectionPieUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By CollectionCode";
		}
        // get chart string
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
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
	public List<StatsCount> speciesByCountry(Long resourceId) {
		OccurrenceResource resource = get(resourceId);
		List<StatsCount> data = resource.getTaxaStatsByCountry();
		return data;
	}
	public String speciesByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height) {
		List<StatsCount> data = speciesByCountry(resourceId);
		return speciesByCountryMapUrl(area, data, width, height);
	}
	public String speciesByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height) {
        // get chartmap string
		return gpb.generateMapChartUrl(width, height, data, area);
	}

	
	
	public List<StatsCount> occByDateColected(Long resourceId) {
		// FIXME: implement this method...
		return null;
	}
	public String occByDateColectedUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByDateColected(resourceId);
		return occByDateColectedUrl(data, width, height, title);
	}

	public String occByDateColectedUrl(List<StatsCount> data, int width, int height, boolean title) {
		// FIXME: implement this method...
		String titleText = null;
		if (title){
			titleText = "Occurrences By DateCollected";
		}
        // get chart string
//		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));

		String chartUrl = "http://chart.apis.google.com/chart?cht=bvs&chs=320x160&chd=t:10,50,60,40,50,60,100,40,20,80,40,77,20,50,60,100,40,20,80,40,7,15,5,9,55,7850,40,50,60,100,40,20,60,100,13,56,48,13,20,10,50,78,60,80,40,50,60,100,40,20,40,50,60,0,80,40,50,60,100,40,20&chco=c6d9fd&chbh=3";
		return chartUrl;
	}

	
	
	public List<StatsCount> occByInstitution(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.institutionCode, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.institutionCode")
						    	.setParameter("resourceId", resourceId)
						    	.list();
        return getDataMap(occBySth);
	}
	public String occByInstitutionPieUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByInstitution(resourceId);
		return occByInstitutionPieUrl(data, width, height, title);
	}

	public String occByInstitutionPieUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By InstitutionCode";
		}
        // get chart string
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
	}
	
	
	
	public List<StatsCount> occByRegion(Long resourceId, RegionType region) {
		String hql = String.format("select dwc.%s, count(dwc)   from DarwinCoreLocation dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.%s", region.columnName, region.columnName);
        List<Object[]> occBySth = getSession().createQuery(hql).setParameter("resourceId", resourceId).list();
        return getDataMap(occBySth);
	}

	public String occByRegionPieUrl(Long resourceId, RegionType region, int width, int height, boolean title) {
		List<StatsCount> data = occByRegion(resourceId, region);
		return occByRegionPieUrl(data, region, width, height, title);
	}

	public String occByRegionPieUrl(List<StatsCount> data, RegionType region, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By "+region.toString();
		}
        // get chart string
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
	}
	
	
	
	public List<StatsCount> occByTaxon(Long resourceId, Rank rank) {
		String hql = String.format("select dwc.%s, count(dwc)   from DarwinCoreTaxonomy dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.%s", rank.columnName, rank.columnName);
        List<Object[]> occBySth = getSession().createQuery(hql).setParameter("resourceId", resourceId).list();
        return getDataMap(occBySth);
	}
	public String occByTaxonPieUrl(Long resourceId, Rank rank, int width, int height, boolean title) {
		List<StatsCount> data = occByTaxon(resourceId, rank);
		return occByTaxonPieUrl(data, rank, width, height, title);
	}
	public String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By "+rank.toString();
		}
        // get chart string
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
	}
	
	
	
	public List<StatsCount> top10Taxa(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.scientificName, count(dwc)  from DarwinCoreTaxonomy dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.scientificName  order by count(dwc) desc")
						    	.setParameter("resourceId", resourceId)
						    	.setMaxResults(10)
						    	.list();
        return getDataMap(occBySth);
	}
	public String top10TaxaPieUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = top10Taxa(resourceId);
		return top10TaxaPieUrl(data, width, height, title);
	}

	public String top10TaxaPieUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By Top 10 Taxa";
		}
        // get chart string
		return gpb.generatePiaChartUrl(width, height, titleText, data, sumData(data));
	}

}
