package org.gbif.provider.service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.gbif.provider.model.ResourceMetadata;

public interface RegistryManager {
	public List<URI> listExtensions();
	public Map<String, ResourceMetadata> findOrganisations(String q);
	public String findOrganisationsAsJSON(String q);
	public boolean registerIPT();
	public boolean registerResource(Long resourceId);
}
