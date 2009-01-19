/**
 * 
 */
package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.DataResource;

/**
 * The full text indexing creation and searching. 
 * @author tim
 */
public interface FullTextSearchManager {
	
	/**
	 * Builds the indexes required for the data resource which may include taxonomic or occurrence
	 * sources
	 * @param resource to build
	 */
	public void buildDataResourceIndexes(DataResource resource);
	
	/**
	 * Builds the single index spanning all resources (e.g. metadata only)
	 */
	public void buildResourceIndexes();
	
	/**
	 * @param resourceId To search within
	 * @param q unparsed query string
	 * @return List of core entity IDs
	 */
	public List<Long> search(Long resourceId, String q);
}
