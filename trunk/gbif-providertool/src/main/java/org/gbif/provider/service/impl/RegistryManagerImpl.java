package org.gbif.provider.service.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.service.RegistryManager;

public class RegistryManagerImpl implements RegistryManager{
	protected final Log log = LogFactory.getLog(getClass());

	public Map<String, String> findOrganisations(String q) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<URI> listExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean registerIPT() {
		log.warn("IPT service registration not implemented");
		return false;
	}

	public boolean registerResource(Long resourceId) {
		// TODO Auto-generated method stub
		return false;
	}

}
