package org.gbif.provider.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;

public interface DataArchiveManager {
	public File packageArchive(DataResource resource) throws IOException;

}
