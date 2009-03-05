package org.gbif.provider.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface RegistryManager {
	public List<URI> listExtensions();
	public Map<String, String> findOrganisations(String q);
	public boolean registerIPT();
	public boolean registerResource(Long resourceId);
}
