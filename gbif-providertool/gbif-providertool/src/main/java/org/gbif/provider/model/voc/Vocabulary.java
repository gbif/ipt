package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Vocabulary {
	Language("http://iso.org/639-1"),
	Country("http://iso.org/iso3166"),
	NomenclaturalStatus("http://rs.tdwg.org/ontology/voc/NomenclaturalStatus"),
	DarwinCoreTypes("http://rs.tdwg.org/dwc/dwctype/"),
	TaxonomicStatus("http://rs.tdwg.org/ontology/voc/TaxonomicStatus"),	
	ResourceType("http://rs.gbif.org/gbrds/resourceType");
	
	public String uri;
	
	private Vocabulary (String vocabularyUri){
		this.uri=vocabularyUri;
	}
}
