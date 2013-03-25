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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.util.Constants;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable {
	@Autowired
	private MapUtil mapUtil;
	private List<OccurrenceResource> resources;
	private Map<Integer, String> countryClasses = new TreeMap<Integer, String>();
	private Map<Integer, String> regionClasses = new TreeMap<Integer, String>();
	private Map<Integer, String> ranks = new TreeMap<Integer, String>();
	private Map<Integer, String> hostTypes = new TreeMap<Integer, String>();
	public String geoserverMapUrl;
	public static int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public static int height = OccResourceStatsAction.DEFAULT_HEIGHT;

	
	public void prepare() {
		super.prepare();
		if (resource != null) {
			// geoserver map link
			geoserverMapUrl = mapUtil.getGeoserverMapUrl(resource_id, width, height, resource.getBbox(), null, null);
		}
		// prepare select lists
		countryClasses.put(1, "occurrences");
		countryClasses.put(2, "distinct taxa");
		for (RegionType rt : RegionType.DARWIN_CORE_REGIONS){
			regionClasses.put(rt.ordinal(), rt.name());
		}
		for (Rank rt : Rank.DARWIN_CORE_HIGHER_RANKS){
			ranks.put(rt.ordinal(), rt.name());
		}
		ranks.put(Rank.TerminalTaxon.ordinal(), "All Taxa");
		
		// hosting bodies
		for (HostType ht : HostType.HOSTING_BODIES){
			hostTypes.put(ht.ordinal(), ht.name());
		}
		
	}
	
		
	public String execute() {
		return SUCCESS;
	}

	public String list() {
		resources = occResourceManager.getAll();
		return SUCCESS;
	}	
	
	
	
	
	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
	}

	public Map<Integer, String> getCountryClasses() {
		return countryClasses;
	}

	public Map<Integer, String> getRegionClasses() {
		return regionClasses;
	}

	public Map<Integer, String> getRanks() {
		return ranks;
	}

	public List<OccurrenceResource> getResources() {
		return resources;
	}

	public Map<Integer, String> getHostTypes() {
		return hostTypes;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}

}