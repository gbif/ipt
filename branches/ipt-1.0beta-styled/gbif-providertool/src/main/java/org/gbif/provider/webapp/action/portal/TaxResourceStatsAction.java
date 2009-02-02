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

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.webapp.action.BaseChecklistResourceAction;
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
public class TaxResourceStatsAction extends ResourceStatsBaseAction<ChecklistResource> implements Preparable {
	private ChecklistResourceManager checklistResourceManager;

	@Autowired
	public TaxResourceStatsAction(ChecklistResourceManager checklistResourceManager){
		this.resourceManager=checklistResourceManager;
		this.checklistResourceManager=checklistResourceManager;
	}

	public String statsByTaxon() {
		recordAction="taxDetail";
		types = Rank.COMMON_RANKS.toArray(new Object[1]);

		if (!useCachedImage(ImageType.ChartByTaxon)){
			Rank rnk = Rank.getByInt(type);
			data = checklistResourceManager.taxByTaxon(resource_id, rnk);
			String url = checklistResourceManager.taxByTaxonPieUrl(data, rnk, width, height, title);
			cacheImage(ImageType.ChartByTaxon, url);
		}
		return PIE_RESULT;
	}
	
	public String statsByRank() {
		recordAction="taxListByRank";
		if (!useCachedImage(ImageType.ChartByRank)){
			data = checklistResourceManager.taxByRank(resource_id);
			String url = checklistResourceManager.taxByRankPieUrl(data, width, height, title);
			cacheImage(ImageType.ChartByRank, url);
		}
		return PIE_RESULT;
	}

	public String statsByStatus() {
		recordAction="taxListByStatus";
		types = StatusType.values();
		if (!useCachedImage(ImageType.ChartByStatus)){
			StatusType st = StatusType.getByInt(type);
			data = checklistResourceManager.taxByStatus(resource_id, st);
			String url = checklistResourceManager.taxByStatusPieUrl(data, st, width, height, title);
			cacheImage(ImageType.ChartByStatus, url);
		}
		return PIE_RESULT;
	}

		
}