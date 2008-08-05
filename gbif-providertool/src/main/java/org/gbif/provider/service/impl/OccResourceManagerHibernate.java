package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.GPieBuilder;
import org.hibernate.Query;

public class OccResourceManagerHibernate extends DatasourceBasedResourceManagerHibernate<OccurrenceResource> implements OccResourceManager{
	public OccResourceManagerHibernate() {
		super(OccurrenceResource.class);
	}

	protected static GPieBuilder gpb = new GPieBuilder();

	private Long sumData(Map<String, Long> data){
		Long sum = 0l;
		for (Long val : data.values()){
			sum += val;
		}
		return sum;
	}
	
	private Map<String, Long> getDataMap(List<Object[]> occBySth){
        Map<String, Long> data = new HashMap<String, Long>();
        for (Object[] row : occBySth){
        	String label = (String) row[0];
        	if (label == null || label.trim().equals("")){
        		label = "?";
        	}
        	Long val = (Long) row[1];
        	data.put(label, val);
        }
        return data;
	}
	
	public String occByBasisOfRecordPieUrl(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.basisOfRecord, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.basisOfRecord")
					        	.setParameter("resourceId", resourceId)
					        	.list();
        Map<String, Long> data = getDataMap(occBySth);
        // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String occByCollectionPieUrl(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.collectionCode, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.collectionCode")
						    	.setParameter("resourceId", resourceId)
						    	.list();
	    Map<String, Long> data = getDataMap(occBySth);
	    // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String occByCountryMapUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String occByDateColectedUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String occByInstitutionPieUrl(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.institutionCode, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.institutionCode")
						    	.setParameter("resourceId", resourceId)
						    	.list();
	    Map<String, Long> data = getDataMap(occBySth);
	    // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String occByRegionPieUrl(Long resourceId, RegionType region) {
		String hql = String.format("select dwc.%s, count(dwc)   from DarwinCoreLocation dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.%s", region.columnName, region.columnName);
        List<Object[]> occBySth = getSession().createQuery(hql).setParameter("resourceId", resourceId).list();
	    Map<String, Long> data = getDataMap(occBySth);
	    // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String occByTaxonPieUrl(Long resourceId, Rank rank) {
		String hql = String.format("select dwc.%s, count(dwc)   from DarwinCoreTaxonomy dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.%s", rank.columnName, rank.columnName);
        List<Object[]> occBySth = getSession().createQuery(hql).setParameter("resourceId", resourceId).list();
	    Map<String, Long> data = getDataMap(occBySth);
	    // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String speciesByCountryMapUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String top10TaxaPieUrl(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.scientificName, count(dwc)   from DarwinCoreTaxonomy dwc   where dwc.dwc.resource.id = :resourceId   group by dwc.scientificName")
						    	.setParameter("resourceId", resourceId)
						    	.setMaxResults(10)
						    	.list();
	    Map<String, Long> data = getDataMap(occBySth);
	    // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

}
