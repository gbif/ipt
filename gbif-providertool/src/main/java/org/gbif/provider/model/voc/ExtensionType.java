package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;

public enum ExtensionType {
	Occurrence(1l, OccurrenceResource.class),
	Checklist(7l, ChecklistResource.class);	
	
	public static ExtensionType byResourceClass(Class resourceClass){
		for (ExtensionType et : ExtensionType.values()){
			if (et.resourceClass.equals(resourceClass)){
				return et;
			}
		}
		return null;
	}
	public Long id;	
	public Class resourceClass;	
	private ExtensionType (Long id, Class resourceClass){
		this.id=id;
		this.resourceClass=resourceClass;
	}
}
