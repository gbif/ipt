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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.IptBaseTest;
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
import java.util.Arrays;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SourceActionTest extends IptBaseTest {

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

  @Test
  public void testAlertFileNotChecked() {
    // the text provider returns the key and its arguments, to see what was reported
    SimpleTextProvider textProvider = mock(SimpleTextProvider.class);
    when(textProvider.getText(any(), anyString(), any(), any(Object[].class)))
      .thenAnswer(i -> i.getArgument(1) + " " + Arrays.toString((Object[]) i.getArgument(3)));
    action = new SourceAction(textProvider, mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));
    HttpServletResponse response = mock(HttpServletResponse.class);
    action.withServletResponse(response);

    TextFileSource src = new TextFileSource();
    src.setName("occurrence");
    src.setEncoding("UTF-8");

    // checked without problems
    action.alertFileNotChecked(src);
    assertTrue(action.getActionWarnings().isEmpty());
    verify(response, never()).setHeader(anyString(), anyString());

    src.setFirstLineWithInvalidCharacters(188715);
    src.setQuotedValuesUnreadable(true);
    action.alertFileNotChecked(src);
    assertEquals(Arrays.asList("manage.source.invalidCharacters [occurrence, UTF-8, 188715]",
                               "manage.source.quotedValuesUnreadable [occurrence]"), action.getActionWarnings());
    verify(response).setHeader("X-Warning-Message", "manage.source.quotedValuesUnreadable [occurrence]");
  }
}
