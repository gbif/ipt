package org.gbif.provider.service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.gbif.provider.model.ResourceMetadata;

public interface RegistryManager {
	public List<URI> listExtensions();
	public boolean registerOrg();
	public boolean registerIPT();
	public boolean registerResource(Long resourceId);
}
