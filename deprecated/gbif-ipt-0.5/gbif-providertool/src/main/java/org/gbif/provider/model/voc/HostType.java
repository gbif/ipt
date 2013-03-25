package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum HostType {
	Institution("institutionCode"),
	Collection("collectionCode");	
	
	public static final List<HostType> HOSTING_BODIES;
	  static  
	  {  
	    List<HostType> hosts = new ArrayList<HostType>();  
	    hosts.add( Institution );  
	    hosts.add( Collection );  
	    HOSTING_BODIES = Collections.unmodifiableList(hosts);  
	  };
	  
	public String columnName;	
	private HostType (String colName){
		columnName=colName;
	}
	
	public static HostType getByInt(int i){
		for (HostType r : HOSTING_BODIES){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
