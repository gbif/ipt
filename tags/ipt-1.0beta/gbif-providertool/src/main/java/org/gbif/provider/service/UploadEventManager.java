package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.UploadEvent;

public interface UploadEventManager extends GenericResourceRelatedManager<UploadEvent>{
	/**
	 * Return a string that represents the upload event statistics for a given resource 
	 * so that it can be used with the Google Charts API
	 * @See http://code.google.com/apis/chart/#chart_data
	 * @param resourceId
	 * @return
	 */
	public String getGoogleChartData(Long resourceId, int width, int height);
	
	/**
	 * Get all upload events for a given resource
	 * @param resourceId
	 * @return
	 */
	public List<UploadEvent> getUploadEventsByResource(Long resourceId);
	
}
