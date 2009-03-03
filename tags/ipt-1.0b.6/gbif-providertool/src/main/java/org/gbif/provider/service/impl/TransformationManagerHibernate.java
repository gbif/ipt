package org.gbif.provider.service.impl;

import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.TransformationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TransformationManagerHibernate extends GenericResourceRelatedManagerHibernate<Transformation> implements TransformationManager{
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;

	public TransformationManagerHibernate() {
		super(Transformation.class);
	}

	@Override
	public void remove(Transformation obj) {
		// make sure no propertyMapping references this transformation
		for (PropertyMapping pm : obj.getPropertyMappings()){
			pm.setTermTransformation(null);
			propertyMappingManager.save(pm);
		}
		super.remove(obj);
	}

	
}
