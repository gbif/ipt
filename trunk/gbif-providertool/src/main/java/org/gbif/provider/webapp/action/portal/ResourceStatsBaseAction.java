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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.webapp.action.BaseChecklistResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
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
public abstract class ResourceStatsBaseAction<T extends Resource> extends BaseResourceAction<T> implements Preparable {
	@Autowired
	private ImageCacheManager imageCacheManager;
	
	public static int DEFAULT_WIDTH = 320;
	public static int DEFAULT_HEIGHT = 160;
	public static int ZOOM_CHART_WIDTH = 680;
	public static int ZOOM_CHART_HEIGHT = 400;
	public static int ZOOM_MAP_WIDTH = 440;
	public static int ZOOM_MAP_HEIGHT = 220;
	
	public static final String MAP_RESULT = "map";
	public static final String PIE_RESULT = "pie";
	public static final String CHART_RESULT = "chart";

	// chart image size
	protected int width = DEFAULT_WIDTH;
	protected int height = DEFAULT_HEIGHT;
	// set title in chart image
	protected boolean title = false;
	// use zoom size instead of default?
	protected boolean zoom = false;
	// subtype of what to select. eg rank (family) or regionClass (continent)
	protected int type;
	// list of all avilable types as Enums
	protected Object[] types;
	// map focus
	protected String area;
	protected String chartUrl;
	// the last part of the action name as matched with the struts.xml expression. Used to link further
	protected String statsBy="";
	protected String resourceClass="occ";
	protected String recordAction;
	protected Long filter;
	protected List<StatsCount> data;

	public void prepare() {
		area = GeographicalArea.WORLD.toString();
		super.prepare();
	}
	
	
	// HELPER
	protected void setMapSize(){
		if (zoom) {
			width=ZOOM_MAP_WIDTH;
			height=ZOOM_MAP_HEIGHT;
		}
	}

	protected boolean useCachedImage(ImageType type) {
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
	protected String cacheImage(ImageType chartType, String url){
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

	public String getStatsBy() {
		return statsBy;
	}
	public void setStatsBy(String statsBy) {
		this.statsBy = statsBy;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object[] getTypes() {
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




	public String getResourceClass() {
		return resourceClass;
	}




	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}
		
}