package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CreateResourceActionTest {

  private CreateResourceAction action;

  @Before
  public void setup() {
    // mock action
    action =
      new CreateResourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
        mock(ResourceManager.class), mock(DataDir.class));
  }

  @Test
  public void testSaveImportException() throws IOException {
    action.setFileFileName("resource.xls");
    String result = action.save();
    assertEquals("input", result);
    // ImportException logged in ActionError
    assertEquals(1, action.getActionErrors().size());
  }
}
