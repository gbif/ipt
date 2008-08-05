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
	
	public String occByBasisOfRecordPieUrl(Long resourceId) {
        List<Object[]> occBySth = getSession().createQuery("select dwc.basisOfRecord, count(dwc) from DarwinCore dwc group by dwc.basisOfRecord WHERE dwc.resource.id = :resourceId")
        	.setParameter("resourceId", resourceId)
        	.list();
        Map<String, Long> data = new HashMap<String, Long>();
        for (Object[] row : occBySth){
        	String label = (String) row[0];
        	Long val = (Long) row[1];
        	data.put(label, val);
        }
        // get chart string
		return gpb.generateChartDataString(CHART_WIDTH, CHART_HEIGTH, data, sumData(data));
	}

	public String occByCollectionPieUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public String occByRegionPieUrl(Long resourceId, RegionType region) {
		// TODO Auto-generated method stub
		return null;
	}

	public String occByTaxonPieUrl(Long resourceId, Rank rank) {
		// TODO Auto-generated method stub
		return null;
	}

	public String speciesByCountryMapUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String top10TaxaPieUrl(Long resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

}
