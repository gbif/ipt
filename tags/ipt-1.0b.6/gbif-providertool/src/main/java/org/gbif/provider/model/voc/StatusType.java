package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum StatusType {
	NomenclaturalStatus("nomenclaturalStatus", "http://rs.tdwg.org/ontology/voc/NomenclaturalStatus"),
	TaxonomicStatus("taxonomicStatus", "http://rs.tdwg.org/ontology/voc/TaxonomicStatus");	
	
	public String columnName;
	public String vocabularyUri;
	
	private StatusType (String colName, String vocabularyUri){
		this.columnName=colName;
		this.vocabularyUri=vocabularyUri;
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
