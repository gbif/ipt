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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceTypeService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceTypeServiceImplTest {

  private static final String DATASET_SUBTYPE_SPECIMEN_IDENTIFIER = "specimen";

  private final VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);

  @Test
  public void testInferCoreType() {
    ResourceTypeService service = getResourceTypeServiceImpl();
    // create test resource
    Resource resource = new Resource();
    // add mapping to taxon core
    ExtensionMapping mapping = new ExtensionMapping();
    Extension ext = new Extension();
    ext.setRowType(Constants.DWC_ROWTYPE_TAXON);
    mapping.setExtension(ext);
    resource.addMapping(mapping);

    resource = service.inferCoreType(resource);
    // assert the coreType has now been correctly inferred
    assertNotNull(resource.getCoreType());
    assertEquals(Resource.CoreRowType.CHECKLIST.toString().toLowerCase(), resource.getCoreType().toLowerCase());
  }

  @Test
  public void testInferSubtype() {
    ResourceTypeService service = getResourceTypeServiceImpl();
    // create test resource
    Resource resource = new Resource();
    resource.setSubtype("unknown");
    resource = service.standardizeSubtype(resource);
    // assert the subtype has been set to null, since it doesn't correspond to a known vocab term
    assertNull(resource.getSubtype());

    resource.setSubtype(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER);
    resource = service.standardizeSubtype(resource);
    // assert the subtype has been set to "specimen", since it does correspond to the known vocab term "specimen"
    assertEquals(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER, resource.getSubtype());
  }

  public ResourceTypeServiceImpl getResourceTypeServiceImpl() {
    // mock creation of datasetSubtypes Map, with 2 occurrence subtypes, and 6 checklist subtypes
    Map<String, String> datasetSubtypes = new LinkedHashMap<>();
    datasetSubtypes.put("", "Select a subtype");
    datasetSubtypes.put("taxonomicAuthority", "Taxonomic Authority");
    datasetSubtypes.put("nomenclatorAuthority", "Nomenclator Authority");
    datasetSubtypes.put("inventoryThematic", "Inventory Thematic");
    datasetSubtypes.put("inventoryRegional", "Inventory Regional");
    datasetSubtypes.put("globalSpeciesDataset", "Global Species Dataset");
    datasetSubtypes.put("derivedFromOccurrence", "Derived from Occurrence");
    datasetSubtypes.put(DATASET_SUBTYPE_SPECIMEN_IDENTIFIER, "Specimen");
    datasetSubtypes.put("observation", "Observation");
    // mock getting the vocabulary
    when(mockVocabulariesManager.getI18nVocab(anyString(), anyString(), anyBoolean())).thenReturn(datasetSubtypes);

    return new ResourceTypeServiceImpl(mockVocabulariesManager);
  }
}
