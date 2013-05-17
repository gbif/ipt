package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceManagerImplTest {

  private SourceManager manager;
  private Resource resource;
  private Source.FileSource src1;
  private SqlSource src2;

  @Before
  public void setup() throws IOException {
    File ddFile = File.createTempFile("distribution", ".txt");
    File logFile = File.createTempFile("distribution", "log");
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.sourceFile(any(Resource.class), any(Source.class))).thenReturn(ddFile);
    when(mockDataDir.sourceLogFile(anyString(), anyString())).thenReturn(logFile);
    // create instance of SourceManager, using mocked AppConfig and DataDir
    manager = new SourceManagerImpl(mock(AppConfig.class), mockDataDir);
    // create test Resource
    resource = new Resource();
    resource.setShortname("testResource");
    // creates test sources
    src1 = new Source.FileSource();
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

    // perform deletion of FileSource
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

  @Test
  public void testAnalyze() throws ImportException, IOException {
    // analyze individual source file with no header row, and 77 real rows of source data
    File srcFile = FileUtils.getClasspathFile("data/distribution.txt");
    // add source file to test Resource
    Source.FileSource fileSource = manager.add(resource, srcFile, null);
    assertEquals("distribution", fileSource.getName());

    // As of dwca-reader 1.11, ArchiveFactory.openArchive(file) / ArchiveFactory.readFileHeaders
    // assigns the default ignoreHeaderLines = 1, even if the first line contains purely UnknownTerms
    // see: http://code.google.com/p/darwincore/issues/detail?id=159
    assertEquals(1, fileSource.getIgnoreHeaderLines());
    assertEquals(3, fileSource.getColumns());
    assertEquals(76, fileSource.getRows());
    assertEquals(2018, fileSource.getFileSize());
    assertEquals(null, fileSource.getFieldsEnclosedBy());
    assertTrue(fileSource.isReadable());
    assertEquals("\t", fileSource.getFieldsTerminatedBy());
  }

  @Test
  public void testAnalyzeEmptyFile() throws ImportException, IOException {
    // analyze individual source file absolutely no data inside at all
    File srcFile = FileUtils.getClasspathFile("data/image_empty.txt");
    // add source file to test Resource
    Source.FileSource fileSource = manager.add(resource, srcFile, null);
    assertEquals("image_empty", fileSource.getName());
    // ensure all properties reflect the fact there is no data in this file
    assertEquals(0, fileSource.getIgnoreHeaderLines());
    assertEquals(0, fileSource.getColumns());
    assertEquals(0, fileSource.getRows());
    assertEquals(0, fileSource.getFileSize());
    assertEquals(null, fileSource.getFieldsEnclosedBy());
    assertTrue(fileSource.isReadable());
    assertEquals("\t", fileSource.getFieldsTerminatedBy());
  }
}
