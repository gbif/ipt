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

public interface ErrorCollector {

  /**
   * Add a field error.
   *
   * @param field field
   * @param msg message
   */
  void addFieldError(String field, String msg);

  /**
   * Add an action error
   * @param msg message
   */
  void addActionError(String msg);

  /**
   * Add a warning.
   * @param msg message
   */
  void addActionWarning(String msg);

  /**
   * True if there are action errors present.
   *
   * @return true/false
   */
  boolean hasActionErrors();

  /**
   * True if there are field errors present.
   *
   * @return true/false
   */
  boolean hasFieldErrors();

  /**
   * True if there are action and/or field errors.
   *
   * @return true/false
   */
  boolean hasErrors();
}
