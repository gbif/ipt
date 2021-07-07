package org.gbif.ipt.service.manage.impl;

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
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceManagerImplTest {

  private SourceManagerImpl manager;
  private Resource resource;
  private TextFileSource src1;
  private SqlSource src2;

  @Before
  public void setup() throws IOException {
    File ddFile = File.createTempFile("distribution", ".txt");
    File logFile = File.createTempFile("distribution", "log");
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenReturn(ddFile);
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(logFile);
    // create instance of SourceManager, using mocked AppConfig and DataDir
    manager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);
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

  @Test (expected=AlreadyExistingException.class)
  public void testAddDuplicateSource() throws AlreadyExistingException {
    resource.addSource(src1, false);
    resource.addSource(src2, false);

    assertEquals(2, resource.getSources().size());

    Source src3 = new SqlSource();
    src3.setName("Identifications");

    // expected to throw AlreadyExistingException
    resource.addSource(src3, false);
  }

  @Test (expected=InvalidFilenameException.class)
  public void testAddSourceWithInvalidFilename() throws IOException, InvalidFilenameException, ImportException {
    manager.add(resource, File.createTempFile("taxøn", "txt"), "taxøn.txt");
  }

  @Test
  public void testAnalyze() throws ImportException, IOException, InvalidFilenameException {
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
    assertEquals(null, ((TextFileSource) fileSource).getFieldsEnclosedBy());
    assertEquals("\t", ((TextFileSource) fileSource).getFieldsTerminatedBy());
  }

  @Ignore("dwca-io does not validate this anymore")
  @Test(expected = ImportException.class)
  public void testAnalyzeEmptyFile() throws InvalidFilenameException, ImportException {
    // analyze individual source file absolutely no data inside at all
    File srcFile = FileUtils.getClasspathFile("data/image_empty.txt");
    // add source file to test Resource
    manager.add(resource, srcFile, srcFile.getName());
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
