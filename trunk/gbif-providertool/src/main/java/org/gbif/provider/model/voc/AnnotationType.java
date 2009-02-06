package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;

public enum AnnotationType {
	Resource,
	WrongDatatype,
	BadReference,
	BadCoreRecord,
	BadExtensionRecord,
	UnknownVocTerm,
	TrimmedData,	
	HumanComment;	

    public static final Map<String, String> htmlSelectMap;
	static
	{
		Map<String, String> map = new HashMap<String, String>();
		for (AnnotationType et : AnnotationType.values()){
			map.put(et.toString(), "annotationType."+et.toString());
		}
		htmlSelectMap = Collections.unmodifiableMap(map);  			
	}
}
