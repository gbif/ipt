package org.gbif.provider.service;

import java.io.IOException;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;

public interface EmlManager {
	public Eml load(Resource resource);
	public void save(Eml eml);
	public Eml publishNewEmlVersion(Resource resource) throws IOException;
}
