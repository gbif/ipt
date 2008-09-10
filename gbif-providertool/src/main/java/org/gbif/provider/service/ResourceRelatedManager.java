package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceRelatedObject;
import org.gbif.provider.model.TreeNode;

public interface ResourceRelatedManager<T extends ResourceRelatedObject> extends GenericManager<T> {
	/**
	 * Delete all records linked to a given resource
	 * @param resource that contains the records to be removed
	 * @return number of deleted instances of T
	 */
	int removeAll(Resource resource);
}
