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
package org.gbif.ipt.model;

import org.gbif.ipt.model.User.Role;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A single row of a user import (CSV) file, together with its validation outcome.
 */
@Getter
@Setter
public class UserImportRow implements Serializable {

  @Serial
  private static final long serialVersionUID = 6427093512580391724L;

  // line number in the uploaded file, header being line 1; 0 for rows added in the preview
  private int line;
  private String firstname;
  private String lastname;
  private String email;
  // role as written in the file
  private String roleValue;
  private Role role;
  // i18n keys of the validation errors
  private List<String> errors = new ArrayList<>();
  // generated (plain text) password, only set once the account has been created
  private String password;
  private boolean created;

  public boolean isValid() {
    return errors.isEmpty();
  }

  public void addError(String key) {
    errors.add(key);
  }
}
