package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;

public enum AnnotationType {
	Resource,
	WrongDatatype,
	BadReference,
	BadCoreRecord,
	BadExtensionRecord,
	UnknownVocTerm,
	TrimmedData;	
	
}
