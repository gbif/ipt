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
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

  @Disabled("dwca-io does not validate this anymore")
  @Test
  public void testAnalyzeEmptyFile() {
    // analyze individual source file absolutely no data inside at all
    File srcFile = FileUtils.getClasspathFile("data/image_empty.txt");
    // add source file to test Resource
    assertThrows(ImportException.class, () -> manager.add(resource, srcFile, srcFile.getName()));
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
}
