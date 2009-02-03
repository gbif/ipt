package org.gbif.provider.service;

import java.io.File;
import java.io.IOException;

import org.gbif.provider.model.DataResource;

public interface DataArchiveManager {
	public File createArchive(DataResource resource) throws IOException;
}
