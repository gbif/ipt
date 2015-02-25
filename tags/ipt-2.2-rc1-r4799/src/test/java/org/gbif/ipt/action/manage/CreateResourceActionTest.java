package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateResourceActionTest {

  private CreateResourceAction action;

  @Before
  public void setup() throws IOException {
    // mock data dir
    DataDir mockDataDir =  mock(DataDir.class);
    when(mockDataDir.tmpFile(anyString(), anyString())).thenReturn(FileUtils.createTempDir());
    // mock action
    action =
      new CreateResourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
        mock(ResourceManager.class), mockDataDir, mock(VocabulariesManager.class));

  }

  /**
   * Trigger an ImportException calling save() while creating a new resource. Verify the right result happens
   * and the action error is stored.
   */
  @Test
  public void testSaveImportException() throws IOException {
    action.setFile(File.createTempFile("archive", "zip"));
    action.setShortname("shortname");
    action.setFileFileName("archive.zip");
    String result = action.save();

    assertEquals("input", result);
    // ImportException logged in ActionError
    assertEquals(1, action.getActionErrors().size());
  }
}
