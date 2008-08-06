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
import org.appfuse.model.LabelValue;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.job.JobUtils;
import org.gbif.provider.job.OccUploadBaseJob;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.StatsCount;
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
import com.opensymphony.xwork2.config.entities.Parameterizable;

/**
 * ActionClass to generate the data for a single occurrence resource statistic with chart image and data
 * Can be parameterized with:
 *  "width" : image width. Defaults to 320
 *  "height" : image height. Defaults to 160
 *  "title" : set title in image? Defaults to false
 * @author markus
 *
 */
public class OccResourceStatsAction extends BaseOccurrenceResourceAction implements Preparable {
	public static int DEFAULT_WIDTH = 320;
	public static int DEFAULT_HEIGHT = 160;

	private OccurrenceResource occResource;
	private int region = 1;
	private int rank = 1;
	// chart image size
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	// set title in chart image
	private boolean title = false;
	public String chartUrl;
	public List<StatsCount> data;

	public void prepare() {
		if (resource_id != null) {
			occResource = occResourceManager.get(resource_id);
		}
	}

	public String statsByCountry() {
		return statsByRegion(RegionType.Country);
	}	
	public String statsByRegion() {
		RegionType r = RegionType.getByInt(region);
		return statsByRegion(r);
	}	
	private String statsByRegion(RegionType reg) {
		data = occResourceManager.occByRegion(resource_id, reg);
		chartUrl = occResourceManager.occByRegionPieUrl(data, reg, width, height, title);
		return SUCCESS;
	}
	public String statsByCountrySpecies() {
		//FIXME: this should be number of species per country...
		data = occResourceManager.occByRegion(resource_id, RegionType.Country);
		chartUrl = occResourceManager.occByRegionPieUrl(data, RegionType.Country, width, height, title);
		return SUCCESS;
	}	

	public String statsByTaxon() {
		Rank rnk = Rank.getByInt(rank);
		data = occResourceManager.occByTaxon(resource_id, rnk);
		chartUrl = occResourceManager.occByTaxonPieUrl(data, rnk, width, height, title);
		return SUCCESS;
	}
	
	public String statsByTop10Taxa() {
		data = occResourceManager.top10Taxa(resource_id);
		chartUrl = occResourceManager.top10TaxaPieUrl(data, width, height, title);
		return SUCCESS;
	}
	
	public String statsByInstitution() {
		data = occResourceManager.occByInstitution(resource_id);
		chartUrl = occResourceManager.occByInstitutionPieUrl(data, width, height, title);
		return SUCCESS;
	}

	public String statsByCollection() {
		data = occResourceManager.occByCollection(resource_id);
		chartUrl = occResourceManager.occByCollectionPieUrl(data, width, height, title);
		return SUCCESS;
	}

	public String statsByBasisOfRecord() {
		data = occResourceManager.occByBasisOfRecord(resource_id);
		chartUrl = occResourceManager.occByBasisOfRecordPieUrl(data, width, height, title);
		return SUCCESS;
	}

	public String statsByDateColected() {
		data = occResourceManager.occByDateColected(resource_id);
		chartUrl = occResourceManager.occByDateColectedUrl(data, width, height, title);
		return SUCCESS;
	}
	
	
	//
	// GETTER SETTER SECTION
	//
	
	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public void setOccResource(OccurrenceResource occResource) {
		this.occResource = occResource;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getChartUrl() {
		return chartUrl;
	}

	public List<StatsCount> getData() {
		return data;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setTitle(boolean title) {
		this.title = title;
	}

}