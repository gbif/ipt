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
import org.gbif.provider.util.GPieBuilder;
import org.hibernate.Query;

public class OccResourceManagerHibernate extends DatasourceBasedResourceManagerHibernate<OccurrenceResource> implements OccResourceManager{
	protected static GPieBuilder gpb = new GPieBuilder();
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
	}

	
	
	public List<StatsCount> occByCountry(Long resourceId) {
		return occByRegion(resourceId, RegionType.Country);
	}
	public String occByCountryMapUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = occByCountry(resourceId);
		return occByCountryMapUrl(data, width, height, title);
	}
	public String occByCountryMapUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Occurrences By Country";
		}
        // get chartmap string
		// FIXME: implement this method...
//		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));

		String chartUrl = "http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff";
		return chartUrl;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.gbif.provider.service.OccResourceManager#speciesByCountry(java.lang.Long)
	 */
	public List<StatsCount> speciesByCountry(Long resourceId) {
		// FIXME: implement this method...
		List<StatsCount> data = new ArrayList<StatsCount>();
		data.add(new StatsCount("Germany", 123l));
		data.add(new StatsCount("United Kingdom", 101l));
		data.add(new StatsCount("United States", 423l));
		data.add(new StatsCount("Brazil", 1123l));
		return data;
	}
	public String speciesByCountryMapUrl(Long resourceId, int width, int height, boolean title) {
		List<StatsCount> data = speciesByCountry(resourceId);
		return speciesByCountryMapUrl(data, width, height, title);
	}
	public String speciesByCountryMapUrl(List<StatsCount> data, int width, int height, boolean title) {
		String titleText = null;
		if (title){
			titleText = "Distinct Taxa Per Country";
		}
        // get chartmap string
		// FIXME: implement this method...
//		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));

		String chartUrl = "http://chart.apis.google.com/chart?chs=320x160&cht=t&chtm=world&chco=cccccc,fff5f0,99000d&chld=SENLAUFONZKRCALUJPUSBMLIISBBNOSIGBFIDKEEMCCHHKATITJMBELVDEFRMYESSGCYGUMOBNAGSKBYIEMQADHRNCBSMTHUAECLQAREPTCZVIKWSCPLLTILMUAWPRROGFPFGYCRBHUYBABGLBGPGRARBRMXDOPETRMARUSTMDVNJOFMVETTSATHOMIRZACOMNUABZTNSVZWVCCNFJAZSDGTMKECIDEGSRHTPKPSPAGECVALTGSYDZBJPHINAMKGBOGASNKZNASZLYGMNGHNVUBWWSUZPYKEBTTOLSZMKMNIGWKIPGGHERLKUGCUDJCMGQCGYESOCITZAFTMMZSBNPMRGNAORWBFMLBIMGMWTDLABDCFKHTJETCDNEMM&chd=t:58.1,56.4,53.6,53.5,52,52,51.7,51.6,50.7,50.5,49.9,48.2,47.4,45.3,44.6,42.2,41,40.6,40,39.5,38.9,38.8,38.2,37.3,36.7,35.4,34.8,34,32.9,32.9,32.3,30.8,30.3,29.7,29.3,28.2,27.5,27.1,26.9,26.5,25.8,25.1,24.8,24.7,24.5,24.3,24.2,22.6,22.4,22,21.5,21.3,20.7,20.6,20.4,19.9,19.8,19.7,19.7,18.6,18.4,18.4,17.6,16.8,16.8,16.4,16.2,16.2,16.2,15.7,15.7,15.7,14.9,14.5,13.7,13.6,13.1,12.9,12.8,12.5,11.7,11.6,11.6,11.2,10,9.7,9.6,9.6,9.5,9.5,9.3,8.6,8.5,8.3,8.2,7.9,7.7,7.5,7.3,7.2,7.1,6.4,6.4,6.4,6.3,6.2,6.1,6,6,5.6,5.5,5.5,5.4,5.3,5.2,5,4.9,4.6,4.6,4.6,4.5,4.4,4.4,4.3,4.2,4.1,4,4,4,3.7,3.5,3.1,3,3,3,2.9,2.9,2.7,2.7,2.6,2.6,2.5,2.5,2.4,2.4,2.3,2.2,2.2,1.9,1.8,1.8,1.7,1.4,1.4,1.4,1.3,1.3,1.3,1.1,1.1,1.1,1,0.8,0.8,0.8,0.8,0.8,0.8,0.7,0.6,0.6,0.5,0.5,0.5,0.5,0.4,0.4,0.4,0.4,0.3,0.3,0.3,0.2,0.2,0.2,0.2,0.2,0.2,0.2,0.1&chf=bg,s,e0f2ff";
		return chartUrl;
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
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
		return gpb.generateChartDataString(width, height, titleText, data, sumData(data));
	}

}
