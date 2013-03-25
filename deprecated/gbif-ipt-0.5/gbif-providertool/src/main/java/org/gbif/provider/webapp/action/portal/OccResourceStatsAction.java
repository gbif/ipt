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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

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
	@Autowired
	private ImageCacheManager imageCacheManager;
	
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
	private int type;
	// list of all avilable types as Enums
	public List types;
	// map focus
	private String area;
	public String chartUrl;
	// the last part of the action name as matched with the struts.xml expression. Used to link further
	public String action="";
	public String recordAction;
	public Long filter;
	public List<StatsCount> data;

	public void prepare() {
		area = GeographicalArea.WORLD.toString();
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
		}
	}

	
	
	public String statsByRegion() {
		recordAction="occRegion";
		types = RegionType.DARWIN_CORE_REGIONS;
		if (!useCachedImage(ImageType.ChartByRegion)){
			RegionType reg = RegionType.getByInt(type);
			data = occResourceManager.occByRegion(resource_id, reg, filter);
			String url = occResourceManager.occByRegionPieUrl(data, reg, width, height, title);
			cacheImage(ImageType.ChartByRegion, url);
		}
		return PIE_RESULT;
	}

	public String statsByTaxon() {
		recordAction="occTaxon";
		types = new ArrayList<Rank>(Rank.DARWIN_CORE_HIGHER_RANKS);
		types.add(Rank.TerminalTaxon);
		if (!useCachedImage(ImageType.ChartByTaxon)){
			Rank rnk = Rank.getByInt(type);
			data = occResourceManager.occByTaxon(resource_id, rnk);
			String url = occResourceManager.occByTaxonPieUrl(data, rnk, width, height, title);
			cacheImage(ImageType.ChartByTaxon, url);
		}
		return PIE_RESULT;
	}
	
	public String statsByHost() {
		types = HostType.HOSTING_BODIES;
		if (!useCachedImage(ImageType.ChartByHost)){
			HostType ht = HostType.getByInt(type);
			data = occResourceManager.occByHost(resource_id, ht);
			String url = occResourceManager.occByHostPieUrl(data, ht, width, height, title);
			cacheImage(ImageType.ChartByHost, url);
		}
		return PIE_RESULT;
	}

	public String statsByBasisOfRecord() {
		if (!useCachedImage(ImageType.ChartByBasisOfRecord)){
			data = occResourceManager.occByBasisOfRecord(resource_id);
			String url = occResourceManager.occByBasisOfRecordPieUrl(data, width, height, title);
			cacheImage(ImageType.ChartByBasisOfRecord, url);
		}
		return PIE_RESULT;
	}


	public String statsByDateColected() {
		if (!useCachedImage(ImageType.ChartByDateCollected)){
			data = occResourceManager.occByDateColected(resource_id);
			String url = occResourceManager.occByDateColectedUrl(data, width, height, title);
			cacheImage(ImageType.ChartByDateCollected, url);
		}
		return CHART_RESULT;
	}
	
	
	
	// MAPS
	public String statsByCountry() {
		setMapSize();
		if (type==2){
			//TODO: link to list of taxon found in that country
			recordAction="";
		}else{
			recordAction="occRegion";
		}
		if (!useCachedImage(ImageType.CountryMapOfOccurrence)){
			String url;
			if (type==2){
				data = occResourceManager.taxaByRegion(resource_id, RegionType.Country);
				url = occResourceManager.taxaByCountryMapUrl(occResourceManager.getMapArea(area), data, width, height);
			}else{
				data = occResourceManager.occByRegion(resource_id, RegionType.Country, filter);
				url = occResourceManager.occByCountryMapUrl(occResourceManager.getMapArea(area), data, width, height);
			}
			cacheImage(ImageType.CountryMapOfOccurrence, url);
		}
		return MAP_RESULT;
	}
	
	
	// HELPER
	private void setMapSize(){
		if (zoom) {
			width=ZOOM_MAP_WIDTH;
			height=ZOOM_MAP_HEIGHT;
		}
	}

	private boolean useCachedImage(ImageType type) {
		File chartCache = getCachedImage(type);
		if (!zoom && chartCache.exists()){
			chartUrl = getCachedImageURL(type).toString();
			return true;
		}else{
			return false;
		}
	}
	private File getCachedImage(ImageType chartType){
		return imageCacheManager.getCachedImage(resource_id, chartType, type, area, width, height);		
	}
	private URL getCachedImageURL(ImageType chartType){
		return imageCacheManager.getCachedImageURL(resource_id, chartType, type, area, width, height);
	}
	private String cacheImage(ImageType chartType, String url){
		chartUrl = cfg.getResourceCacheUrl(resource_id, imageCacheManager.cacheImage(resource_id, chartType, type, area, width, height,  url)).toString();
		return chartUrl;
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