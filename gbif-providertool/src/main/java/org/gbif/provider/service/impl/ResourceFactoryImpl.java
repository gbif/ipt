package org.gbif.provider.service.impl;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourceFactoryImpl implements ResourceFactory{
	private GenericManagerHibernate<Extension> extensionManager;

	private ResourceFactoryImpl(GenericManagerHibernate<Extension> extensionManager) {
		super();
		this.extensionManager = extensionManager;
	}


	public OccurrenceResource newOccurrenceResourceInstance(){
		Extension core = extensionManager.get(OccurrenceResource.EXTENSION_ID);
		OccurrenceResource resource =  OccurrenceResource.newInstance(core);
		return resource;
	}
	
	public ChecklistResource newChecklistResourceInstance(){
		Extension core = extensionManager.get(ChecklistResource .EXTENSION_ID);
		ChecklistResource resource =  ChecklistResource.newInstance(core);
		return resource;
	}

	public Resource newResourceInstance(){
		Resource resource = new Resource();
		return resource;
	}
}
