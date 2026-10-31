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
package org.gbif.ipt.validation;

import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.UserImportRow;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.validation.UserImportValidator.UserImportException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserImportValidatorTest {

  @TempDir
  Path tempDir;

  private UserAccountManager userManager;
  private final UserImportValidator validator = new UserImportValidator();

  @BeforeEach
  public void setup() {
    userManager = mock(UserAccountManager.class);
    when(userManager.get("existing@example.org")).thenReturn(new User());
  }

  private File csv(String content) throws IOException {
    Path file = Files.createTempFile(tempDir, "users", ".csv");
    Files.writeString(file, content, StandardCharsets.UTF_8);
    return file.toFile();
  }

  @Test
  public void testValidFile() throws Exception {
    List<UserImportRow> rows = validator.parse(csv(
        "firstname,lastname,email,role\n"
            + "John,Doe,John.Doe@example.org,manager\n"
            + "\"Smith, Jr\",Smith,jane@example.org,\n"), userManager);

    assertEquals(2, rows.size());
    UserImportRow first = rows.get(0);
    assertTrue(first.isValid());
    assertEquals(2, first.getLine());
    assertEquals("john.doe@example.org", first.getEmail());
    assertEquals(Role.Manager, first.getRole());

    UserImportRow second = rows.get(1);
    assertTrue(second.isValid());
    assertEquals("Smith, Jr", second.getFirstname());
    assertEquals(Role.User, second.getRole());
  }

  @Test
  public void testColumnOrderSemicolonsAndBom() throws Exception {
    List<UserImportRow> rows = validator.parse(csv(
        "\uFEFFEmail;Role;LastName;FirstName\n"
            + "john@example.org;Admin;Doe;John\n"), userManager);

    assertEquals(1, rows.size());
    UserImportRow row = rows.get(0);
    assertTrue(row.isValid());
    assertEquals("John", row.getFirstname());
    assertEquals("Doe", row.getLastname());
    assertEquals(Role.Admin, row.getRole());
  }

  @Test
  public void testInvalidRows() throws Exception {
    List<UserImportRow> rows = validator.parse(csv(
        "firstname,lastname,email,role\n"
            + "John,Doe,john@example.org,User\n"
            + "\n"
            + "Johnny,Doe,JOHN@example.org,User\n"
            + "Ann,Other,existing@example.org,User\n"
            + ",Nobody,not-an-email,Superuser\n"), userManager);

    assertEquals(4, rows.size());
    assertTrue(rows.get(0).isValid());

    UserImportRow duplicate = rows.get(1);
    assertEquals(4, duplicate.getLine());
    assertEquals(List.of("admin.users.import.error.duplicate"), duplicate.getErrors());

    assertEquals(List.of("admin.users.import.error.exists"), rows.get(2).getErrors());

    UserImportRow invalid = rows.get(3);
    assertFalse(invalid.isValid());
    assertTrue(invalid.getErrors().contains("validation.firstname.required"));
    assertTrue(invalid.getErrors().stream().anyMatch(e -> e.startsWith("validation.email.")));
    assertTrue(invalid.getErrors().contains("admin.users.import.error.role"));
    assertNull(invalid.getRole());
  }

  @Test
  public void testPasswordColumnIgnored() throws Exception {
    List<UserImportRow> rows = validator.parse(csv(
        "firstname,lastname,email,role,password\n"
            + "John,Doe,john@example.org,User,secret\n"), userManager);

    assertTrue(rows.get(0).isValid());
    assertNull(rows.get(0).getPassword());
  }

  @Test
  public void testMissingColumns() {
    UserImportException e = assertThrows(UserImportException.class,
        () -> validator.parse(csv("firstname,email\nJohn,john@example.org\n"), userManager));
    assertEquals("admin.users.import.error.columns", e.getKey());
    assertEquals("lastname", e.getArgs()[0]);
  }

  @Test
  public void testEmptyFile() {
    assertThrows(UserImportException.class, () -> validator.parse(csv(""), userManager));
    UserImportException e = assertThrows(UserImportException.class,
        () -> validator.parse(csv("firstname,lastname,email\n\n"), userManager));
    assertEquals("admin.users.import.error.empty", e.getKey());
  }

  @Test
  public void testNotUtf8() throws Exception {
    // invalid UTF-8 only after the header, as in a file saved by a spreadsheet application in Windows-1252
    Path file = Files.createTempFile(tempDir, "users", ".csv");
    Files.writeString(file, "firstname,lastname,email\nJosé,García,jose@example.org\n", Charset.forName("windows-1252"));

    UserImportException e = assertThrows(UserImportException.class, () -> validator.parse(file.toFile(), userManager));
    assertEquals("admin.users.import.error.encoding", e.getKey());
  }

  @Test
  public void testTooManyRows() throws Exception {
    StringBuilder sb = new StringBuilder("firstname,lastname,email\n");
    for (int i = 0; i <= UserImportValidator.MAX_ROWS; i++) {
      sb.append("John,Doe,john").append(i).append("@example.org\n");
    }
    File file = csv(sb.toString());

    UserImportException e = assertThrows(UserImportException.class, () -> validator.parse(file, userManager));
    assertEquals("admin.users.import.error.tooManyRows", e.getKey());
  }

  @Test
  public void testParseRole() {
    assertEquals(Role.User, UserImportValidator.parseRole(null));
    assertEquals(Role.User, UserImportValidator.parseRole(" "));
    assertEquals(Role.Publisher, UserImportValidator.parseRole(" PUBLISHER "));
    assertNull(UserImportValidator.parseRole("Root"));
  }
}
