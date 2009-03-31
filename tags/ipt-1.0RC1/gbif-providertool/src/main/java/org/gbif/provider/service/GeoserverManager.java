package org.gbif.provider.service;

import java.io.IOException;

import org.gbif.provider.model.OccurrenceResource;

public interface GeoserverManager {

	public String buildFeatureTypeDescriptor(OccurrenceResource resource);

	public void removeFeatureType(OccurrenceResource resource)
			throws IOException;

	public void updateFeatureType(OccurrenceResource resource)
			throws IOException;

	public void updateCatalog() throws IOException;

	public void updateGeowebcache(OccurrenceResource resource);

	public boolean login(String username, String password, String geoserverURL);

	public void reloadCatalog() throws IOException;

}