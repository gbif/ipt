package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Vocabulary{
	Rank("http://rs.tdwg.org/ontology/voc/TaxonRank"),
	TaxonomicStatus("http://rs.tdwg.org/ontology/voc/TaxonomicStatus"),
	NomenclaturalStatus("http://rs.tdwg.org/ontology/voc/NomenclaturalStatus"),
	IsoCountry("http://iso.org/3166"),
	Language("http://iso.org/639-1");	
	
	public String uri;	
	private Vocabulary (String uri){
		uri=uri;
	}
	
	public String getUri(){
		return uri;
	}
	public static Vocabulary getByInt(int i){
		
		for (Vocabulary r : Vocabulary.values()){
			if (r.ordinal() == i){
				return r;
			}
		}
		return null;
	}

}
