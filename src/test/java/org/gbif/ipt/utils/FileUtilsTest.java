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
package org.gbif.ipt.utils;

import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileUtilsTest {

  @Test
  public void testGetSuffixedFileName() {
    assertEquals("http_rs_gbif_org_vocabulary_gbif_rank_xml.vocab", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/vocabulary/gbif/rank.xml", VocabulariesManagerImpl.VOCAB_FILE_SUFFIX));
    assertEquals("http_rs_gbif_org_extension_gbif_1_0_multimedia_xml.xml", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/extension/gbif/1.0/multimedia.xml",
        ExtensionManagerImpl.EXTENSION_FILE_SUFFIX));
  }
}
