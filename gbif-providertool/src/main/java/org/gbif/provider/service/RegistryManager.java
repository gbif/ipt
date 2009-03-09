package org.gbif.provider.service;

import java.net.URI;
import java.util.List;

public interface RegistryManager {
	public List<URI> listExtensions();
	public boolean testLogin();
	public boolean registerOrg();
	public boolean registerIPT();
	public boolean registerResource(Long resourceId);
}
