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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceFileProblem;
import org.gbif.ipt.model.SourceFileProblem.Type;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceActionTest extends IptBaseTest {

  private SourceAction action;
  private ResourceManager resourceManager;

  @BeforeEach
  public void setup() throws IOException {
    // mock action
    resourceManager = mock(ResourceManager.class);
    action = new SourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      resourceManager, mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));
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
  public void testAlertProblems() {
    // the text provider returns the key and its arguments, to see what was reported
    SimpleTextProvider textProvider = mock(SimpleTextProvider.class);
    when(textProvider.getText(any(), anyString(), any(), any(Object[].class)))
      .thenAnswer(i -> i.getArgument(1) + " " + Arrays.toString((Object[]) i.getArgument(3)));
    action = new SourceAction(textProvider, mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));

    TextFileSource src = new TextFileSource();
    src.setName("occurrence");
    src.setEncoding("UTF-8");

    // not analysed since problems were checked for
    action.alertProblems(src);
    assertTrue(action.getActionWarnings().isEmpty());

    // only empty lines, which are skipped
    src.setProblems(Collections.singletonList(new SourceFileProblem(Type.EMPTY_LINES, 2, 5)));
    action.alertProblems(src);
    assertTrue(action.getActionWarnings().isEmpty());

    // one problem, with how to fix it
    src.setProblems(Arrays.asList(new SourceFileProblem(Type.INVALID_CHARACTERS, 3, 188715),
                                  new SourceFileProblem(Type.EMPTY_LINES, 2, 5)));
    action.alertProblems(src);
    assertEquals(Collections.singletonList("manage.source.problem.found [occurrence, "
                   + "manage.source.problem.invalidCharacters [3, 188715, UTF-8]]"),
                 new ArrayList<>(action.getActionWarnings()));
    action.getActionWarnings().clear();

    // more than one: they're listed on the source page
    src.setProblems(Arrays.asList(new SourceFileProblem(Type.INVALID_CHARACTERS, 3, 188715),
                                  new SourceFileProblem(Type.UNREADABLE_QUOTES, 1, 12),
                                  new SourceFileProblem(Type.EMPTY_LINES, 2, 5)));
    action.alertProblems(src);
    assertEquals(Collections.singletonList("manage.source.problems.found [occurrence]"),
                 new ArrayList<>(action.getActionWarnings()));

    // on the source page in short, with the empty lines as a note
    action.setSource(src);
    assertEquals(Arrays.asList("manage.source.problem.invalidCharacters.short [3, 188715, UTF-8]",
                               "manage.source.problem.unreadableQuotes.short [1, 12, UTF-8]"),
                 action.getProblemMessages());
    assertEquals("manage.source.problem.emptyLines [2, 5, UTF-8]", action.getEmptyLinesMessage());

    // only empty lines: no problems
    src.setProblems(Collections.singletonList(new SourceFileProblem(Type.EMPTY_LINES, 2, 5)));
    assertTrue(action.getProblemMessages().isEmpty());
    assertEquals("manage.source.problem.emptyLines [2, 5, UTF-8]", action.getEmptyLinesMessage());

    // none
    src.setProblems(new ArrayList<>());
    assertNull(action.getEmptyLinesMessage());
  }

  @Test
  public void testUrlSourceProblemMessages() {
    SimpleTextProvider textProvider = mock(SimpleTextProvider.class);
    when(textProvider.getText(any(), anyString(), any(), any(Object[].class)))
      .thenAnswer(i -> i.getArgument(1) + " " + Arrays.toString((Object[]) i.getArgument(3)));
    action = new SourceAction(textProvider, mock(AppConfig.class), mock(RegistrationManager.class),
      mock(ResourceManager.class), mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));

    UrlSource src = new UrlSource();
    src.setEncoding("UTF-8");
    action.setSource(src);
    // not downloaded and checked yet
    assertNull(action.getProblemMessages());

    src.setProblems(Arrays.asList(new SourceFileProblem(Type.COLUMN_COUNT, 2, 7),
                                  new SourceFileProblem(Type.EMPTY_LINES, 1, 9)));
    assertEquals(Collections.singletonList("manage.source.problem.columnCount.short [2, 7, UTF-8]"),
                 action.getProblemMessages());
    // fixed in the file at the URL, but empty lines need no fixing
    action.alertProblems(src);
    assertEquals(Collections.singletonList("manage.source.problem.found [null, "
                   + "manage.source.problem.url.columnCount [2, 7, UTF-8]]"),
                 new ArrayList<>(action.getActionWarnings()));
    assertEquals("manage.source.problem.emptyLines [1, 9, UTF-8]", action.getEmptyLinesMessage());
  }

  @Test
  public void testReadingSettings() {
    TextFileSource src = new TextFileSource();
    src.setEncoding("UTF-8");
    src.setIgnoreHeaderLines(1);
    src.setFieldsTerminatedBy("\t");
    src.setFieldsEnclosedBy(null);
    List<Object> original = SourceAction.readingSettings(src);

    // no quote sent back by the form as an empty value
    src.setFieldsEnclosedByEscaped("");
    assertEquals(original, SourceAction.readingSettings(src));
    // nor do settings that don't decide how the file is read
    src.setMultiValueFieldsDelimitedBy("|");
    assertEquals(original, SourceAction.readingSettings(src));

    src.setEncoding("windows-1252");
    assertNotEquals(original, SourceAction.readingSettings(src));
    src.setEncoding("UTF-8");
    src.setIgnoreHeaderLines(0);
    assertNotEquals(original, SourceAction.readingSettings(src));
    src.setIgnoreHeaderLines(1);
    src.setFieldsTerminatedByEscaped(",");
    assertNotEquals(original, SourceAction.readingSettings(src));
    src.setFieldsTerminatedByEscaped("\\t");
    src.setFieldsEnclosedByEscaped("\"");
    assertNotEquals(original, SourceAction.readingSettings(src));

    assertNull(SourceAction.readingSettings(new SqlSource()));
  }

  /**
   * No resource parameter: must return 404 instead of throwing an NPE.
   */
  @Test
  public void testNoResourceSelectedReturnsNotFound() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn("occurrence");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());

    action.prepare();
    assertEquals(SourceAction.NOT_FOUND, action.peek());
    assertEquals(SourceAction.NOT_FOUND, action.execute());

    // POST: validation must not touch the missing resource
    when(req.getMethod()).thenReturn("POST");
    action.validate();
    assertEquals(SourceAction.NOT_FOUND, action.execute());
  }

  /**
   * The resource exists but has no source with the requested id: must return 404 instead of rendering the source
   * form without a source.
   */
  @Test
  public void testUnknownSourceReturnsNotFound() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("r")).thenReturn("res1");
    when(req.getParameter("id")).thenReturn("occurrences");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());

    action.prepare();
    // an action error would make the workflow interceptor return the input form before execute()
    assertFalse(action.hasErrors());
    assertEquals(SourceAction.NOT_FOUND, action.execute());
    assertEquals(SourceAction.NOT_FOUND, action.peek());
    assertEquals(SourceAction.NOT_FOUND, action.delete());
  }

  /**
   * No source id: prepare() creates a new, unsaved SQL source, which peek must not try to preview (it threw a 500).
   */
  @Test
  public void testPeekWithoutIdReturnsBadRequest() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("r")).thenReturn("res1");
    when(req.getParameter("id")).thenReturn("");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());

    action.prepare();
    assertEquals(SourceAction.BAD_REQUEST, action.peek());
  }

  /**
   * No resource parameter: a resource stored in the session, e.g. by IPT versions up to 3.3.7, must not be used instead.
   */
  @Test
  public void testSessionResourceIsNotUsed() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn("occurrences");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put("curr_resource", "res1");
    action.setSession(session);

    action.prepare();
    assertNull(action.getResource());
    assertEquals(SourceAction.NOT_FOUND, action.execute());
  }

  /**
   * Saving a DuckDB source with connection options, an in-memory or a MotherDuck database in the database field must
   * fail validation: the options would undo the restrictions the database is opened with (#3283).
   */
  @ParameterizedTest
  @ValueSource(strings = {
      "/data/occ.duckdb;duckdb.read_only=false",
      "/data/occ.duckdb;session_init_sql_file=/data/init.sql",
      "/data/occ.duckdb?enable_external_access=true",
      ":memory:",
      "md:my_db"
  })
  public void testDuckDbDatabaseWithOptionsIsRefusedOnSave(String database) throws Exception {
    assertTrue(validateDuckDbSource(database).containsKey("sqlSource.database"));
  }

  @Test
  public void testDuckDbDatabaseFileIsAcceptedOnSave() throws Exception {
    assertFalse(validateDuckDbSource("/data/occurrences.duckdb").containsKey("sqlSource.database"));
  }

  private Map<String, List<String>> validateDuckDbSource(String database) throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("r")).thenReturn("res1");
    when(req.getMethod()).thenReturn("POST");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());
    action.prepare();

    SqlSource source = new SqlSource();
    source.setName("occurrences");
    source.setDatabase(database);
    source.setSql("SELECT * FROM occurrence");
    action.setSource(source);
    action.setRdbms("duckdb");
    action.validateHttpPostOnly();
    return action.getFieldErrors();
  }
}
