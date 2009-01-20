package org.gbif.provider.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.CSVReader;
import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class DarwinCoreManagerHibernate extends CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager  {
	public static final Long GEO_EXTENSION_ID = 3l;
	public static final ExtensionProperty LATITUDE_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/DecimalLatitude");
	public static final ExtensionProperty LONGITUDE_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/DecimalLongitude");
	public static final ExtensionProperty GEODATUM_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/GeodeticDatum");
	
	@Autowired
	private AnnotationManager annotationManager;
		
	public DarwinCoreManagerHibernate() {
		super(DarwinCore.class);
	}

	@Override
	@Transactional(readOnly=false)
	public DarwinCore save(DarwinCore dwc) {
		// removed the unique checking here cause its too performance consuming
		// the database has a unique constraint on resource_fk + local_id 
		// hibernate will raise a "" 
//		if (dwc.getId() == null){
//			// only check localId constraint for transient objects
//			Long resourceId = dwc.getResourceId();
//			String localId = dwc.getLocalId();
//			DarwinCore twin = this.findByLocalId(localId, resourceId);
//			if (twin != null){				
//				throw new EntityExistsException(String.format("DarwinCoreRecord must have a unique localId within a resource. But localId %s exists already for resourceId %s",localId, resourceId));
//			}
//		}
		try{
			dwc = super.save(dwc);
		}catch (ConstraintViolationException e){
			// raised most likely when a local_id/resource_fk duplicate exists 
			// therefore raise EntityExistsException...
			Long resourceId = null;
			if (dwc.getResource() != null){
				resourceId = dwc.getResource().getId();
			}
			throw new EntityExistsException(String.format("DarwinCoreRecord must have a unique localId within a resource. But localId %s seems to exist already for resourceId %s",dwc.getLocalId(), resourceId), e);
		}
		return dwc;
	}

	public List<DarwinCore> getByRegion(Long regionId, Long resourceId, boolean inclChildren) {
		String hql; 
		if (inclChildren){
			hql = "select dwc FROM DarwinCore dwc, Region r, Region r2 WHERE dwc.resource.id=:resourceId  and dwc.region=r2  and r.id=:regionId  and r2.lft>=r.lft and r2.rgt<=r.rgt"; 
		}else{
			hql = "select dwc FROM DarwinCore dwc WHERE dwc.resource.id=:resourceId  and dwc.region.id=:regionId"; 
		}
        Query query = getSession().createQuery(hql)
			.setParameter("regionId", regionId)
			.setParameter("resourceId", resourceId);
        return query.list();
	}

	public List<DarwinCore> getByTaxon(Long taxonId, Long resourceId, boolean inclChildren) {
		String hql; 
		if (inclChildren){
			hql = "select dwc FROM DarwinCore dwc, Taxon t, Taxon t2 WHERE dwc.resource.id=:resourceId  and dwc.taxon=t2  and t.id=:taxonId  and t2.lft>=t.lft and t2.rgt<=t.rgt"; 
		}else{
			hql = "select dwc FROM DarwinCore dwc WHERE dwc.resource.id=:resourceId  and dwc.taxon.id=:taxonId"; 
		}
        Query query = getSession().createQuery(hql)
			.setParameter("taxonId", taxonId)
			.setParameter("resourceId", resourceId);
        return query.list();
	}

	@Override
	public int removeAll(Resource resource) {
		return super.removeAll(resource);
	}

	public boolean updateWithGeoExtension(DarwinCore dwc, ExtensionRecord extRec){
		String geodatum = null;
		Point loc = new Point();
		// tmp raw value
		for (ExtensionProperty prop : extRec){
			String val = StringUtils.trimToNull(extRec.getPropertyValue(prop));
			// check string coordinates
			if(prop.equals(LATITUDE_PROP)){
				if (val !=null){
					try {
						loc.setLatitude(Double.valueOf(val));
					} catch (NumberFormatException e) {
						annotationManager.badDataType(dwc, "DecimalLatitude", "Float", val);
					} catch (IllegalArgumentException e) {
						annotationManager.annotate(dwc, AnnotationType.WrongDatatype, String.format("Latitude value '%s' is out of allowed range", val));
					}
				}
			}
			else if(prop.equals(LONGITUDE_PROP)){
				if (val !=null){
					try {
						loc.setLongitude(Double.valueOf(val));
					} catch (NumberFormatException e) {
						annotationManager.badDataType(dwc, "DecimalLongitude", "Float", val);
					} catch (IllegalArgumentException e) {
						annotationManager.annotate(dwc, AnnotationType.WrongDatatype, String.format("Longitude value '%s' is out of allowed range", val));
					}
				}
			}
			else if(prop.equals(GEODATUM_PROP)){
				geodatum=extRec.getPropertyValue(prop);
			}
		}
		if (loc.isValid()){
			dwc.setLocation(loc);
			return true;
		}
		return false;
	}
	
}
