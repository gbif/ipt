package org.gbif.provider.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Rank {
	Kingdom,
	Phylum,
	Class,
	Order,
	Family,
	Genus;	
	
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
	  
}
