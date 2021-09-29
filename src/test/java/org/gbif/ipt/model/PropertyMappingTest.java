package org.gbif.ipt.model;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.TermFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyMappingTest {

  @Test
  public void testEquals() {

    PropertyMapping pm = new PropertyMapping();
    pm.setTerm(DwcTerm.occurrenceID);

    assertEquals(0, DwcTerm.valueOf(pm.getTerm().simpleName()).normAlts.length);

    String identificationId = "identificationId";
    assertFalse(TermFactory.normaliseTerm(pm.getTerm().simpleName()).equalsIgnoreCase(identificationId));

    String occurrenceId = "occurrenceId";
    assertTrue(TermFactory.normaliseTerm(pm.getTerm().simpleName()).equalsIgnoreCase(occurrenceId));
  }
}
