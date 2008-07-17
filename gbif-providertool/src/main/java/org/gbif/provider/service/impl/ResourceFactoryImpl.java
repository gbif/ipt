package org.gbif.provider.service.impl;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceFactoryImpl implements ResourceFactory{
	private GenericDaoHibernate<Extension, Long> extensionDao;

	private ResourceFactoryImpl(GenericDaoHibernate<Extension, Long> extensionDao) {
		super();
		this.extensionDao = extensionDao;
	}


	public OccurrenceResource newOccurrenceResourceInstance(){
		Extension core = extensionDao.get(OccurrenceResource.EXTENSION_ID);
		OccurrenceResource resource =  OccurrenceResource.newInstance(core);
		return resource;
	}
	
	public ChecklistResource newChecklistResourceInstance(){
		Extension core = extensionDao.get(ChecklistResource .EXTENSION_ID);
		ChecklistResource resource =  ChecklistResource.newInstance(core);
		return resource;
	}

	public Resource newResourceInstance(){
		Resource resource = new Resource();
		return resource;
	}
}
