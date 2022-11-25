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
package org.gbif.ipt.utils;

import javax.validation.constraints.NotNull;

public class StringUtils {

  /**
   * Function to convert camel case string to snake case string.
   *
   * @param str input string in camel case
   * @return result string in snake case
   */
  public static String camelToSnake(@NotNull String str) {
    StringBuilder result = new StringBuilder();

    // Append first character (in lower case) to result string
    char c = str.charAt(0);
    result.append(Character.toLowerCase(c));

    // Traverse the string from
    for (int i = 1; i < str.length(); i++) {
      char ch = str.charAt(i);

      // Check if the character is upper case then append '_' and such character (in lower case) to result string
      if (Character.isUpperCase(ch)) {
        result.append('_');
        result.append(Character.toLowerCase(ch));
      }
      // If the character is lower case then add such character into result string
      else {
        result.append(ch);
      }
    }

    return result.toString();
  }
}
