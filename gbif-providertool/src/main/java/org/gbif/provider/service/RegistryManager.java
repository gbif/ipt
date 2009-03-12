package org.gbif.provider.service;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;

public interface RegistryManager {
	public boolean testLogin();
	
	public String registerOrg() throws RegistryException;
	public void updateOrg() throws RegistryException;
	
	public String registerIPT() throws RegistryException;
	public void updateIPT() throws RegistryException;
	
	public String registerResource(Resource resource) throws RegistryException;
	public void updateResource(Resource resource) throws RegistryException;
	public void deleteResource(Resource resource) throws RegistryException;
	
	/**
	 * Calls the central registry to receive a list of the Extensions that are available
	 * @return The (supposedly) publicly accessible URLs of the extensions
	 */
	public Collection<String> listAllExtensions();

	/**
	 * Calls the central registry to receive a list of the ThesaurusVocabularies that are available
	 * @return The (supposedly) publicly accessible URLs of the thesauri
	 */
	public Collection<String> listAllThesauri();
}
