package org.gbif.provider.model.voc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TransformationType {
    Union("Union of columns"),
    Hierarchy("Hierarchy normalisation"),
    Lookup("ID lookup"),
    Vocabulary("Vocabulary translation"),  
    Sql("SQL view");  

    public static final Map<Integer, String> htmlSelectMap;
    	static
    	{
    		Map<Integer, String> map = new HashMap<Integer, String>();
			for (TransformationType tt : TransformationType.values()){
				map.put(tt.ordinal(), tt.name());
			}
			htmlSelectMap = Collections.unmodifiableMap(map);  			
    	}
    public String verbatim;
    private TransformationType(String verbatim){
    	this.verbatim=verbatim;
    }
    
}
