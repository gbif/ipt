package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MetadataActionTest {

  MetadataAction action;
  Map<String, String> datasetSubtypes;

  @Before
  public void setup() {

    // initiate action
    action = new MetadataAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(VocabulariesManager.class));

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
    action.setDatasetSubtypes(MapUtils.getMapWithLowercaseKeys(datasetSubtypes));
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
