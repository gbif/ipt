/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.config;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IPTContextListenerTest {
  Pattern regex = Pattern.compile(IPTContextListener.ALL_BUT_AUTHENTICATED);

  /**
   * Make sure the regex used to apply the xss filter only fires on all urls not having manage or admin in the path.
   */
  @Test
  public void testRegex() {
    assertTrue(matches(""));
    assertTrue(matches("/"));
    assertTrue(matches("/aha"));
    assertTrue(matches("/manage"));
    assertTrue(matches("/my-manager/678"));
    assertTrue(matches("/admin"));
    assertTrue(matches("/administrator/67483"));
    assertTrue(matches("/fhdjs/administrator/67483?fd"));

    assertFalse(matches("/manage/resource.do"));
    assertFalse(matches("/admin/resource.do"));
    assertFalse(matches("/root/manage/resource.do"));
  }

  private boolean matches(String path) {
    return regex.matcher(path).matches();
  }
}
