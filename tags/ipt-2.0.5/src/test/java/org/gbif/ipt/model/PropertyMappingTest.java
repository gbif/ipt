package org.gbif.ipt.model;

import org.gbif.dwc.terms.DwcTerm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PropertyMappingTest {

  @Test
  public void testEquals() {

    PropertyMapping pm = new PropertyMapping();
    pm.setTerm(DwcTerm.occurrenceID);

    assertEquals(0, pm.getTerm().simpleNormalisedAlternativeNames().length);

    String identificationId = "identificationId";
    assertFalse(pm.getTerm().simpleNormalisedName().equalsIgnoreCase(identificationId));

    String occurrenceId = "occurrenceId";
    assertTrue(pm.getTerm().simpleNormalisedName().equalsIgnoreCase(occurrenceId));
  }
}
