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

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

import com.googlecode.gchartjava.GeographicalArea;
import com.opensymphony.xwork2.Preparable;

/**
 * ActionClass to generate the data for a single occurrence resource statistic with chart image and data
 * Can be parameterized with:
 *  "zoom" : return the largest image possible if true. Defaults to false
 *  "title" : set title in image? Defaults to false
 * @author markus
 *
 */
public class OccResourceStatsAction extends BaseOccurrenceResourceAction implements Preparable {
	public static int DEFAULT_WIDTH = 320;
	public static int DEFAULT_HEIGHT = 160;
	public static int ZOOM_CHART_WIDTH = 700;
	public static int ZOOM_CHART_HEIGHT = 400;
	public static int ZOOM_MAP_WIDTH = 440;
	public static int ZOOM_MAP_HEIGHT = 220;
	
	public static final String MAP_RESULT = "map";
	public static final String PIE_RESULT = "pie";
	public static final String CHART_RESULT = "chart";

	// chart image size
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	// set title in chart image
	private boolean title = false;
	// use zoom size instead of default?
	private boolean zoom = false;
	// subtype of what to select. eg rank (family) or regionClass (continent)
	private int type = 0;
	// list of all avilable types as Enums
	public List types;
	// map focus
	private String area = GeographicalArea.WORLD.toString();
	public String chartUrl;
	// the last part of the action name as matched with the struts.xml expression. Used to link further
	public String action="";
	public String recordAction;
	public Long filter;
	public List<StatsCount> data;

	public void prepare() {
		if (resource_id != null) {
			occResource = occResourceManager.get(resource_id);
		}
	}

	public String statsByRegion() {
		recordAction="occRegion";
		RegionType reg = RegionType.getByInt(type);
		types = RegionType.DARWIN_CORE_REGIONS;
		data = occResourceManager.occByRegion(resource_id, reg, filter);
		chartUrl = occResourceManager.occByRegionPieUrl(data, reg, width, height, title);
		return PIE_RESULT;
	}

	public String statsByTaxon() {
		recordAction="occTaxon";
		Rank rnk = Rank.getByInt(type);
		types = new ArrayList<Rank>(Rank.DARWIN_CORE_HIGHER_RANKS);
		types.add(Rank.TerminalTaxon);
		data = occResourceManager.occByTaxon(resource_id, rnk);
		chartUrl = occResourceManager.occByTaxonPieUrl(data, rnk, width, height, title);
		return PIE_RESULT;
	}
	
	public String statsByHost() {
		HostType ht = HostType.getByInt(type);
		types = HostType.HOSTING_BODIES;
		data = occResourceManager.occByHost(resource_id, ht);
		chartUrl = occResourceManager.occByHostPieUrl(data, ht, width, height, title);
		return PIE_RESULT;
	}

	public String statsByBasisOfRecord() {
		data = occResourceManager.occByBasisOfRecord(resource_id);
		chartUrl = occResourceManager.occByBasisOfRecordPieUrl(data, width, height, title);
		return PIE_RESULT;
	}

	public String statsByDateColected() {
		data = occResourceManager.occByDateColected(resource_id);
		chartUrl = occResourceManager.occByDateColectedUrl(data, width, height, title);
		return CHART_RESULT;
	}
	
	
	
	// MAPS
	public String statsByCountry() {
		recordAction="occRegion";
		setMapSize();
		data = occResourceManager.occByRegion(resource_id, RegionType.Country, filter);
		chartUrl = occResourceManager.occByCountryMapUrl(occResourceManager.getMapArea(area), data, width, height);
		return MAP_RESULT;
	}	
	public String statsByTaxaPerCountry() {
		setMapSize();
		data = occResourceManager.taxaByRegion(resource_id, RegionType.Country);
		chartUrl = occResourceManager.taxaByCountryMapUrl(occResourceManager.getMapArea(area), data, width, height);
		return MAP_RESULT;
	}	

	private void setMapSize(){
		if (zoom) {
			width=ZOOM_MAP_WIDTH;
			height=ZOOM_MAP_HEIGHT;
		}
	}

	
	
	//
	// GETTER SETTER SECTION
	//

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

	public void setZoom(boolean zoom) {
		this.zoom = zoom;
		if (zoom) {
			// use chart default zoom. map methods have to set sizes themselves
			width=ZOOM_CHART_WIDTH;
			height=ZOOM_CHART_HEIGHT;
		}
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<Enum> getTypes() {
		return types;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getRecordAction() {
		return recordAction;
	}

	public void setFilter(Long filter) {
		this.filter = filter;
	}
		
}