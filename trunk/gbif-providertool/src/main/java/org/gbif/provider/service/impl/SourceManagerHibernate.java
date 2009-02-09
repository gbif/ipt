package org.gbif.provider.service.impl;

import java.util.List;
import java.util.Set;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ViewMappingManager;
import org.springframework.beans.factory.annotation.Autowired;

public class SourceManagerHibernate extends	GenericResourceRelatedManagerHibernate<SourceBase> implements SourceManager{
	@Autowired
	private ViewMappingManager viewMappingManager;
	
	public SourceManagerHibernate() {
		super(SourceBase.class);
		// TODO Auto-generated constructor stub
	}

	public SourceFile getSourceByFilename(Long resourceId, String filename) {
		return (SourceFile) query("from SourceFile s WHERE s.resource.id = :resourceId and s.name = :filename")
			        .setLong("resourceId", resourceId)
			        .setString("filename", filename)
	        		.uniqueResult();
	}

	@Override
	public void remove(SourceBase obj) {
		// also remove all ViewMappings that are based on this source
		List<ViewMappingBase> views = viewMappingManager.getAll(obj.getResource().getId());
		for (ViewMappingBase vm : views){
			if (vm.getSource().equals(obj)){
				// view mapping uses this source, so also delete it!
				viewMappingManager.remove(vm);
			}
		}
		// finally remove the source itself
		super.remove(obj);
	}

}
