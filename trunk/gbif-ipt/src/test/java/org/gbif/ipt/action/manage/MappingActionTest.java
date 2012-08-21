package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MappingActionTest {

  private MappingAction action;

  @Before
  public void setup() {
    // mock action
    action = new MappingAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(ExtensionManager.class), mock(SourceManager.class),
      mock(VocabulariesManager.class));

    // set small list of source column names representing a source file to be mapped
    List<String> columns = new ArrayList<String>();
    columns.add("identificationID");
    columns.add("identificationQualifier");
    columns.add("unknown");
    columns.add("occurrenceID");
    action.setColumns(columns);

    // set mappingCoreId = OccurrenceID
    PropertyMapping mappingCoreid = new PropertyMapping();
    mappingCoreid.setTerm(DwcTerm.occurrenceID);
    action.setMappingCoreid(mappingCoreid);

    // set an ExtensionMapping
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(new Extension());
    action.setMapping(mapping);

    // set fields: small subset of 2 fields from identification history extension
    List<PropertyMapping> fields = new ArrayList<PropertyMapping>();
    PropertyMapping identificationId = new PropertyMapping();
    identificationId.setTerm(DwcTerm.identificationID);
    fields.add(identificationId);
    PropertyMapping identificationQualifier = new PropertyMapping();
    identificationQualifier.setTerm(DwcTerm.identificationQualifier);
    fields.add(identificationQualifier);
    action.setFields(fields);
  }

  @Test
  public void testAutoMap() {
    // we expect 3 terms to automap: occurrenceID, identificationId, and identificationQualifier (not term unknown)
    assertEquals(3, action.automap());
  }

  @Test
  public void testNormalizeColumnName() {
    String col1 = null;
    assertEquals(null, action.normalizeColumnName(col1));

    String col2 = "KingDOM";
    assertEquals("kingdom", action.normalizeColumnName(col2));

    String col3 = "scientificName:1";
    assertEquals("scientificname", action.normalizeColumnName(col3));
  }

}
