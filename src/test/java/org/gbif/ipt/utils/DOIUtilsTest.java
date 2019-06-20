package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DOIUtilsTest {

  @Test
  public void testMintDoiDataCite() {
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, "10.1234");
    assertNotNull(doi);
    assertTrue(doi.getDoiName().startsWith("10.1234"));
  }

  // TODO: 2019-06-20 remove
  @Test
  public void testMintDoiEZID() {
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.EZID, "10.1234/FK2");
    assertNotNull(doi);
    assertTrue(doi.getDoiName().startsWith("10.1234/fk2")); // converted to lower case by DOI library
  }
}
