package org.gbif.provider.service;

import java.util.List;

import org.appfuse.service.GenericManager;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.model.OccurrenceResource;

public interface ViewMappingManager extends GenericManager<ViewMapping, Long> {
	public List<ViewMapping> findByResource(Long resourceId);
	
}
