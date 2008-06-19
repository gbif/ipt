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

package org.gbif.provider.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.gbif.provider.webapp.Constants;

/**
 * External datasource driven resource representing a taxonomic checklist
 * @author markus
 *
 */
@Entity
public class ChecklistResource extends DatasourceBasedResource {

	@Transient
	public ViewMapping getCoreMapping() {
		return this.getMappings().get(Constants.CHECKLIST_EXTENSION_ID);
	}
	
	@Transient
	public Collection<ViewMapping> getExtensionMappings() {
		Map<Long, ViewMapping> extMappings = new HashMap<Long, ViewMapping>();
		extMappings.putAll(this.getMappings());
		extMappings.remove(Constants.CHECKLIST_EXTENSION_ID);
		return extMappings.values();
	}		
}
