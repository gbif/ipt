package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ExtensionType {
	Occurrence(1l),
	Checklist(4l);	
	
	public static final List<ExtensionType> CORE_ENTITIES;
	  static  
	  {  
	    List<ExtensionType> hosts = new ArrayList<ExtensionType>();  
	    hosts.add( Occurrence );  
	    hosts.add( Checklist );  
	    CORE_ENTITIES = Collections.unmodifiableList(hosts);  
	  };
	  
	public Long id;	
	private ExtensionType (Long id){
		this.id=id;
	}
}
