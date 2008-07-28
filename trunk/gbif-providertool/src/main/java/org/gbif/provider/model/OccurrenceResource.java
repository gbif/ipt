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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.gbif.provider.util.ConfigUtil;
import org.gbif.provider.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MapKeyManyToMany;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DatasourceBasedResource {
	public static final Long EXTENSION_ID = 1l;
	
	public static OccurrenceResource newInstance(Extension core){
		OccurrenceResource resource =  new OccurrenceResource();
		// ensure that core mapping exists
		CoreViewMapping coreVM = new CoreViewMapping();
		coreVM.setExtension(core);
		resource.setCoreMapping(coreVM);
		return resource;
	}

	@Override
	@Transient
	public File getDataDir(){
		return new File(super.getDataDir(), "occ");
	}

	@Transient
    public File getDumpArchiveFile(){
		File file = new File(getDataDir(), "data.zip");
		return file;    	
    }

	@Transient
    public String getDumpArchiveUrl(){
		return String.format("%s/occ/data.zip", getResourceBaseUrl());
    }

	@Transient
    public File getDumpFile(Extension extension) throws IOException{    	
		File file = new File(getDataDir(), String.format("%s.txt", extension.getTablename()));
		return file;
	}    

	@Transient
	public String getTapirEndpoint(){
		return String.format("%s/tapir", getResourceBaseUrl());
	}
	
	@Transient
	public String getWfsEndpoint(){
		return String.format("%s/wfs", getResourceBaseUrl());
	}
	
	@Transient
	public String getRecordResolverEndpoint(){
		return String.format("%s/detail", getResourceBaseUrl());
	}
    

    
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}
}
