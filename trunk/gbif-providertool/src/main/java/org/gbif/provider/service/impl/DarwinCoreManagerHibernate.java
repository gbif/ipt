package org.gbif.provider.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.service.DarwinCoreManager;
import org.hibernate.Query;
import org.hibernate.Session;

public class DarwinCoreManagerHibernate extends CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager  {
	public static String[] searchFields = {"scientificName","locality","country","guid"};
	
	public DarwinCoreManagerHibernate() {
		super(DarwinCore.class, searchFields);
	}

	@Override
	public DarwinCore save(DarwinCore dwc) {
//		if (dwc.getId() == null){
//			// only check localId constraint for transient objects
//			Long resourceId = dwc.getResourceId();
//			String localId = dwc.getLocalId();
//			DarwinCore twin = this.findByLocalId(localId, resourceId);
//			if (twin != null){				
//				throw new EntityExistsException(String.format("DarwinCoreRecord must have a unique localId within a resource. But localId %s exists already for resourceId %s",localId, resourceId));
//			}
//		}
		return super.save(dwc);
	}
}
