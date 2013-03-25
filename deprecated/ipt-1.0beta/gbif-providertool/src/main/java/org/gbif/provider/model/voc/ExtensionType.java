package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;

public enum ExtensionType {
	Occurrence(1l, OccurrenceResource.class, "Darwin_Core", "occ", 1l),
	Checklist(7l, ChecklistResource.class, "Taxon", "tax", 7l),	
	Metadata(null, Resource.class, "Resource", "meta", null);	

	public static ExtensionType byResourceClass(Class resourceClass){
		for (ExtensionType et : ExtensionType.values()){
			if (et.resourceClass.equals(resourceClass)){
				return et;
			}
		}
		return null;
	}
	
    public static final Map<String, String> htmlSelectMap;
	static
	{
		Map<String, String> map = new HashMap<String, String>();
		for (ExtensionType et : ExtensionType.values()){
			map.put(et.alias, "resourceType."+et.alias);
		}
		htmlSelectMap = Collections.unmodifiableMap(map);  			
	}
	
	public Long id;	
	public Class resourceClass;	
	public String tableName;	
	public String alias;	
	public Long extensionID;
	
	private ExtensionType (Long id, Class resourceClass, String tableName, String alias, Long extensionID){
		this.id=id;
		this.resourceClass=resourceClass;
		this.tableName=tableName;
		this.alias=alias;
		this.extensionID=extensionID;
	}
}
