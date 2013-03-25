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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonTest {
  private OccurrenceResource resource;

  @Test
  public void testTaxonComparison() {
    resource = new OccurrenceResource();
    List<Taxon> taxa = new java.util.ArrayList<Taxon>();
    Taxon plants = newTaxon("Plantae");
    Taxon pinales = newTaxon("Pinales", plants);
    Taxon asterales = newTaxon("Asterales", plants);
    Taxon asteraceae = newTaxon("Asteraceae", asterales);
    Taxon pinaceae = newTaxon("Pinaaceae", pinales);
    Taxon abies = newTaxon("Abies", pinaceae);
    Taxon pinus = newTaxon("Pinus", pinaceae);
    Taxon aster = newTaxon("Aster", asteraceae);
    Taxon crepis = newTaxon("Crepis", asteraceae);
    Taxon crepisVulg = newTaxon("Crepis vulgaris L.", crepis);
    Taxon crepisCom = newTaxon("Crepis communis L.", crepis);
    taxa.add(crepisCom);
    taxa.add(pinus);
    taxa.add(pinaceae);
    taxa.add(crepisVulg);
    taxa.add(asterales);
    taxa.add(pinales);
    taxa.add(plants);
    taxa.add(crepis);
    taxa.add(abies);
    taxa.add(asteraceae);
    taxa.add(aster);

    // System.out.println(crepis_com.getParents());
    assertTrue(plants.getParents().isEmpty());

    // try sorting
    // System.out.println("# sort all");
    // System.out.println(taxa);
    Collections.sort(taxa);
    // System.out.println(taxa);
    assertTrue(taxa.get(0).equals(plants));
    assertTrue(taxa.get(taxa.size() - 1).equals(pinus));
    assertTrue(taxa.get(2).equals(asteraceae));

    // System.out.println("# sort genera only");
    List<Taxon> taxa2 = new java.util.ArrayList<Taxon>();
    taxa2.add(crepis);
    taxa2.add(abies);
    taxa2.add(pinus);
    taxa2.add(aster);
    // System.out.println(taxa2);
    Collections.sort(taxa2);
    // System.out.println(taxa2);
    assertTrue(taxa2.get(0).equals(aster));
    assertTrue(taxa2.get(2).equals(abies));
    assertTrue(taxa2.get(3).equals(pinus));

    // System.out.println("# sort upper 4");
    List<Taxon> taxa3 = new java.util.ArrayList<Taxon>();
    taxa3.add(pinales);
    taxa3.add(plants);
    taxa3.add(asterales);
    taxa3.add(asteraceae);
    taxa3.add(pinaceae);
    // System.out.println(taxa3);
    Collections.sort(taxa3);
    // System.out.println(taxa3);
    assertTrue(taxa3.get(0).equals(plants));
    assertTrue(taxa3.get(2).equals(asteraceae));
    assertTrue(taxa3.get(4).equals(pinaceae));
  }

  private Taxon newTaxon(String name) {
    return newTaxon(name, null);
  }

  private Taxon newTaxon(String name, Taxon parent) {
    Taxon t = Taxon.newInstance(resource);
    t.setScientificName(name);
    t.setParent(parent);
    return t;
  }

}
