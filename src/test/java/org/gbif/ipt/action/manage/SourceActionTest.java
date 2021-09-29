package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SourceActionTest {

  private SourceAction action;

  @BeforeEach
  public void setup() throws IOException {
    // mock action
    action = new SourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));
  }

  @Test
  public void testAlertColumnNumberChange() {
    Source src = new TextFileSource();
    src.setName("images");
    action.setSource(src);

    assertFalse(action.alertColumnNumberChange(false, 1, 2));
    assertFalse(action.alertColumnNumberChange(true, 0, 0));
    assertFalse(action.alertColumnNumberChange(true, 10, 10));
    assertTrue(action.alertColumnNumberChange(true, 1, 2));
    assertTrue(action.alertColumnNumberChange(true, 2, 1));
  }
}
