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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for class {@link URLUtils}.
 */
public class URLUtilsTest {

  @Test
  public void isLocalhostTest() throws MalformedURLException {
    assertTrue(URLUtils.isLocalhost(new URL("http://localhost:8080")));
    assertTrue(URLUtils.isLocalhost(new URL("http://127.0.0.1/test/tests")));
  }

  @Test
  public void getHostNameTest() {
    assertNotNull(URLUtils.getHostName());
  }

  @Test
  public void hasPortTest() {
    assertTrue(URLUtils.hasPort("http://localhost:8080"));
    assertTrue(URLUtils.hasPort("http://gbif.nothing.org:9932"));
    assertFalse(URLUtils.hasPort("http://gbif.nothing.org/eee/oo"));
  }


  @Test
  public void isURLValidTest() {
    assertTrue(URLUtils.isURLValid("http://localhost:8080"));
    assertTrue(URLUtils.isURLValid("https://gbif.nothing.com:9932"));
    assertTrue(URLUtils.isURLValid("http://gbif.nothing.org/eee/oo"));
    assertFalse(URLUtils.isURLValid("ftp://gbif.nothing.org/eee/oo"));
    assertFalse(URLUtils.isURLValid("file://gbif.nothing.org/eee/oo"));
    assertFalse(URLUtils.isURLValid("//gbif.nothing.org/eee/oo"));
    assertFalse(URLUtils.isURLValid("nothing.com/eee/oo"));
  }

  @Test
  public void testIsHostName() throws MalformedURLException {
    // try to save a baseURL that uses the machine name
    String machineName = URLUtils.getHostName();
    URL baseURL = new URL("http://" + machineName + "/ipt");
    assertTrue(URLUtils.isHostName(baseURL));
    // try to save a baseURL that doesn't use the machine name
    assertFalse(URLUtils.isHostName(new URL("http://www.ipt.test.org")));
  }

}
