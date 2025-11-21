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

public class ActionErrorCollector implements ErrorCollector {

  private final BaseAction action;

  public ActionErrorCollector(BaseAction action) {
    this.action = action;
  }

  @Override
  public void addFieldError(String field, String msg) {
    action.addFieldError(field, msg);
  }

  @Override
  public void addActionError(String msg) {
    action.addActionError(msg);
  }

  @Override
  public void addActionWarning(String msg) {
    action.addActionWarning(msg);
  }

  @Override
  public boolean hasActionErrors() {
    return action.hasActionErrors();
  }

  @Override
  public boolean hasFieldErrors() {
    return action.hasFieldErrors();
  }

  @Override
  public boolean hasErrors() {
    return hasActionErrors() || hasFieldErrors();
  }
}
