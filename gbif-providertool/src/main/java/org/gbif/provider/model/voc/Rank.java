package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Rank {
	Kingdom("kingdom"),
	Phylum("phylum"),
	Class("classs"),
	Order("order"),
	Family("family"),
	Genus("genus");	
	
	public static final List<Rank> DARWIN_CORE_RANKS;
	  static  
	  {  
	    List<Rank> dwcRanks = new ArrayList<Rank>();  
	    dwcRanks.add( Kingdom );  
	    dwcRanks.add( Phylum );  
	    dwcRanks.add( Class );  
	    dwcRanks.add( Order );  
	    dwcRanks.add( Family );  
	    dwcRanks.add( Genus );  
	    DARWIN_CORE_RANKS = Collections.unmodifiableList(dwcRanks);  
	  }  

	  
	public String columnName;	
	private Rank (String colName){
		columnName=colName;
	}

	public static Rank getByInt(int i){
		for (Rank r : DARWIN_CORE_RANKS){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}
	  
}
