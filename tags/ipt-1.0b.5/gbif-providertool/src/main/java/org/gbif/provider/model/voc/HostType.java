package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum HostType {
	Institution("institutionCode"),
	Collection("collectionCode");	
	
	public String columnName;	
	private HostType (String colName){
		columnName=colName;
	}
	
	public static HostType getByInt(int i){
		for (HostType r : HostType.values()){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
