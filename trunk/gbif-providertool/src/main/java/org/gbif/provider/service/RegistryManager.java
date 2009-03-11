package org.gbif.provider.service;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.gbif.provider.model.DataResource;

public interface RegistryManager {
	public boolean testLogin();
	public boolean registerOrg();
	public boolean registerIPT();
	public boolean registerResource(DataResource resource);
	
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
