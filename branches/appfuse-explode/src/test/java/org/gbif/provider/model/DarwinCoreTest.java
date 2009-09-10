/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model;

import org.gbif.provider.util.ResourceTestBase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class DarwinCoreTest extends ResourceTestBase {

  @Test
  public void testDarwinCore() {
    OccurrenceResource r = getResourceMock();

    DarwinCore dwc = DarwinCore.newInstance(r);
    dwc.setCatalogNumber("befhjsa6788-x");
    dwc.setScientificName("Abies alba");
    dwc.setBasisOfRecord("specimen");
    // System.out.println(dwc);
    assertTrue(dwc.hashCode() > 0);
    assertTrue(dwc.toString().length() > 0);
    assertTrue(dwc.equals(dwc));
    assertFalse(dwc.equals(null));

    DarwinCore dwc2 = DarwinCore.newInstance(r);
    dwc2.setCatalogNumber("befhjsa6788-x");
    dwc2.setScientificName("Abies alba");
    dwc2.setBasisOfRecord("specimen");
    assertTrue(dwc.equals(dwc2));
    assertTrue(dwc.hashCode() == dwc2.hashCode());
    // System.out.println(dwc2);

    dwc2.setInstitutionCode("RBGK");
    assertFalse(dwc.hashCode() == dwc2.hashCode());
    assertFalse(dwc.equals(dwc2));
  }

}
