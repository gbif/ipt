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

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;

public interface GenericResourceManager<T extends Resource> extends GenericManager<T> {
	/**
	 * Return all resources created by that user
	 * @param userId
	 * @return
	 */
	public List<T> getResourcesByUser(Long userId);

	/**
	 * Get resource by GUID
	 * @param guid of resource to be returned
	 * @return
	 */
	public T get(String guid);

}
