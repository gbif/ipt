package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Rank {
	Kingdom(1, "kingdom"),
	Phylum(2, "phylum"),
	Class(3, "classs"),
	Order(4, "order"),
	Family(5, "family"),
	Genus(6 ,"genus");	
	
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

	  
	public int index;	
	public String columnName;	
	private Rank (int idx, String colName){
		columnName=colName;
		index=idx;
	}

	public static Rank getByInt(int i){
		for (Rank r : DARWIN_CORE_RANKS){
			if (r.index == i){
				return r;
			}
		}
		return null;
	}
	  
}
