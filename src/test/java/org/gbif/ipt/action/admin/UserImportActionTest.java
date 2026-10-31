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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.UserImportRow;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.dispatcher.multipart.StrutsUploadedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserImportActionTest {

  @TempDir
  Path tempDir;

  private UserAccountManager userManager;
  private UserImportAction action;
  private Map<String, Object> session;

  @BeforeEach
  public void setup() {
    userManager = mock(UserAccountManager.class);
    action = new UserImportAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), userManager);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn("POST");
    action.withServletRequest(req);

    User admin = new User();
    admin.setEmail("admin@example.org");
    admin.setRole(Role.Admin);
    session = new HashMap<>();
    session.put(Constants.SESSION_USER, admin);
    action.withSession(session);
  }

  private void upload(String content) throws Exception {
    Path file = Files.createTempFile(tempDir, "users", ".csv");
    Files.writeString(file, content, StandardCharsets.UTF_8);
    action.withUploadedFiles(List.of(StrutsUploadedFile.Builder.create(file.toFile())
        .withContentType("text/csv").withOriginalName("users.csv").withInputName("file").build()));
  }

  private void submit(String[]... rows) {
    action.setLines(column(rows, 0));
    action.setFirstnames(column(rows, 1));
    action.setLastnames(column(rows, 2));
    action.setEmails(column(rows, 3));
    action.setRoles(column(rows, 4));
  }

  private static String[] column(String[][] rows, int index) {
    String[] values = new String[rows.length];
    for (int i = 0; i < rows.length; i++) {
      values[i] = rows[i][index];
    }
    return values;
  }

  @Test
  public void testUploadDoesNotCreateUsers() throws Exception {
    upload("firstname,lastname,email\nJohn,Doe,john@example.org\nJane,Doe,bad-email\n");

    action.upload();

    assertTrue(action.isPreview());
    assertEquals(1, action.getValidCount());
    assertEquals(1, action.getInvalidCount());
    verify(userManager, never()).create(any());
  }

  @Test
  public void testRevalidateEditedRows() throws Exception {
    // invalid email fixed, row added in the browser, empty row ignored
    submit(new String[] {"2", "John", "Doe", "john@example.org", "User"},
        new String[] {"3", "Jane", "Doe", "jane@example.org", "Root"},
        new String[] {"0", "Ann", "Roe", "ann@example.org", "Admin"},
        new String[] {"0", "", " ", "", "User"});

    action.revalidate();

    assertTrue(action.isPreview());
    List<UserImportRow> rows = action.getRows();
    assertEquals(3, rows.size());
    assertEquals(List.of("admin.users.import.error.role"), rows.get(1).getErrors());
    assertEquals(0, rows.get(2).getLine());
    assertEquals(Role.Admin, rows.get(2).getRole());
    verify(userManager, never()).create(any());
  }

  @Test
  public void testConfirmCreatesValidUsersWithGeneratedPasswords() throws Exception {
    submit(new String[] {"2", "John", "Doe", "John@Example.org", "Manager"},
        new String[] {"3", "Jane", "Doe", "bad-email", "User"},
        new String[] {"0", "Ann", "Roe", "ann@example.org", ""});

    action.confirm();

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userManager, times(2)).create(captor.capture());
    assertEquals("john@example.org", captor.getAllValues().get(0).getEmail());
    assertEquals(Role.Manager, captor.getAllValues().get(0).getRole());
    assertEquals(Role.User, captor.getAllValues().get(1).getRole());

    List<UserImportRow> rows = action.getRows();
    assertTrue(action.isCompleted());
    assertTrue(rows.get(0).isCreated());
    assertEquals(12, rows.get(0).getPassword().length());
    assertNotEquals(rows.get(0).getPassword(), rows.get(2).getPassword());
    assertFalse(rows.get(1).isCreated());
    assertNull(rows.get(1).getPassword());
  }

  @Test
  public void testConfirmRevalidatesAgainstExistingUsers() throws Exception {
    // e.g. the same rows submitted twice
    when(userManager.get("john@example.org")).thenReturn(new User());
    submit(new String[] {"2", "John", "Doe", "john@example.org", "User"});

    action.confirm();

    verify(userManager, never()).create(any());
    assertEquals(List.of("admin.users.import.error.exists"), action.getRows().get(0).getErrors());
  }

  @Test
  public void testConfirmUserCreatedInTheMeantime() throws Exception {
    submit(new String[] {"2", "John", "Doe", "john@example.org", "User"});
    doThrow(new AlreadyExistingException()).when(userManager).create(any());

    action.confirm();

    UserImportRow row = action.getRows().get(0);
    assertFalse(row.isCreated());
    assertNull(row.getPassword());
    assertEquals(List.of("admin.users.import.error.exists"), row.getErrors());
  }

  @Test
  public void testConfirmWithoutRows() throws Exception {
    action.confirm();

    assertFalse(action.isCompleted());
    assertEquals(1, action.getActionErrors().size());
    verify(userManager, never()).create(any());
  }

  @Test
  public void testUploadWithoutFile() {
    action.upload();

    assertFalse(action.isPreview());
    assertEquals(1, action.getActionErrors().size());
  }
}
