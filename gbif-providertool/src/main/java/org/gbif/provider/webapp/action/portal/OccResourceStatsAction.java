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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.service.OccResourceManager;
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
public class OccResourceStatsAction extends ResourceStatsBaseAction<OccurrenceResource> implements Preparable {	
	private OccResourceManager occResourceManager;

	@Autowired
	public OccResourceStatsAction(OccResourceManager occResourceManager){
		this.resourceManager=occResourceManager;
		this.occResourceManager=occResourceManager;
	}
	
	
	
	public String statsByRegion() {
		recordAction="occRegion";
		types = RegionType.DARWIN_CORE_REGIONS.toArray();
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
		List<Rank> ranks = new ArrayList<Rank>(Rank.DARWIN_CORE_HIGHER_RANKS);
		ranks.add(Rank.TerminalTaxon);
		types = ranks.toArray();
		if (!useCachedImage(ImageType.ChartByTaxon)){
			Rank rnk = Rank.getByInt(type);
			data = occResourceManager.occByTaxon(resource_id, rnk);
			String url = occResourceManager.occByTaxonPieUrl(data, rnk, width, height, title);
			cacheImage(ImageType.ChartByTaxon, url);
		}
		return PIE_RESULT;
	}
	
	public String statsByHost() {
		types = HostType.values();
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
		
}