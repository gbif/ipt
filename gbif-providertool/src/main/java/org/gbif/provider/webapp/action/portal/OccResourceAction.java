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
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.util.Constants;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware {
	protected Map session;
	private OccurrenceResource occResource;
	private List<OccurrenceResource> resources;
	private Map<Integer, String> regionClasses = new TreeMap<Integer, String>();
	private Map<Integer, String> ranks = new TreeMap<Integer, String>();
	public String geoserverMapUrl = "http://chart.apis.google.com/chart?cht=t&chs=320x160&chd=s:_&chtm=world";

	
	public void prepare() {
		if (resource_id != null) {
			occResource = occResourceManager.get(resource_id);
			// update recently viewed resources in session
			updateRecentResouces();
		}
		// prepare select lists
		for (RegionType rt : RegionType.DARWIN_CORE_REGIONS){
			regionClasses.put(rt.ordinal(), rt.name());
		}
		for (Rank rt : Rank.DARWIN_CORE_RANKS){
			ranks.put(rt.ordinal(), rt.name());
		}
	}
	
	private void updateRecentResouces(){
		LabelValue res = new LabelValue(occResource.getTitle(), resource_id.toString());
		Queue<LabelValue> queue; 
		Object rr = session.get(Constants.RECENT_RESOURCES);
		if (rr != null && rr instanceof Queue){
			queue = (Queue) rr;
		}else{
			queue = new ConcurrentLinkedQueue<LabelValue>(); 
		}
		// remove old entry from queue if it existed before and insert at tail again
		queue.remove(res);
		queue.add(res);
		if (queue.size()>10){
			// only remember last 10 resources
			queue.remove();
		}
		// save back to session
		log.debug("Recently viewed resources: "+queue.toString());
		session.put(Constants.RECENT_RESOURCES, queue);
	}
		
	public String execute() {
		return SUCCESS;
	}

	public String list() {
		resources = occResourceManager.getAll();
		return SUCCESS;
	}	
	
	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public void setOccResource(OccurrenceResource occResource) {
		this.occResource = occResource;
	}

	public void setSession(Map session) {
		this.session = session;
	}

	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
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

}