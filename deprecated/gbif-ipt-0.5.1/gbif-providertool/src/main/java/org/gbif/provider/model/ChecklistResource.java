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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.util.Constants;

/**
 * External datasource driven resource representing a taxonomic checklist
 * @author markus
 *
 */
@Entity
public class ChecklistResource extends DataResource {
	//FIXME: create new extension for taxa. right now uses the dwc one so that tests dont fail...
	public static final Long CORE_EXTENSION_ID = 1L;
	
	public static ChecklistResource newInstance(Extension core){
		ChecklistResource resource =  new ChecklistResource();
		// ensure that core mapping exists
		ViewCoreMapping coreVM = new ViewCoreMapping();
		coreVM.setExtension(core);
		coreVM.setResource(resource);
		resource.setCoreMapping(coreVM);
		return resource;
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}

}
