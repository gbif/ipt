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

package org.gbif.provider.webapp.action.portal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.job.JobUtils;
import org.gbif.provider.job.OccUploadBaseJob;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.GPieBuilder;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable {
	private UploadEventManager uploadEventManager;	
	private OccurrenceResource occResource;
	private List<RegionType> locTypes;
	private RegionType locDefault;
	private List<Rank> taxTypes;
	private Rank taxDefault;
	public String occByRegionUrl;
	public String occByTaxonUrl;
	public String occByTop10TaxaUrl;
	public String occByInstitutionUrl;
	public String occByCollectionUrl;
	public String occByBasisOfRecordUrl;

	public void prepare() {
		if (resource_id != null) {
			occResource = occResourceManager.get(resource_id);
		}
		locTypes=RegionType.DARWIN_CORE_REGIONS;
		taxTypes=Rank.DARWIN_CORE_RANKS;
	}

	public String execute() {
		locDefault = RegionType.Country;
		taxDefault = Rank.Kingdom;
		occByRegionUrl = occResourceManager.occByRegionPieUrl(resource_id, locDefault, 320, 160, false);
		occByTaxonUrl = occResourceManager.occByTaxonPieUrl(resource_id, taxDefault, 320, 160, false);
		occByTop10TaxaUrl = occResourceManager.top10TaxaPieUrl(resource_id, 320, 160, false);
//		occByInstitutionUrl = occResourceManager.occByInstitutionPieUrl(resource_id);
//		occByCollectionUrl = occResourceManager.occByCollectionPieUrl(resource_id);
//		occByBasisOfRecordUrl = occResourceManager.occByBasisOfRecordPieUrl(resource_id);
		return SUCCESS;
	}

	
	
	public void setUploadEventManager(UploadEventManager uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public void setOccResource(OccurrenceResource occResource) {
		this.occResource = occResource;
	}

	public List getLocTypes() {
		return locTypes;
	}

	public List getTaxTypes() {
		return taxTypes;
	}

	public RegionType getLocDefault() {
		return locDefault;
	}

	public Rank getTaxDefault() {
		return taxDefault;
	}

	public String getOccByRegionUrl() {
		return occByRegionUrl;
	}

	public String getOccByTaxonUrl() {
		return occByTaxonUrl;
	}

	public String getOccByTop10TaxaUrl() {
		return occByTop10TaxaUrl;
	}

	public String getOccByInstitutionUrl() {
		return occByInstitutionUrl;
	}

	public String getOccByCollectionUrl() {
		return occByCollectionUrl;
	}

	public String getOccByBasisOfRecordUrl() {
		return occByBasisOfRecordUrl;
	}
	
	
}