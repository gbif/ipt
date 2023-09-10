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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class UserValidatorTest {
  private static UserValidator USER_VALIDATOR = new UserValidator();
  private static BaseAction BASE_ACTION = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(
    RegistrationManager.class));

  @DisplayName("Test email validator")
  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("emails")
  public void testEmail(String email, String reason, boolean valid) {
    ValidationResult result = USER_VALIDATOR.checkEmailValid(email);
    assertEquals(valid, result.isValid());
    assertEquals(reason, result.getMessage());
  }

  @Test
  public void testUser() {
    // good user
    User user = new User();
    user.setEmail("jcuadra@gbif.org");
    user.setFirstname("Jose");
    user.setLastname("Cuadra");
    user.setPassword("password");
    user.setRole(User.Role.Admin);
    user.setLastLoginToNow();
    assertTrue(USER_VALIDATOR.validate(BASE_ACTION, user));

    // bad user - bad email
    user.setEmail("jcuadra @gbif.org");
    assertFalse(USER_VALIDATOR.validate(BASE_ACTION, user));
    // bad user - bad first name
    user.setEmail("jcuadra@gbif.org");
    user.setFirstname(" ");
    assertFalse(USER_VALIDATOR.validate(BASE_ACTION, user));
    // bad user - bad last name
    user.setFirstname("Jose");
    user.setLastname(" ");
    assertFalse(USER_VALIDATOR.validate(BASE_ACTION, user));
    // bad user - bad password
    user.setLastname("Cuadra");
    user.setPassword("p");
    assertFalse(USER_VALIDATOR.validate(BASE_ACTION, user));
  }

  public static Stream<Arguments> emails() throws Exception {
    return Stream.of(
      Arguments.of(Named.of("Email [kbraak@gbif.org] is valid", "kbraak@gbif.org"), null, true),
      Arguments.of(Named.of("Email [kyle.braak@gmail.com] is valid", "kyle.braak@gmail.com"), null, true),
      Arguments.of(Named.of("Email [JohnDoe@example.com] is valid (varying case)", "JohnDoe@example.com"), null, true),
      Arguments.of(Named.of("Email [johndoe@example.com] is valid (varying case)", "johndoe@example.com"), null, true),
      Arguments.of(Named.of("Email [katia@gov.cat] is valid (.cat extension)", "katia@gov.cat"), null, true),
      Arguments.of(Named.of("Email null is invalid (null)", null), null, false),
      Arguments.of(Named.of("Email [] is invalid (empty)", ""), "Illegal address", false),
      Arguments.of(Named.of("Email [JohnDoe] is invalid (no domain)", "JohnDoe"), "Missing final '@domain'", false),
      Arguments.of(Named.of("Email [johndoe@] is invalid (no domain)", "johndoe@"), "Missing domain", false),
      Arguments.of(Named.of("Email [johndoe@.] is invalid (domain starts with dot)", "johndoe@."), "Domain starts with dot", false),
      Arguments.of(Named.of("Email [johndoe@d oe] is invalid (contains space)", "johndoe@d oe"), "Domain contains control or whitespace", false),
      Arguments.of(Named.of("Email [johndoe@\u200Bdoe.com] is invalid (contains zero width space char)", "johndoe@\u200Bdoe.com"), "Domain contains control or whitespace", false),
      Arguments.of(Named.of("Email [johndoe@do%e.com] is invalid (contains illegal char)", "johndoe@do%e.com"), "Domain contains illegal character", false),
      Arguments.of(Named.of("Email [johndoe@.doe.com] is invalid (starts with dot)", "johndoe@.doe.com"), "Domain starts with dot", false),
      Arguments.of(Named.of("Email [johndoe@d..oe.com] is invalid (contains double dot)", "johndoe@d..oe.com"), "Domain contains dot-dot", false),
      Arguments.of(Named.of("Email [johndoe@doe.] is invalid (ends with dot)", "johndoe@doe."), "Domain ends with dot", false),
      Arguments.of(Named.of("Email [@doe] is invalid (no local name)", "@doe"), "Missing local name", false),
      Arguments.of(Named.of("Email [john doe@doe.com] is invalid (contains space)", "john doe@doe.com"), "Local address contains control or whitespace", false),
      Arguments.of(Named.of("Email [john\u200Bdoe@doe.com] is invalid (contains zero width space char)", "john\u200Bdoe@doe.com"), "Local address contains control or whitespace", false)
    );
  }
}
