package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum StatusType {
	Nomenclature("nomenclaturalStatus"),
	Taxonomic("taxonomicStatus");	
	
	public String columnName;	
	private StatusType (String colName){
		columnName=colName;
	}
	
	public static StatusType getByInt(int i){
		for (StatusType r : StatusType.values()){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
