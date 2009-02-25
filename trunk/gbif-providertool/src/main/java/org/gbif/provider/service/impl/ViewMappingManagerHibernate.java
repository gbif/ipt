package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ViewMappingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ViewMappingManagerHibernate extends GenericResourceRelatedManagerHibernate<ViewMappingBase> implements ViewMappingManager{
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;
	
	public ViewMappingManagerHibernate() {
		super(ViewMappingBase.class);
	}

	@Override
	public int removeAll(Resource resource) {
		// make sure all existing extension records are removed too!
		DataResource res = (DataResource) resource;
		int i = 0;
		for (ViewMappingBase vm : res.getExtensionMappings()){
			remove(vm);
			i++;
		}
		return i;
	}

	@Override
	public void remove(ViewMappingBase obj) {
		// make sure all existing extension records are removed too!
		if (!obj.isCore()){
			extensionRecordManager.removeAll(obj.getExtension(), obj.getResource().getId());
		}
		// remove links from resource
		DataResource res = obj.getResource();
		if (res.getCoreMapping().equals(obj)){
			// its a core mapping. replace with empty one
			res.resetCoreMapping();
		}else{
			res.removeExtensionMapping(obj);
		}		
		universalSave(res);
		// remove property mappings for this view
		List<PropertyMapping> pms = obj.getPropertyMappingsSorted();
		for (PropertyMapping pm : pms){
			propertyMappingManager.remove(pm);
		}
		super.remove(obj);
	}
}
