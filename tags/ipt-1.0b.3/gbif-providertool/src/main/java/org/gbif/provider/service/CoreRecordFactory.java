package org.gbif.provider.service;

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;

public interface CoreRecordFactory {
	CoreRecord build(DataResource resource, ImportRecord iRec);
	DarwinCore build(OccurrenceResource resource, ImportRecord iRec);
	Taxon build(ChecklistResource resource, ImportRecord iRec);
	CoreRecord copyPersistentProperties(CoreRecord target, CoreRecord source);
}
