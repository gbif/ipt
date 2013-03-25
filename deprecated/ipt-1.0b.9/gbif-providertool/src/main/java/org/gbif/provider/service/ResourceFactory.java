package org.gbif.provider.service;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;

public interface ResourceFactory {
	public OccurrenceResource newOccurrenceResourceInstance();
	
	public ChecklistResource newChecklistResourceInstance();

	public Resource newMetadataResourceInstance();

	public Resource newResourceInstance(Class resourceClass);

}
