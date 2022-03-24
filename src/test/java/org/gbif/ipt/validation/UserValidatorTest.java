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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class UserValidatorTest {
  private static UserValidator USER_VALIDATOR = new UserValidator();
  private static BaseAction BASE_ACTION = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(
    RegistrationManager.class));

  @Test
  public void testEmail() {
    // good emails
    assertTrue(USER_VALIDATOR.isValidEmail("kbraak@gbif.org"));
    assertTrue(USER_VALIDATOR.isValidEmail("kyle.braak@gmail.com"));
    // emails in varying case? see https://code.google.com/p/gbif-providertoolkit/issues/detail?id=1013
    assertTrue(USER_VALIDATOR.isValidEmail("JohnDoe@example.com"));
    assertTrue(USER_VALIDATOR.isValidEmail("johndoe@example.com"));
    // emails with .cat extension, see see https://code.google.com/p/gbif-providertoolkit/issues/detail?id=1010
    assertTrue(USER_VALIDATOR.isValidEmail("katia@gov.cat"));

    // bad emails
    assertFalse(USER_VALIDATOR.isValidEmail(null));
    assertFalse(USER_VALIDATOR.isValidEmail(""));
    assertFalse(USER_VALIDATOR.isValidEmail("JohnDoe"));
    assertFalse(USER_VALIDATOR.isValidEmail("John Doe@example.com"));
    assertFalse(USER_VALIDATOR.isValidEmail("JohnDoe.com"));
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
}
