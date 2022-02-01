/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
