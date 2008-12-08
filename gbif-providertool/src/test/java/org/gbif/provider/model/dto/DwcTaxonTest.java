package org.gbif.provider.model.dto;

import static org.junit.Assert.*;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.Rank;
import org.junit.Before;
import org.junit.Test;

public class DwcTaxonTest {
	private DwcTaxon getNewTaxon(){
		OccurrenceResource r = OccurrenceResource.newInstance();
		DwcTaxon dt = DwcTaxon.newInstance(r);
		dt.setScientificName("Bellis perennis L.");
		dt.setKingdom("Plantae");
		dt.setFamily("Asteraceae");
		dt.setGenus("Bellis");
		dt.setSpeciesEpi("perennis");
		return dt;
	}

	@Test
	public void testEqualsObject() {
		DwcTaxon dt = getNewTaxon();
		DwcTaxon dt2 = getNewTaxon();
		
		assertEquals(dt, dt2);
		dt2.setClasss("Magnolieae");
		assertFalse(dt.equals(dt2));
	}

	@Test
	public void testNewDwcTaxonByRank() {
		DwcTaxon dt = getNewTaxon();
		DwcTaxon family = DwcTaxon.newDwcTaxon(dt, Rank.Family);
		
		assertFalse(dt.equals(family));
		assertTrue(dt.getFamily().equals(family.getScientificName()));
	}

}
