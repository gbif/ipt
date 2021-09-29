package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateResourceActionTest {
  private static final Logger LOG = LogManager.getLogger(CreateResourceActionTest.class);
  private static final String SHORTNAME = "bugs";
  private CreateResourceAction action;
  private static final File RESOURCES_DIRECTORY = getResourceDirectory();

  @BeforeEach
  public void setup() throws IOException, DeletionNotAllowedException {
    // mock DataDir returning temp file (needed during import stage)
    DataDir mockDataDir =  mock(DataDir.class);
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(FileUtils.createTempDir());

    // mock DataDir returning resources directory
    when(mockDataDir.dataFile(anyString())).thenReturn(RESOURCES_DIRECTORY);

    // mock resource manager that returns resource with shortname "bugs"
    Resource res = new Resource();
    res.setShortname(SHORTNAME);
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    when(mockResourceManager.get(SHORTNAME)).thenReturn(res);

    // mock resource manager that deletes resource directory "bugs"
    doAnswer(invocation -> org.apache.commons.io.FileUtils.deleteQuietly(new File(RESOURCES_DIRECTORY, SHORTNAME)))
      .when(mockResourceManager).delete(res, true);

    // mock action
    action =
      new CreateResourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
        mockResourceManager, mockDataDir, mock(VocabulariesManager.class));
  }

  /**
   * Trigger an ImportException calling save() while creating a new resource. Verify the right result happens
   * and the action error is stored.
   */
  @Test
  public void testSaveImportException() throws IOException {
    action.setFile(File.createTempFile("archive", "zip"));
    action.setShortname("otherShortname");
    action.setFileFileName("archive.zip");
    String result = action.save();

    assertEquals("input", result);
    // ImportException logged in ActionError
    assertEquals(1, action.getActionErrors().size());
  }

  @Test
  public void testCleanupResourceFolder() throws IOException, InterruptedException {
    Date start = new Date();
    long startTimeInMs = start.getTime() - 5000;
    LOG.info("Start in milliseconds: " + startTimeInMs);

    DataDir dataDir = action.getDataDir();
    File resourcesDirectory = dataDir.dataFile(DataDir.RESOURCES_DIR);
    File resourceDirectory = new File(resourcesDirectory, SHORTNAME);
    resourceDirectory.mkdir();
    assertTrue(resourceDirectory.exists());
    File sources = new File (resourceDirectory, "sources");
    sources.mkdir();
    assertTrue(sources.exists());
    assertEquals(1, resourceDirectory.listFiles().length);
    File occurrences = new File (sources, "occurrences.txt");
    occurrences.createNewFile();
    assertTrue(occurrences.exists());

    LOG.info("Resource directory last modified: " + resourceDirectory.lastModified());
    assertTrue(resourceDirectory.lastModified() > startTimeInMs);

    action.cleanupResourceFolder(SHORTNAME, startTimeInMs);
    assertFalse(resourceDirectory.exists());
    assertFalse(sources.exists());
    assertFalse(occurrences.exists());
    assertNull(resourceDirectory.listFiles());
  }

  private static File getResourceDirectory() {
    File f = null;
    try {
      f = FileUtils.createTempDir();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return f;
  }
}
