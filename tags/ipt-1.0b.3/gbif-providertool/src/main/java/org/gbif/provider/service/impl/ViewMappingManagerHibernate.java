package org.gbif.provider.service.impl;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ExtensionRecordManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ViewMappingManagerHibernate extends GenericResourceRelatedManagerHibernate<ViewMappingBase>{
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	
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
		extensionRecordManager.removeAll(obj.getExtension(), obj.getResource().getId());
		super.remove(obj);
	}
	
}
