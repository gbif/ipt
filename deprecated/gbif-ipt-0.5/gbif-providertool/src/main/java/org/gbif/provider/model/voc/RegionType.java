package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum RegionType {
	Continent("continent"),
	Waterbody("waterBody"),
	IslandGroup("islandGroup"),
	Island("island"),
	Country("country"),
	State("stateProvince"),
	County("county"),
	Locality("locality");	
	
	public static final List<RegionType> DARWIN_CORE_REGIONS;
	  static  
	  {  
	    List<RegionType> dwc = new ArrayList<RegionType>();  
	    dwc.add( Continent );  
	    dwc.add( Waterbody );  
	    dwc.add( IslandGroup );  
	    dwc.add( Island );  
	    dwc.add( Country );  
	    dwc.add( State );  
	    dwc.add( County );  
	    DARWIN_CORE_REGIONS = Collections.unmodifiableList(dwc);  
	  };

		public static final List<RegionType> ALL_REGIONS;
		  static  
		  {  
		    List<RegionType> dwc = new ArrayList<RegionType>(DARWIN_CORE_REGIONS);  
		    dwc.add( Locality );  
		    ALL_REGIONS = Collections.unmodifiableList(dwc);  
		  };
	  
	public String columnName;	
	private RegionType (String colName){
		columnName=colName;
	}
	
	public static RegionType getByInt(int i){
		for (RegionType r : ALL_REGIONS){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
