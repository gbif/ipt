package org.gbif.provider.model;


import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.upload.TaxonomyBuilder;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonTest {
	private OccurrenceResource resource;

	private Taxon newTaxon(String name, Taxon parent){
		Taxon t = new Taxon();
		t.setResource(resource);
		t.setName(name);
		t.setFullname(name);
		t.setParent(parent);
		return t;
	}
	private Taxon newTaxon(String name){
		return newTaxon(name, null);
	}
	
	@Test
	public void testTaxonComparison(){
		resource = new OccurrenceResource();
		List<Taxon> taxa = new java.util.ArrayList<Taxon>();
		Taxon plants = newTaxon("Plantae");
		Taxon pinales = newTaxon("Pinales",plants);
		Taxon asterales= newTaxon("Asterales",plants);
		Taxon asteraceae = newTaxon("Asteraceae",asterales);
		Taxon pinaceae = newTaxon("Pinaaceae",pinales);
		Taxon abies = newTaxon("Abies",pinaceae);
		Taxon pinus = newTaxon("Pinus",pinaceae);
		Taxon aster = newTaxon("Aster",asteraceae);
		Taxon crepis= newTaxon("Crepis",asteraceae);
		Taxon crepis_vulg= newTaxon("Crepis vulgaris L.",crepis);
		Taxon crepis_com= newTaxon("Crepis communis L.",crepis);
		taxa.add(crepis_com);
		taxa.add(pinus);
		taxa.add(pinaceae);
		taxa.add(crepis_vulg);
		taxa.add(asterales);
		taxa.add(pinales);
		taxa.add(plants);
		taxa.add(crepis);
		taxa.add(abies);
		taxa.add(asteraceae);
		taxa.add(aster);
		
//		System.out.println(crepis_com.getParents());
		assertTrue(plants.getParents().isEmpty());
		
		// try sorting
//		System.out.println("# sort all");
//		System.out.println(taxa);
		Collections.sort(taxa);
//		System.out.println(taxa);
		assertTrue(taxa.get(0).equals(plants));
		assertTrue(taxa.get(taxa.size()-1).equals(pinus));
		assertTrue(taxa.get(2).equals(asteraceae));
		
		
//		System.out.println("# sort genera only");
		List<Taxon> taxa2 = new java.util.ArrayList<Taxon>();		
		taxa2.add(crepis);
		taxa2.add(abies);
		taxa2.add(pinus);
		taxa2.add(aster);
//		System.out.println(taxa2);
		Collections.sort(taxa2);
//		System.out.println(taxa2);
		assertTrue(taxa2.get(0).equals(aster));
		assertTrue(taxa2.get(2).equals(abies));
		assertTrue(taxa2.get(3).equals(pinus));

//		System.out.println("# sort upper 4");
		List<Taxon> taxa3 = new java.util.ArrayList<Taxon>();
		taxa3.add(pinales);
		taxa3.add(plants);
		taxa3.add(asterales);
		taxa3.add(asteraceae);
		taxa3.add(pinaceae);
//		System.out.println(taxa3);
		Collections.sort(taxa3);
//		System.out.println(taxa3);
		assertTrue(taxa3.get(0).equals(plants));
		assertTrue(taxa3.get(2).equals(asteraceae));
		assertTrue(taxa3.get(4).equals(pinaceae));
	}

}
