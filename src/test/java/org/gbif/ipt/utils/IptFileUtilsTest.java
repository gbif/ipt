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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

  @Test
  void testImageType(@TempDir Path dir) throws IOException {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    // the format comes from the content, not the file name
    for (String format : new String[] {"jpeg", "png", "gif"}) {
      File file = dir.resolve("logo-" + format + ".txt").toFile();
      assertTrue(ImageIO.write(image, format, file));
      assertEquals(format, IptFileUtils.imageType(file));
    }

    File bmp = dir.resolve("logo.png").toFile();
    assertTrue(ImageIO.write(image, "bmp", bmp));
    assertNull(IptFileUtils.imageType(bmp));

    File text = Files.writeString(dir.resolve("logo.jpeg"), "<svg/>").toFile();
    assertNull(IptFileUtils.imageType(text));

    assertNull(IptFileUtils.imageType(dir.resolve("missing.png").toFile()));
    assertNull(IptFileUtils.imageType(null));
  }

  @Test
  public void testTempSiblingMovedIntoPlace(@TempDir Path dir) throws Exception {
    // the target's directory doesn't exist yet
    File target = dir.resolve("res").resolve("datapackage.json").toFile();

    File tmp = IptFileUtils.createTempSibling(target);
    assertEquals(target.getAbsoluteFile().getParentFile(), tmp.getParentFile());
    Files.writeString(tmp.toPath(), "first", StandardCharsets.UTF_8);
    IptFileUtils.moveIntoPlace(tmp, target);

    tmp = IptFileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);
    IptFileUtils.moveIntoPlace(tmp, target);

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
    File tmp = IptFileUtils.createTempSibling(target);
    assertEquals(defaults, Files.getPosixFilePermissions(tmp.toPath()));
    IptFileUtils.moveIntoPlace(tmp, target);
    assertEquals(defaults, Files.getPosixFilePermissions(target.toPath()));
  }

  @Test
  public void testMoveIntoPlaceKeepsTargetPermissions(@TempDir Path dir) throws Exception {
    assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    File target = dir.resolve("eml.xml").toFile();
    Files.writeString(target.toPath(), "first", StandardCharsets.UTF_8);
    Set<PosixFilePermission> custom = PosixFilePermissions.fromString("rw-rw----");
    Files.setPosixFilePermissions(target.toPath(), custom);

    File tmp = IptFileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);
    IptFileUtils.moveIntoPlace(tmp, target);

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
    File tmp = IptFileUtils.createTempSibling(target);
    Files.writeString(tmp.toPath(), "second", StandardCharsets.UTF_8);

    Set<PosixFilePermission> writable = Files.getPosixFilePermissions(resDir);
    Files.setPosixFilePermissions(resDir, PosixFilePermissions.fromString("r-xr-xr-x"));
    try {
      // root can still create files in a read-only directory
      assumeFalse(canCreateFile(resDir.resolve("probe")));
      IptFileUtils.moveIntoPlace(tmp, target);
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
