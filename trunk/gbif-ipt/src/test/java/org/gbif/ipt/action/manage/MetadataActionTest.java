package org.gbif.ipt.action.manage;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MetadataActionTest {

  MetadataAction action;
  Map<String, String> datasetSubtypes;

  @Before
  public void setup() {
    // initiate action
    action = new MetadataAction();

    // mock creation of datasetSubtypes Map, with 2 occurrence subtypes, and 6 checklist subtypes
    datasetSubtypes = new LinkedHashMap<String, String>();
    datasetSubtypes.put("", "Select a subtype");
    datasetSubtypes.put("taxonomicAuthority", "Taxonomic Authority");
    datasetSubtypes.put("nomenclatorAuthority", "Nomenclator Authority");
    datasetSubtypes.put("inventoryThematic", "Inventory Thematic");
    datasetSubtypes.put("inventoryRegional", "Inventory Regional");
    datasetSubtypes.put("globalSpeciesDataset", "Global Species Dataset");
    datasetSubtypes.put("derivedFromOccurrence", "Derived from Occurrence");
    datasetSubtypes.put("specimen", "Specimen");
    datasetSubtypes.put("observation", "Observation");
    // set Map
    action.setDatasetSubtypes(datasetSubtypes);
    // make all keys lower case in datasetSubtypes Map
    action.setDatasetSubtypes(action.getMapWithLowercaseKeys(datasetSubtypes));
  }

   @Test
  public void testGroupChecklistSubtypeKeysatasetSubtypes() {
     action.groupDatasetSubtypes();
     assertEquals(2, action.getOccurrenceSubtypeKeys().size());
     assertEquals(6, action.getChecklistSubtypeKeys().size());
   }

  @Test
  public void testGetOccurrenceSubtypesMap() {
    action.groupDatasetSubtypes();
    assertEquals(3, action.getOccurrenceSubtypesMap().size());
  }

  @Test
  public void testGetChecklistSubtypesMap() {
    action.groupDatasetSubtypes();
    assertEquals(7, action.getChecklistSubtypesMap().size());
  }
}
