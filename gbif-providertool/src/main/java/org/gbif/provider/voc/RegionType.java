package org.gbif.provider.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum RegionType {
	Continent,
	Waterbody,
	Island,
	Country,
	State,
	County;	
	
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
	  }  
	  
}
