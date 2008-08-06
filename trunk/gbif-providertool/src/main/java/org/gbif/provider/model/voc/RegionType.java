package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum RegionType {
	Continent(1,"continent"),
	Waterbody(2,"waterBody"),
	Island(3,"island"),
	Country(4,"country"),
	State(5,"stateProvince"),
	County(6,"county");	
	
	public static final List<RegionType> DARWIN_CORE_REGIONS;
	  static  
	  {  
	    List<RegionType> dwc = new ArrayList<RegionType>();  
	    dwc.add( Continent );  
	    dwc.add( Waterbody );  
	    dwc.add( Island );  
	    dwc.add( Country );  
	    dwc.add( State );  
	    DARWIN_CORE_REGIONS = Collections.unmodifiableList(dwc);  
	  };
	  
	  
	public int index;	
	public String columnName;	
	private RegionType (int idx, String colName){
		index=idx;
		columnName=colName;
	}
	
	public static RegionType getByInt(int i){
		for (RegionType r : DARWIN_CORE_REGIONS){
			if (r.index == i){
				return r;
			}
		}
		return null;
	}

}
