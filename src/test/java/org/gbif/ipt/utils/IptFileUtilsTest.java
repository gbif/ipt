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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IptFileUtilsTest extends IptBaseTest {

  @Test
  void testGetSuffixedFileName() {
    assertEquals("http_rs_gbif_org_vocabulary_gbif_rank_xml.vocab", IptFileUtils
        .getSuffixedFileName("http://rs.gbif.org/vocabulary/gbif/rank.xml", VocabulariesManagerImpl.VOCAB_FILE_SUFFIX));
    assertEquals("http_rs_gbif_org_extension_gbif_1_0_multimedia_xml.xml", IptFileUtils
        .getSuffixedFileName("http://rs.gbif.org/extension/gbif/1.0/multimedia.xml",
            ExtensionManagerImpl.EXTENSION_FILE_SUFFIX));
  }

  @Test
  void testWithoutByteOrderMark() throws IOException {
    for (Charset charset : new Charset[] {StandardCharsets.UTF_8, StandardCharsets.UTF_16LE,
      StandardCharsets.UTF_16BE, Charset.forName("UTF-32LE"), Charset.forName("UTF-32BE")}) {
      byte[] content = "\uFEFFid\uFEFF".getBytes(charset);
      // only the mark at the start
      assertEquals("id\uFEFF", new String(read(content, charset.name()), charset), charset.name());
      assertEquals("id", new String(read("id".getBytes(charset), charset.name()), charset), charset.name());
    }

    byte[] utf8 = "\uFEFFid".getBytes(StandardCharsets.UTF_8);
    // the mark of another encoding is data
    assertArrayEquals(utf8, read(utf8, "UTF-16LE"));
    // in an encoding without one, its bytes are characters
    assertArrayEquals(utf8, read(utf8, "ISO-8859-1"));
    // the UTF-16 decoder removes it itself
    byte[] utf16 = "id".getBytes(StandardCharsets.UTF_16);
    assertArrayEquals(utf16, read(utf16, "UTF-16"));
    // reading the stream reports an encoding that isn't known
    assertArrayEquals(utf8, read(utf8, "no-such-encoding"));
    assertArrayEquals(utf8, read(utf8, null));
  }

  private static byte[] read(byte[] content, String encoding) throws IOException {
    try (InputStream in = IptFileUtils.withoutByteOrderMark(new ByteArrayInputStream(content), encoding)) {
      return IOUtils.toByteArray(in);
    }
  }

  @Test
  void testLocaleUtilsLocaleParse() {
    assertEquals(Locale.UK, LocaleUtils.toLocale("en_GB"));
    assertEquals(Locale.FRENCH, LocaleUtils.toLocale("fr"));
    assertEquals(new Locale("es"), LocaleUtils.toLocale("es"));
    assertEquals(Locale.CHINESE, LocaleUtils.toLocale("zh"));
    assertEquals(new Locale("ru"), LocaleUtils.toLocale("ru"));
    assertEquals(new Locale("pt"), LocaleUtils.toLocale("pt"));
    assertEquals(Locale.JAPANESE, LocaleUtils.toLocale("ja"));
  }

  @Test
  void testDeleteDirectoryContainingSingleFile() throws Exception {
    // mock resource directory with single file
    File resourceDir = org.gbif.utils.file.FileUtils.createTempDir();
    assertTrue(resourceDir.isDirectory());
    File emlFile = new File(resourceDir, "eml.xml");
    assertTrue(emlFile.createNewFile());
    IptFileUtils.deleteDirectoryContainingSingleFile(emlFile);
    // ensure method deleted resource directory and its file
    assertFalse(resourceDir.exists());
    assertFalse(emlFile.exists());

    // mock another resource directory with two files
    resourceDir = org.gbif.utils.file.FileUtils.createTempDir();
    assertTrue(resourceDir.isDirectory());
    emlFile = new File(resourceDir, "eml.xml");
    assertTrue(emlFile.createNewFile());
    File metaFile = new File(resourceDir, "meta.xml");
    assertTrue(metaFile.createNewFile());
    IptFileUtils.deleteDirectoryContainingSingleFile(emlFile);
    // ensure method didn't delete resource directory and its files
    assertTrue(resourceDir.exists());
    assertTrue(emlFile.exists());
    assertTrue(metaFile.exists());
  }

}
