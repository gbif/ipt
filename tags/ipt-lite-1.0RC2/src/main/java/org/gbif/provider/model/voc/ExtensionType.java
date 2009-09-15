package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;

public enum ExtensionType {
	Occurrence(1l, OccurrenceResource.class, DarwinCore.class, "Darwin_Core", "occ"),
	Checklist(7l, ChecklistResource.class, Taxon.class, "Taxon", "tax"),	
	Metadata(null, Resource.class, Resource.class, "Resource", "meta");	

	public static ExtensionType byResourceClass(Class resourceClass){
		for (ExtensionType et : ExtensionType.values()){
			if (et.resourceClass.isAssignableFrom(resourceClass)){
				return et;
			}
		}
		return null;
	}
	public static ExtensionType byCoreClass(Class coreClass){
		for (ExtensionType et : ExtensionType.values()){
			if (et.coreClass.isAssignableFrom(coreClass)){
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
	public Class coreClass;	
	public String tableName;	
	public String alias;	
	
	private ExtensionType (Long id, Class resourceClass, Class coreClass, String tableName, String alias){
		this.id=id;
		this.resourceClass=resourceClass;
		this.coreClass=coreClass;
		this.tableName=tableName;
		this.alias=alias;
	}
}
