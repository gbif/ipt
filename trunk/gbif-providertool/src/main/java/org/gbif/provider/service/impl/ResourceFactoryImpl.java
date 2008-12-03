package org.gbif.provider.service.impl;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ResourceFactory;

public class ResourceFactoryImpl implements ResourceFactory{
	private GenericManager<Extension> extensionManager;

	public ResourceFactoryImpl(GenericManager<Extension> extensionManager) {
		super();
		this.extensionManager = extensionManager;
	}

	
	public OccurrenceResource newOccurrenceResourceInstance(){
		Extension core = extensionManager.get(OccurrenceResource.CORE_EXTENSION_ID);
		OccurrenceResource resource =  OccurrenceResource.newInstance(core);
		return resource;
	}
	
	public ChecklistResource newChecklistResourceInstance(){
		Extension core = extensionManager.get(ChecklistResource.CORE_EXTENSION_ID);
		ChecklistResource resource =  ChecklistResource.newInstance(core);
		return resource;
	}

	public Resource newMetadataResourceInstance(){
		Resource resource = new Resource();
		return resource;
	}


	public Resource newResourceInstance(Class resourceClass) {
		Resource res = null;
		if (resourceClass.isAssignableFrom(OccurrenceResource.class)){
			res = newOccurrenceResourceInstance();
		}else if (resourceClass.isAssignableFrom(ChecklistResource.class)){
			res = newChecklistResourceInstance();
		}else{
			res = newMetadataResourceInstance();
		}
		return res;
	}
}
