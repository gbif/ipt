package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ViewMappingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ViewMappingManagerHibernate extends GenericResourceRelatedManagerHibernate<ExtensionMapping> implements ViewMappingManager{
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;
	
	public ViewMappingManagerHibernate() {
		super(ExtensionMapping.class);
	}

	@Override
	public int removeAll(Resource resource) {
		// make sure all existing extension records are removed too!
		List<ExtensionMapping> views = this.getAll(resource.getId());
		int i = 0;
		for (ExtensionMapping vm : views){
			remove(vm, true);
			i++;
		}
		return i;
	}

	@Override
	public void remove(ExtensionMapping obj) {
		remove(obj, false);
	}
	
	private void remove(ExtensionMapping obj, boolean force) {
		// cant delete core extension mappings unless forced. They will just be emptied to look like new ones
		if (obj.isCore() && !force){
			// reset core mapping
			obj.reset();
			this.save(obj);
			// remove property mappings for this view
			List<PropertyMapping> pms = obj.getPropertyMappingsSorted();
			for (PropertyMapping pm : pms){
				propertyMappingManager.remove(pm);
			}
		}else{
			DataResource res = obj.getResource();
			res.removeExtensionMapping(obj);
			super.remove(obj);
			universalSave(res);
		}
	}
	

}
