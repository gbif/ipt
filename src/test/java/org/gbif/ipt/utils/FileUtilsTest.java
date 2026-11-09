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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class FileUtilsTest extends IptBaseTest {

  @Test
  public void testGetSuffixedFileName() {
    assertEquals("http_rs_gbif_org_vocabulary_gbif_rank_xml.vocab", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/vocabulary/gbif/rank.xml", VocabulariesManagerImpl.VOCAB_FILE_SUFFIX));
    assertEquals("http_rs_gbif_org_extension_gbif_1_0_multimedia_xml.xml", FileUtils
      .getSuffixedFileName("http://rs.gbif.org/extension/gbif/1.0/multimedia.xml",
        ExtensionManagerImpl.EXTENSION_FILE_SUFFIX));
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
  public void testTempSiblingMovedIntoPlace(@TempDir Path dir) throws Exception {
    // the target's directory doesn't exist yet
    File target = dir.resolve("res").resolve("datapackage.json").toFile();

    File tmp = FileUtils.createTempSibling(target);
    assertEquals(target.getAbsoluteFile().getParentFile(), tmp.getParentFile());
    Files.writeString(tmp.toPath(), "first", StandardCharsets.UTF_8);
    FileUtils.moveIntoPlace(tmp, target);

    tmp = FileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);
    FileUtils.moveIntoPlace(tmp, target);

    assertEquals("second", Files.readString(target.toPath(), StandardCharsets.UTF_8));
    assertArrayEquals(new String[] {"datapackage.json"}, Objects.requireNonNull(target.getParentFile().list()));
  }

  /**
   * Files.createTempFile would make eml.xml readable by its owner only.
   */
  @Test
  public void testTempSiblingHasDefaultPermissions(@TempDir Path dir) throws Exception {
    assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    Set<PosixFilePermission> defaults = Files.getPosixFilePermissions(Files.createFile(dir.resolve("plain.txt")));

    File target = dir.resolve("eml.xml").toFile();
    File tmp = FileUtils.createTempSibling(target);
    assertEquals(defaults, Files.getPosixFilePermissions(tmp.toPath()));
    FileUtils.moveIntoPlace(tmp, target);
    assertEquals(defaults, Files.getPosixFilePermissions(target.toPath()));
  }

  @Test
  public void testMoveIntoPlaceKeepsTargetPermissions(@TempDir Path dir) throws Exception {
    assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    File target = dir.resolve("eml.xml").toFile();
    Files.writeString(target.toPath(), "first", StandardCharsets.UTF_8);
    Set<PosixFilePermission> custom = PosixFilePermissions.fromString("rw-rw----");
    Files.setPosixFilePermissions(target.toPath(), custom);

    File tmp = FileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);
    FileUtils.moveIntoPlace(tmp, target);

    assertEquals("second", Files.readString(target.toPath(), StandardCharsets.UTF_8));
    assertEquals(custom, Files.getPosixFilePermissions(target.toPath()));
  }

  /**
   * When the target can't be replaced (on Windows: a reader has it open; here: the directory is read-only), its
   * content is overwritten instead.
   */
  @Test
  public void testMoveIntoPlaceFallsBackToOverwriting(@TempDir Path dir) throws Exception {
    assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    Path resDir = Files.createDirectory(dir.resolve("res"));
    File target = resDir.resolve("eml.xml").toFile();
    Files.writeString(target.toPath(), "first", StandardCharsets.UTF_8);
    File tmp = FileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);

    Set<PosixFilePermission> writable = Files.getPosixFilePermissions(resDir);
    Files.setPosixFilePermissions(resDir, PosixFilePermissions.fromString("r-xr-xr-x"));
    try {
      // root can still create files in a read-only directory
      assumeFalse(canCreateFile(resDir.resolve("probe")));
      FileUtils.moveIntoPlace(tmp, target);
      assertEquals("second", Files.readString(target.toPath(), StandardCharsets.UTF_8));
    } finally {
      Files.setPosixFilePermissions(resDir, writable);
    }
  }

  private static boolean canCreateFile(Path path) {
    try {
      return path.toFile().createNewFile();
    } catch (IOException e) {
      return false;
    }
  }
}
