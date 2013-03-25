/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;

public interface GenericResourceManager<T extends Resource> extends GenericManager<T> {
	/**
	 * Return all resources created by that user
	 * @param userId
	 * @return
	 */
	public List<T> getResourcesByUser(Long userId);

	/** retrieve all resource IDs that have been published already
	 * @return list of resourceIDs
	 */
	public List<Long> getPublishedResourceIDs();
	public List<T> getPublishedResources();
	
	/** get latest modified resources
	 * @param startPage starting page, first page = 1
	 * @param pageSize
	 * @return
	 */
	List<T> latest(int startPage, int pageSize);

	/** Publishes a resource, creating a new EML document version
	 * and registering the resource with GBIF if not already registered. 
	 * Also tries to write/update the geoserver entry and lucene index (which doesnt happen through simple saves)
	 * @param resourceId
	 */
	public Resource publish(Long resourceId);
	
	/** Unpublishes a resource, i.e. removes the GBIF registry entry
	 * and flag the resource object & lucene index entry 
	 * so it doesnt show up in the public portal / searches anymore.
	 * Also removes the geoserver entry in case of occurrence resources
	 *  
	 * leaves all archived EML documents, but doesnt advertise them anymore. 
	 * @param resourceId
	 */
	public void unPublish(Long resourceId);
	
	/**
	 * Get resource by GUID
	 * @param guid of resource to be returned
	 * @return
	 */
	public T get(String guid);

}
