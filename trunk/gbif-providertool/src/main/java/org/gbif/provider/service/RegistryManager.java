package org.gbif.provider.service;

import java.net.URI;
import java.util.List;

import org.gbif.provider.model.DataResource;

public interface RegistryManager {
	public List<URI> listExtensions();
	public boolean testLogin();
	public boolean registerOrg();
	public boolean registerIPT();
	public boolean registerResource(DataResource resource);
}
