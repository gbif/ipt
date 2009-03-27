package org.gbif.provider.model.factory;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.util.Constants;

public class ResourceFactory {
	private GenericManager<Extension> extensionManager;

	public ResourceFactory(GenericManager<Extension> extensionManager) {
		super();
		this.extensionManager = extensionManager;
	}

	
	public OccurrenceResource newOccurrenceResourceInstance(){
		OccurrenceResource resource =  new OccurrenceResource();
		initCoreMapping(resource);
		return resource;
	}
	
	public ChecklistResource newChecklistResourceInstance(){
		ChecklistResource resource =  new ChecklistResource();
		initCoreMapping(resource);
		return resource;
	}
	private void initCoreMapping(DataResource resource){
		resource.resetCoreMapping();
		ExtensionMapping coreVM = new ExtensionMapping();
		Extension core = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);		
		coreVM.setResource(resource);
		coreVM.setExtension(core);
		resource.addExtensionMapping(coreVM);
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
