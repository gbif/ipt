package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum RegionType {
	Continent("continent"),
	Waterbody("waterBody"),
	Island("island"),
	Country("country"),
	State("stateProvince"),
	County("county");	
	
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
	  
	  
	public String columnName;	
	private RegionType (String colName){
		columnName=colName;
	}
}
