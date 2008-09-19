package org.gbif.provider.service.impl;

import java.util.List;

import javax.persistence.EntityExistsException;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DarwinCoreExtended;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.DarwinCoreManager;
import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class DarwinCoreManagerHibernate extends CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager  {
	public static String[] searchFields = {"scientificName","locality","country","guid"};
	
	public DarwinCoreManagerHibernate() {
		super(DarwinCore.class, searchFields);
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
		removeAll(resource, DarwinCoreExtended.class);
		return super.removeAll(resource);
	}

//	@Override
//	public void remove(DarwinCore obj) {
//		DarwinCoreLocation loc = obj.getLoc();
//		DarwinCoreTaxonomy tax = obj.getTax();
//		universalRemove(loc);
//		universalRemove(tax);
//		super.remove(obj);
//		
//	}

}
