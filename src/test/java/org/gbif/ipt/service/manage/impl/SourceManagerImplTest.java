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
import org.gbif.ipt.model.SourceFileProblem;
import org.gbif.ipt.model.SourceFileProblem.Type;
import org.gbif.ipt.model.SourceLog;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.file.DataFile;
import org.gbif.ipt.service.file.FileStoreManager;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceManagerImplTest extends IptBaseTest {

  private SourceManagerImpl manager;
  private Resource resource;
  private TextFileSource src1;
  private SqlSource src2;
  private File logFile;
  private File logJsonFile;
  private DataDir mockDataDir;
  private FileStoreManager mockFileStoreManager;

  @BeforeEach
  public void setup() throws IOException {
    File ddFile = File.createTempFile("distribution", ".txt");
    logFile = File.createTempFile("distribution", "log");
    mockDataDir = mock(DataDir.class);
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(ddFile);
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(logFile);
    logJsonFile = File.createTempFile("distribution", "log.json");
    when(mockDataDir.sourceLogJsonFile(anyString(), anyString())).thenReturn(logJsonFile);
    // create instance of SourceManager, using mocked AppConfig and DataDir
    mockFileStoreManager = mock(FileStoreManager.class);
    manager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir, mockFileStoreManager);
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
    // with its logs
    assertFalse(logFile.exists());
    assertFalse(logJsonFile.exists());
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
    assertProblems(src, new SourceFileProblem(Type.INVALID_CHARACTERS, 1, 300002));
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
    assertProblems(src);
    assertTrue(logText(src).contains(
      "Fixed when the file was uploaded: 1 line break(s) inside quoted values were removed"));
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
    assertProblems(src, new SourceFileProblem(Type.UNREADABLE_QUOTES, 1, 2));
    // the page reads the same from the JSON log
    SourceLog log = manager.readLog(src);
    assertEquals("UTF-8", log.getEncoding());
    assertEquals("tab", log.getDelimiter());
    assertEquals("\"", log.getQuote());
    assertEquals(3, log.getRows());
    SourceLog.Example example = log.getProblems().get(0).getExamples().get(0);
    assertEquals(2, example.getLine());
    assertEquals(2, example.getColumnNumber());
    assertEquals("remarks", example.getColumn());
    assertEquals("\"first\" and more", example.getValue());
    assertEquals(SourceLog.Reason.TEXT_AFTER_QUOTE, example.getReason());
    assertTrue(logText(src).contains("Line 2, column remarks: text after the closing quote: \"first\" and more"));
    assertTrue(src.isReadable());
    assertEquals(2, src.getColumns());
    assertArrayEquals(java.nio.file.Files.readAllBytes(srcFile.toPath()),
                      java.nio.file.Files.readAllBytes(src.getFile().toPath()));
  }

  @Test
  public void testFailedAnalysisLeavesNoLog(@TempDir File tmpDir) throws Exception {
    File srcFile = new File(tmpDir, "quotes.txt");
    java.nio.file.Files.write(srcFile.toPath(),
      "id\tremarks\n1\t\"first\" and more\n".getBytes(StandardCharsets.UTF_8));
    TextFileSource src = (TextFileSource) manager.add(resource, srcFile, srcFile.getName());
    // the log is kept as JSON only
    assertFalse(logFile.exists());
    assertTrue(logJsonFile.exists());
    // the text log of a source analysed before that
    java.nio.file.Files.write(logFile.toPath(), "old".getBytes(StandardCharsets.UTF_8));

    // the file can't be read anymore: what was found in it before no longer holds
    assertTrue(src.getFile().delete());
    assertNotNull(manager.analyze(src));
    assertNull(src.getProblems());
    assertFalse(logFile.exists());
    assertFalse(logJsonFile.exists());
    assertNull(manager.readLog(src));
  }

  @Test
  public void testUrlSourceIsCheckedWhenDownloaded(@TempDir File tmpDir) throws Exception {
    File downloadDir = downloadsOnce(tmpDir,
      "id\tlocality\tremarks\n1\tOslo\t\"near the road, in grass\"\n2\tDragÃ¸r\tin grass\n"
      + "3\tMalmö\t\"first\" and more\n");
    HttpServer server = startServer();
    try {
      UrlSource src = manager.add(resource, url(server), null);

      assertEquals("occurrence", src.getName());
      assertFalse(src.isProcessing());
      assertTrue(src.isReadable());
      assertEquals(3, src.getRows());
      assertEquals(2, src.getProblems().size());
      assertEquals(Type.UNREADABLE_QUOTES, src.getProblems().get(0).getType());
      assertEquals(4, src.getProblems().get(0).getFirstLine());
      assertEquals(Type.DAMAGED_VALUES, src.getProblems().get(1).getType());
      assertEquals(3, src.getProblems().get(1).getFirstLine());

      SourceLog log = manager.readLog(src);
      assertEquals("tab", log.getDelimiter());
      assertEquals("\"", log.getQuote());
      assertEquals("DragÃ¸r", log.getProblems().get(1).getExamples().get(0).getValue());
      assertTrue(logText(src).contains("Line 3, column locality: DragÃ¸r"));

      // analysed where it was downloaded, and not kept: the source is read from the URL
      assertNull(src.getFile());
      assertFalse(downloadDir.exists());
    } finally {
      server.stop(0);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUrlSourceDownloadFails(@TempDir File tmpDir) throws Exception {
    downloadsOnce(tmpDir, "id\tlocality\n1\tDragÃ¸r\n");
    HttpServer server = startServer();
    try {
      UrlSource src = manager.add(resource, url(server), null);
      assertEquals(1, src.getProblems().size());
      assertTrue(logJsonFile.exists());

      doAnswer(i -> {
        ((Consumer<Throwable>) i.getArgument(3)).accept(new IOException("Not found"));
        return null;
      }).when(mockFileStoreManager).downloadDataFile(anyString(), anyString(), any(), any());
      manager.analyze(src);
      // what was found in the earlier download doesn't hold anymore
      assertFalse(src.isProcessing());
      assertFalse(src.isReadable());
      assertNull(src.getProblems());
      assertFalse(logJsonFile.exists());
    } finally {
      server.stop(0);
    }
  }

  @Test
  public void testOldCopiesOfUrlSourcesAreDeleted(@TempDir File tmpDir) throws Exception {
    File sources = new File(tmpDir, "sources");
    assertTrue(sources.mkdir());
    when(mockDataDir.sourceFile(any(Resource.class), anyString()))
      .thenAnswer(i -> new File(sources, (String) i.getArgument(1)));
    // copies kept by earlier versions, with the paths of wherever the data directory was then
    File copy = new File(sources, "occurrence.txt");
    java.nio.file.Files.write(copy.toPath(), "id\n1\n".getBytes(StandardCharsets.UTF_8));
    File taxon = new File(sources, "taxon.txt");
    java.nio.file.Files.write(taxon.toPath(), "id\n1\n".getBytes(StandardCharsets.UTF_8));
    src1.setFile(taxon);
    resource.addSource(src1, false);

    downloadsOnce(tmpDir, "id\tlocality\n1\tOslo\n");
    HttpServer server = startServer();
    try {
      UrlSource occurrence = new UrlSource();
      occurrence.setName("occurrence");
      occurrence.setUrl(url(server));
      occurrence.setFile(new File("/var/lib/elsewhere/sources/occurrence.txt"));
      resource.addSource(occurrence, false);
      // analysed again
      manager.analyze(occurrence);
      assertNull(occurrence.getFile());
      assertFalse(copy.exists());

      // the same name as an uploaded source's file, which could have overwritten it
      UrlSource sameName = new UrlSource();
      sameName.setName("taxa");
      sameName.setUrl(url(server));
      sameName.setFile(new File("/var/lib/elsewhere/sources/taxon.txt"));
      resource.addSource(sameName, false);
      manager.delete(resource, sameName);
      assertTrue(taxon.exists());
    } finally {
      server.stop(0);
    }
  }

  /**
   * Makes the file store download the content at once, as occurrence.txt in a download directory.
   *
   * @return the download directory
   */
  @SuppressWarnings("unchecked")
  private File downloadsOnce(File tmpDir, String content) throws IOException {
    File downloadDir = new File(tmpDir, "download");
    assertTrue(downloadDir.mkdir());
    java.nio.file.Files.write(new File(downloadDir, "occurrence.txt").toPath(),
      content.getBytes(StandardCharsets.UTF_8));
    when(mockDataDir.tmpFile(anyString())).thenReturn(downloadDir);
    when(mockFileStoreManager.downloadDataFile(anyString(), anyString(), any(), any())).thenAnswer(i -> {
      ((Consumer<DataFile>) i.getArgument(2)).accept(null);
      return null;
    });
    return downloadDir;
  }

  /**
   * @return a server to ask whether a URL redirects, which is done before it's downloaded
   */
  private static HttpServer startServer() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext("/", exchange -> {
      exchange.sendResponseHeaders(200, -1);
      exchange.close();
    });
    server.start();
    return server;
  }

  private static URI url(HttpServer server) {
    return URI.create("http://localhost:" + server.getAddress().getPort() + "/occurrence.txt");
  }

  private void assertProblems(TextFileSource src, SourceFileProblem... expected) {
    assertEquals(expected.length, src.getProblems().size());
    for (int i = 0; i < expected.length; i++) {
      SourceFileProblem actual = src.getProblems().get(i);
      assertEquals(expected[i].getType(), actual.getType());
      assertEquals(expected[i].getLines(), actual.getLines());
      assertEquals(expected[i].getFirstLine(), actual.getFirstLine());
    }
  }

  // the log as it's downloaded
  private String logText(Source src) {
    return manager.logText(src, manager.readLog(src));
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
    assertProblems(src);
    // stored exactly as uploaded
    assertArrayEquals(java.nio.file.Files.readAllBytes(srcFile.toPath()),
        java.nio.file.Files.readAllBytes(src.getFile().toPath()));
  }
}
