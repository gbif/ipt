package org.gbif.ipt.action.manage;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.IdentifierStatus;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MetadataActionTest {

  MetadataAction action;
  Map<String, String> datasetSubtypes;

  @Before
  public void setup() {

    // initiate action
    action = new MetadataAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(VocabulariesManager.class), mock(ConfigWarnings.class));

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

  @Test
  public void testHasDoiReservedOrAssigned() {
    Resource resource = new Resource();
    resource.setDoi(new DOI("doi:10.1594/KHU654"));

    resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);
    assertFalse(action.hasDoiReservedOrAssigned(resource));

    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    assertTrue(action.hasDoiReservedOrAssigned(resource));

    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertTrue(action.hasDoiReservedOrAssigned(resource));

    resource.setIdentifierStatus(IdentifierStatus.UNAVAILABLE);
    assertTrue(action.hasDoiReservedOrAssigned(resource));
  }

  @Test
  public void testLicensesProperties() {
    assertEquals(6, MetadataAction.licenseProperties().size());
  }

  @Test
  public void testLoadLicensesMaps() {
    MetadataAction.loadLicenseMaps("Select a license");
    assertEquals(4, action.getLicenses().size()); // includes "select a license"
    assertEquals("Select a license", action.getLicenses().get(""));
    assertEquals(3, action.getLicenseTexts().size());
  }

  @Test
  public void testDirectoriesProperties() {
    assertEquals(5, MetadataAction.directoriesProperties().size());
  }

  @Test
  public void testLoadDirectoriesMap() {
    MetadataAction.loadDirectories("Select a directory");
    assertEquals(6, action.getUserIdDirectories().size()); // includes "select a license"
    assertEquals("Select a directory", action.getUserIdDirectories().get(""));
  }

  @Test
  public void testRemoveNewlineCharacters() {
    String withNewline = "Copyright Creative Commons Attribution-Share Alike License © 2011. \n"
                         + "http://creativecommons.org/licenses/by-sa/3.0/";
    assertEquals("Copyright Creative Commons Attribution-Share Alike License © 2011.  http://creativecommons.org/licenses/by-sa/3.0/", action.removeNewlineCharacters(withNewline));

    withNewline = "Copyright Creative Commons Attribution-Share Alike License © 2011. \r\n"
                         + "http://creativecommons.org/licenses/by-sa/3.0/";
    assertEquals("Copyright Creative Commons Attribution-Share Alike License © 2011.  http://creativecommons.org/licenses/by-sa/3.0/", action.removeNewlineCharacters(withNewline));

    withNewline = "Copyright Creative Commons Attribution-Share Alike License © 2011. \r"
                  + "http://creativecommons.org/licenses/by-sa/3.0/";
    assertEquals("Copyright Creative Commons Attribution-Share Alike License © 2011.  http://creativecommons.org/licenses/by-sa/3.0/", action.removeNewlineCharacters(withNewline));
  }
}
