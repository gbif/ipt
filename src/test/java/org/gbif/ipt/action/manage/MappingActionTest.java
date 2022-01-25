/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingActionTest {

  private MappingAction action;
  private static final String RESOURCE_SHORT_NAME = "myResource";

  @BeforeEach
  public void setup() {
    // Mock HttpServletRequest
    HttpServletRequest req = mock(HttpServletRequest.class);
    // to return Occurrence Rowtype for parameter "id"
    when(req.getParameter("id")).thenReturn(Constants.DWC_ROWTYPE_OCCURRENCE);
    // to return Resource shortname "myResource" for parameter Constants.REQ_PARAM_RESOURCE
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn("RESOURCE_SHORT_NAME");
    // to return "post"
    when(req.getMethod()).thenReturn("post");

    // set small list of source column names representing a source file to be mapped
    List<String> columns = new ArrayList<String>();
    columns.add("identificationID");
    columns.add("identificationQualifier");
    columns.add("unknown");
    columns.add("occurrenceID");

    // mappingCoreId = OccurrenceID
    PropertyMapping mappingCoreid = new PropertyMapping();
    mappingCoreid.setTerm(DwcTerm.occurrenceID);
    mappingCoreid.setIndex(3);

    // an ExtensionMapping to Extension Darwin Core Occurrence Core
    ExtensionMapping mapping = new ExtensionMapping();
    // create a new Extension, that represents the Darwin Core Occurrence Core
    Extension occurrenceCore = new Extension();
    occurrenceCore.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    List<ExtensionProperty> extensionProperties = new ArrayList<>();
    ExtensionProperty extensionProperty = new ExtensionProperty();
    extensionProperty.setQualname(DwcTerm.occurrenceID.qualifiedName());
    extensionProperties.add(extensionProperty);
    occurrenceCore.setProperties(extensionProperties);
    // add extension to mapping
    mapping.setExtension(occurrenceCore);

    // Resource
    Resource resource = new Resource();
    resource.setShortname(RESOURCE_SHORT_NAME);
    // set core type
    resource.setCoreType(StringUtils.capitalize(Resource.CoreRowType.OCCURRENCE.toString()));
    // set mappings
    resource.addMapping(mapping);

    // mock ExtensionManager to return Occurrence Core Type Extension
    ExtensionManager mockExtensionManager = mock(ExtensionManager.class);
    when(mockExtensionManager.get(anyString())).thenReturn(occurrenceCore);

    // mock ResourceManager to return Resource on get by shortname
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    when(mockResourceManager.get(anyString())).thenReturn(resource);

    // fields: small subset of 2 fields from identification history extension
    List<PropertyMapping> fields = new ArrayList<>();
    PropertyMapping identificationId = new PropertyMapping();
    identificationId.setTerm(DwcTerm.identificationID);
    identificationId.setIndex(0);
    fields.add(identificationId);
    PropertyMapping identificationQualifier = new PropertyMapping();
    identificationQualifier.setTerm(DwcTerm.identificationQualifier);
    identificationQualifier.setIndex(1);
    fields.add(identificationQualifier);

    // mock action
    action = new MappingAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mockResourceManager, mockExtensionManager, mock(SourceManager.class), mock(VocabulariesManager.class));

    action.setColumns(columns);
    action.setMappingCoreid(mappingCoreid);
    action.setMapping(mapping);
    action.setResource(resource); // overridden by resourceManager.get(shortname)
    action.setFields(fields);
    action.setServletRequest(req);
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

  @Disabled("the mapping and the resource's mapping are not the same object and so the remove(mapping) is always false")
  public void testDelete() {
    // prepare id, mid parameters
    action.prepare();
    // to begin with, show the resource's core type isn't null
    assertNotNull(action.getResource().getCoreType());
    // simulate delete core type mapping being deleted
    action.delete();
    // assert the resource's core type has been reset
    assertNull(action.getResource().getCoreType());
  }

  @Test
  public void testSave() throws IOException {
    // prepare id, mid parameters
    action.prepare();
    // to begin with, show the resource's core ROW type isn't null
    assertEquals(Constants.DWC_ROWTYPE_OCCURRENCE, action.getResource().getCoreRowType());
    // perform save
    action.save();
    // assert the resource's core type remains as Occurrence Core Type
    assertEquals(Resource.CoreRowType.OCCURRENCE.toString(), StringUtils.capitalize(action.getResource().getCoreType()));
  }

  @Test
  public void testGetNonMappedColumns() {
    List<String> nonMapped = action.getNonMappedColumns();
    assertEquals(1, nonMapped.size());
    assertEquals("unknown", nonMapped.get(0));
  }

  @Test
  public void testIsCoreMapping() {
    action.prepare();
    assertTrue(action.isCoreMapping());
  }
}
