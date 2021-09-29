/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.portal;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the regex in the resource page.
 */

public class RegexResourceTest {

  // It Matches for schemes http, https and ftp.
  private static final String REGEX = "(http(s)?|ftp)://(([\\w-]+\\.)?)+[\\w-]+(:\\d+)?+(/[\\w- ./-?%&=]*)?";

  @Test
  public void regexTest() {
    assertTrue(Pattern.matches(REGEX, "http://www.example.com"));
    assertTrue(Pattern.matches(REGEX, "http://www.example.org"));
    assertTrue(Pattern.matches(REGEX, "https://example.com"));
    assertTrue(Pattern.matches(REGEX, "ftp://example.com"));
    assertTrue(Pattern.matches(REGEX, "http://www.example.com:8080"));
    assertTrue(Pattern.matches(REGEX, "http://www.example.com:8080/html.com"));
    assertTrue(Pattern.matches(REGEX, "http://example.com/example/example%&=."));
    assertTrue(Pattern.matches(REGEX, "http://www.example.com/link?id=100408"));
    assertTrue(Pattern.matches(REGEX, "http://www.example.com.co"));
    assertTrue(Pattern.matches(REGEX, "http://my-sever"));

    assertFalse(Pattern.matches(REGEX, "www.example.org"));
    assertFalse(Pattern.matches(REGEX, "example"));
    assertFalse(Pattern.matches(REGEX, "/123456"));
    assertFalse(Pattern.matches(REGEX, "http://"));
    assertFalse(Pattern.matches(REGEX, ""));
    assertFalse(Pattern.matches(REGEX, " "));
    assertFalse(Pattern.matches(REGEX, "htp://www.example.com"));
    assertFalse(Pattern.matches(REGEX, "http:example.com"));
    assertFalse(Pattern.matches(REGEX, "http:/www.example.com/"));
    assertFalse(Pattern.matches(REGEX, "http//example.com"));
    assertFalse(Pattern.matches(REGEX, "://www.example.gov.in"));
    assertFalse(Pattern.matches(REGEX, "//www.example.gov.in"));
    assertFalse(Pattern.matches(REGEX, "words http://www.example.gov words"));
  }
}
