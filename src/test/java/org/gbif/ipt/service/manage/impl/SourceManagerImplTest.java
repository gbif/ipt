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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.file.FileStoreManager;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceManagerImplTest extends IptBaseTest {

  private SourceManagerImpl manager;
  private Resource resource;
  private TextFileSource src1;
  private SqlSource src2;

  @BeforeEach
  public void setup() throws IOException {
    File ddFile = File.createTempFile("distribution", ".txt");
    File logFile = File.createTempFile("distribution", "log");
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(ddFile);
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(logFile);
    // create instance of SourceManager, using mocked AppConfig and DataDir
    manager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir, mock(FileStoreManager.class));
    // create test Resource
    resource = new Resource();
    resource.setShortname("testResource");
    // creates test sources
    src1 = new TextFileSource();
    src1.setName("Taxon");
    src1.setResource(resource);
    src1.setFile(File.createTempFile("tmp", ".txt"));
    src2 = new SqlSource();
    src2.setName("Identifications");
    src2.setResource(resource);
  }

  @Test
  public void testDeleteSqlSource() throws AlreadyExistingException {
    resource.addSource(src1, false);
    resource.addSource(src2, false);
    assertEquals(2, resource.getSources().size());

    // perform deletion of SqlSource
    manager.delete(resource, src2);
    assertEquals(1, resource.getSources().size());
  }

  @Test
  public void testDeleteFileSource() throws AlreadyExistingException {
    resource.addSource(src1, false);
    resource.addSource(src2, false);
    assertEquals(2, resource.getSources().size());

    // perform deletion of TextFileSource
    manager.delete(resource, src1);
    assertEquals(1, resource.getSources().size());
  }

  @Test
  public void testAddDuplicateSource() throws AlreadyExistingException {
    resource.addSource(src1, false);
    resource.addSource(src2, false);

    assertEquals(2, resource.getSources().size());

    Source src3 = new SqlSource();
    src3.setName("Identifications");

    // expected to throw AlreadyExistingException
    assertThrows(AlreadyExistingException.class, () -> resource.addSource(src3, false));
  }

  @Test
  public void testAddSourceWithInvalidFilename() {
    assertThrows(InvalidFilenameException.class,
        () -> manager.add(resource, File.createTempFile("taxøn", "txt"), "taxøn.txt"));
  }

  @Test
  public void testAnalyze() throws Exception {
    // analyze individual source file with no header row, and 77 real rows of source data
    File srcFile = FileUtils.getClasspathFile("data/distribution.txt");
    // add source file to test Resource
    FileSource src = manager.add(resource, srcFile, srcFile.getName());
    assertEquals("distribution", src.getName());

    assertTrue(src.isFileSource());
    TextFileSource fileSource = (TextFileSource) src;


    // As of dwca-reader 1.11, ArchiveFactory.openArchive(file) / ArchiveFactory.readFileHeaders
    // assigns the default ignoreHeaderLines = 1, even if the first line contains purely UnknownTerms
    // see: http://code.google.com/p/darwincore/issues/detail?id=159
    assertEquals(1, fileSource.getIgnoreHeaderLines());
    assertEquals(3, fileSource.getColumns());
    assertEquals(76, fileSource.getRows());
    assertEquals(2018, fileSource.getFileSize());
    assertTrue(fileSource.isReadable());
    assertEquals("\"", fileSource.getFieldsEnclosedBy());
    assertEquals("\t", fileSource.getFieldsTerminatedBy());
  }

  @Test
  public void testAnalyzeEmptyFile() {
    // analyze individual source file absolutely no data inside at all
    File srcFile = FileUtils.getClasspathFile("data/image_empty.txt");
    // add source file to test Resource
    assertThrows(ImportException.class, () -> manager.add(resource, srcFile, srcFile.getName()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r"})
  public void testAddFileWithInvalidBytesAfterDetectedEncoding(String lineEnding, @TempDir File tmpDir)
    throws Exception {
    // ~5MB of plain ASCII, more than encoding detection reads, so the encoding is detected as UTF-8, then a byte
    // that isn't valid UTF-8 on line 300002
    StringBuilder sb = new StringBuilder("id\tname").append(lineEnding);
    for (int i = 1; i <= 300000; i++) {
      sb.append(i).append("\tname ").append(i).append(lineEnding);
    }
    sb.append("x\tSkakbr?");
    File srcFile = new File(tmpDir, "invalidBytes.txt");
    try (FileOutputStream out = new FileOutputStream(srcFile)) {
      out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
      out.write(0xAC);
      out.write(("t" + lineEnding).getBytes(StandardCharsets.US_ASCII));
    }

    // added as uploaded, with the line to warn about
    TextFileSource src = (TextFileSource) manager.add(resource, srcFile, srcFile.getName());
    assertEquals(300002, src.getFirstLineWithInvalidCharacters());
    assertTrue(src.isReadable());
    assertEquals(300001, src.getRows());
    assertArrayEquals(java.nio.file.Files.readAllBytes(srcFile.toPath()),
                      java.nio.file.Files.readAllBytes(src.getFile().toPath()));
  }

  @Test
  public void testAddFileWithEmbeddedLineBreakIsNormalized(@TempDir File tmpDir) throws Exception {
    File srcFile = new File(tmpDir, "remarks.txt");
    java.nio.file.Files.write(srcFile.toPath(),
      "id\tremarks\n1\t\"first\nsecond\"\n2\tthird\n3\tfourth\n".getBytes(StandardCharsets.UTF_8));

    TextFileSource src = (TextFileSource) manager.add(resource, srcFile, srcFile.getName());
    assertEquals(1, src.getLinesWithEmbeddedBreaksRemoved());
    assertFalse(src.isQuotedValuesUnreadable());
    assertEquals(3, src.getRows());
    assertEquals(4, java.nio.file.Files.readAllLines(src.getFile().toPath()).size());
  }

  @Test
  public void testAddFileWithUnreadableQuotedValue(@TempDir File tmpDir) throws Exception {
    // a closing quote followed by more text in the same value
    File srcFile = new File(tmpDir, "quotes.txt");
    java.nio.file.Files.write(srcFile.toPath(),
      "id\tremarks\n1\t\"first\" and more\n2\tsecond\n3\tthird\n".getBytes(StandardCharsets.UTF_8));

    // added as uploaded, to warn about
    TextFileSource src = (TextFileSource) manager.add(resource, srcFile, srcFile.getName());
    assertTrue(src.isQuotedValuesUnreadable());
    assertEquals(0, src.getFirstLineWithInvalidCharacters());
    assertTrue(src.isReadable());
    assertEquals(2, src.getColumns());
    assertArrayEquals(java.nio.file.Files.readAllBytes(srcFile.toPath()),
                      java.nio.file.Files.readAllBytes(src.getFile().toPath()));
  }

  @ParameterizedTest
  @MethodSource("undecodableLineCases")
  public void testLineOfFirstUndecodableBytes(byte[] content, Charset charset, long expectedLine, @TempDir File tmpDir)
    throws IOException {
    File file = new File(tmpDir, "file.txt");
    java.nio.file.Files.write(file.toPath(), content);
    assertEquals(expectedLine, SourceManagerImpl.lineOfFirstUndecodableBytes(file, charset));
  }

  static Stream<Arguments> undecodableLineCases() {
    return Stream.of(
      // invalid byte on the first line
      Arguments.of(new byte[]{'a', (byte) 0xAC, '\n', 'b', '\n'}, StandardCharsets.UTF_8, 1L),
      // \r\n, \n and \r line endings
      Arguments.of(new byte[]{'a', '\r', '\n', 'b', '\r', '\n', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      Arguments.of(new byte[]{'a', '\n', 'b', '\n', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      Arguments.of(new byte[]{'a', '\r', 'b', '\r', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      // a multi-byte sequence cut off at the end of the file
      Arguments.of(new byte[]{'a', '\n', 'b', '\n', 'c', (byte) 0xC3}, StandardCharsets.UTF_8, 3L),
      // a lone surrogate in UTF-16LE on the second line
      Arguments.of(new byte[]{'a', 0, '\n', 0, 'b', 0, 0, (byte) 0xD8, 'c', 0}, StandardCharsets.UTF_16LE, 2L),
      // a byte windows-1252 doesn't define
      Arguments.of(new byte[]{'a', '\n', (byte) 0x81}, Charset.forName("windows-1252"), 2L));
  }

  @Test
  public void testAcceptableFileName() {
    // accepted names
    assertTrue(manager.acceptableFileName("taxon.txt"));
    assertTrue(manager.acceptableFileName("taxon 1.csv"));
    assertTrue(manager.acceptableFileName("taxon (1).tab"));
    assertTrue(manager.acceptableFileName("taxon-1.xls"));
    assertTrue(manager.acceptableFileName("taxon_2.xls"));

    // non accepted names
    assertFalse(manager.acceptableFileName("taxøn.txt"));
    assertFalse(manager.acceptableFileName("taxoñ.txt"));
    assertFalse(manager.acceptableFileName("taxon & aves.txt"));
  }

  @Test
  public void testAddFileWithCommasInValuesKeepsTabDelimiter(@TempDir File tmpDir) throws Exception {
    // tabs split every line evenly, but the WKT values make commas more frequent
    StringBuilder sb = new StringBuilder("id\tlocality\tfootprintWKT\n");
    for (int i = 1; i <= 20; i++) {
      sb.append(i).append("\tDrag\u00f8r\t\"POLYGON ((");
      for (int j = 0; j < i % 5 + 3; j++) {
        sb.append(j).append(' ').append(j).append(", ");
      }
      sb.append("0 0))\"\n");
    }
    File srcFile = new File(tmpDir, "event.txt");
    java.nio.file.Files.write(srcFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));

    TextFileSource src = (TextFileSource) manager.add(resource, srcFile, srcFile.getName());
    assertEquals("\t", src.getFieldsTerminatedBy());
    assertEquals(3, src.getColumns());
    assertEquals(0, src.getLinesWithEmbeddedBreaksRemoved());
    assertEquals(0, src.getFirstLineWithInvalidCharacters());
    assertFalse(src.isQuotedValuesUnreadable());
    // stored exactly as uploaded
    assertArrayEquals(java.nio.file.Files.readAllBytes(srcFile.toPath()),
        java.nio.file.Files.readAllBytes(src.getFile().toPath()));
  }
}
