package org.gbif.provider.service;

import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;

public interface SourceManager extends GenericResourceRelatedManager<SourceBase> {
	public SourceFile getSourceByFilename(Long resourceId, String filename);
}
