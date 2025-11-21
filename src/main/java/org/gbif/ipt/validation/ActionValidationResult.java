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

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;

@Getter
public class ActionValidationResult {

  private final List<String> fieldErrors = new ArrayList<>();
  private final List<String> actionErrors = new ArrayList<>();
  private final List<String> warnings = new ArrayList<>();

  public void addFieldError(String field, String msg) {
    fieldErrors.add(field + ": " + msg);
  }

  public void addActionError(String msg) {
    actionErrors.add(msg);
  }

  public void addWarning(String msg) {
    warnings.add(msg);
  }

  public boolean hasFieldErrors() {
    return !fieldErrors.isEmpty();
  }

  public boolean hasActionErrors() {
    return !actionErrors.isEmpty();
  }

  public boolean hasErrors() {
    return !fieldErrors.isEmpty() || !actionErrors.isEmpty();
  }
}
