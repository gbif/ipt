/**
 * 
 */
package org.gbif.provider.service;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
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
	public void buildDataResourceIndex(DataResource resource);
	
	/**
	 * (Re)builds the whole metadata index spanning all resources (e.g. metadata only)
	 */
	public void buildResourceIndex();

	/** (Re)builds the metadata index for a single resource 
	 * @param resourceId
	 */
	public void buildResourceIndex(Long resourceId);
	
	/**
	 * @param resourceId To search within
	 * @param q unparsed query string
	 * @return List of core entity GUIDs
	 */
	public List<String> search(Long resourceId, String q);
	
	/** do full text search on metadata of all published resources
	 * @param q unparsed query string
	 * @return list of GUIDs matching
	 */
	public List<String> search(String q);
	
	/** do full text search on metadata of all resources accessible to a given user
	 * @param q unparsed query string
	 * @return list of GUIDs for resources matching
	 */
	public List<String> search(String q, Long userId);
}
