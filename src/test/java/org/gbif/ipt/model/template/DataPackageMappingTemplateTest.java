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
package org.gbif.ipt.model.template;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class DataPackageMappingTemplateTest {

  @Test
  void testSerDeser() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    File templateFile = FileUtils.getClasspathFile("schemas/templates/template.json");

    DataPackageMappingTemplate firstDeser = mapper.readValue(templateFile, DataPackageMappingTemplate.class);

    verifyTemplateObject(firstDeser);

    String firstSer = mapper.writeValueAsString(firstDeser);

    DataPackageMappingTemplate secondDeser = mapper.readValue(firstSer, DataPackageMappingTemplate.class);

    verifyTemplateObject(secondDeser);
  }

  private void verifyTemplateObject(DataPackageMappingTemplate template) {
    assertEquals("first-test-template-dwc-dp", template.getName());
    assertEquals("Test template", template.getDescription());
    assertEquals(1, template.getKeywords().size());
    assertEquals(Set.of("occurrence"), template.getKeywords());

    assertEquals("http://rs.tdwg.org/dwc/dwc-dp", template.getSchema().getIdentifier());
    assertEquals("dwc-dp", template.getSchema().getName());
    assertEquals("0.1", template.getSchema().getVersion());
    // TODO: verify issuedDate
  }
}