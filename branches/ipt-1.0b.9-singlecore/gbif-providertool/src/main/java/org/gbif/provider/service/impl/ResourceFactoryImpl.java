package org.gbif.provider.service.impl;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;

public class ResourceFactoryImpl implements ResourceFactory{
	private GenericManager<Extension> extensionManager;

	public ResourceFactoryImpl(GenericManager<Extension> extensionManager) {
		super();
		this.extensionManager = extensionManager;
	}

	
	public OccurrenceResource newOccurrenceResourceInstance(){
		OccurrenceResource resource =  new OccurrenceResource();
		resource.resetCoreMapping();
		Extension core = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
		resource.getCoreMapping().setExtension(core);
		return resource;
	}
	
	public ChecklistResource newChecklistResourceInstance(){
		ChecklistResource resource =  new ChecklistResource();
		resource.resetCoreMapping();
		Extension core = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
		resource.getCoreMapping().setExtension(core);
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
