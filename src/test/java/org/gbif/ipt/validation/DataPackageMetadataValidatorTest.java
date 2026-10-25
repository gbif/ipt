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
package org.gbif.ipt.validation;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.manage.impl.JsonServiceImpl;
import org.gbif.ipt.service.manage.impl.MetadataReaderImpl;
import org.gbif.ipt.service.manage.impl.YamlServiceImpl;
import org.gbif.utils.file.FileUtils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPackageMetadataValidatorTest {

  private final DataPackageMetadataValidator validator = new DataPackageMetadataValidator();

  /**
   * A ColDP without metadata (e.g. just created) can't be published.
   */
  @Test
  public void testBlankColDpMetadataIsInvalid() {
    Resource resource = new Resource();
    resource.setDataPackageMetadata(new ColMetadata());

    assertFalse(validator.isValid(resource));
  }

  @Test
  public void testColDpMetadataIsValid() throws Exception {
    Resource resource = new Resource();
    resource.setDataPackageMetadata(new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl())
        .readValue(FileUtils.getClasspathFile("frictionless/coldp/plazi/metadata.json"), ColMetadata.class));

    assertTrue(validator.isValid(resource));
  }
}
