package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum StatusType {
	Nomenclature("nomenclaturalStatus"),
	Taxonomic("taxonomicStatus");	
	
	public static final List<StatusType> ALL_STATUSES;
		  static  
		  {  
		    List<StatusType> tcs = new ArrayList<StatusType>();  
		    tcs.add( Nomenclature );  
		    tcs.add( Taxonomic );  
		    ALL_STATUSES = Collections.unmodifiableList(tcs);  
		  };
	  
	public String columnName;	
	private StatusType (String colName){
		columnName=colName;
	}
	
	public static StatusType getByInt(int i){
		for (StatusType r : ALL_STATUSES){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
