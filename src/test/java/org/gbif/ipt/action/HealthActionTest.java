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
package org.gbif.ipt.action;

import org.gbif.utils.ExtendedResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthActionTest {

  private static final long GB = FileUtils.ONE_GB;

  @Test
  public void testDiskLevelUsesFreeSpaceNotShareOfDisk() {
    // 300 GB free on a 2 TB disk (85% used) is plenty
    assertEquals("ok", HealthAction.diskLevel(300 * GB));
    // 6 GB free on a 20 GB disk (70% used) is running low
    assertEquals("warn", HealthAction.diskLevel(6 * GB));
    assertEquals("bad", HealthAction.diskLevel(GB));
    assertEquals("bad", HealthAction.diskLevel(0));
  }

  @Test
  public void testDiskLevelThresholds() {
    assertEquals("ok", HealthAction.diskLevel(HealthAction.DISK_LOW_BYTES));
    assertEquals("warn", HealthAction.diskLevel(HealthAction.DISK_LOW_BYTES - 1));
    assertEquals("warn", HealthAction.diskLevel(HealthAction.DISK_FULL_BYTES));
    assertEquals("bad", HealthAction.diskLevel(HealthAction.DISK_FULL_BYTES - 1));
  }

  private static final String URL = "https://ipt.example.org/ipt";

  private static ExtendedResponse response(int statusCode, String content) {
    HttpResponse httpResponse = mock(HttpResponse.class);
    when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, ""));
    ExtendedResponse resp = new ExtendedResponse(httpResponse);
    resp.setContent(content);
    return resp;
  }

  @Test
  public void testPublicAccessReachable() {
    HealthAction.PublicAccessCheck check = HealthAction.readPublicAccessResponse(
        response(200, "{\"success\":true,\"status\":200,\"url\":\"" + URL + "\"}"), URL);
    assertTrue(check.checked());
    assertTrue(check.reachable());
  }

  @Test
  public void testPublicAccessNotReachable() {
    // the validator ran but couldn't open the IPT
    HealthAction.PublicAccessCheck check = HealthAction.readPublicAccessResponse(
        response(200, "{\"success\":false,\"error\":\"IOException\",\"url\":\"" + URL + "\"}"), URL);
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertEquals("IOException", check.message());

    // the IPT answered the validator with an error status
    check = HealthAction.readPublicAccessResponse(response(200, "{\"success\":false,\"status\":503}"), URL);
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertEquals("503", check.status());

    // validurl-ws refuses localhost and private network addresses
    check = HealthAction.readPublicAccessResponse(
        response(200, "{\"success\":false,\"error\":\"NonPublicAddress\",\"url\":\"http://localhost:8080\"}"),
        "http://localhost:8080");
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertTrue(check.message().contains("private network"));
  }

  @Test
  public void testPublicAccessNotCheckedWhenValidatorFails() {
    HealthAction.PublicAccessCheck check = HealthAction.readPublicAccessResponse(null, URL);
    assertFalse(check.checked());
    assertEquals("NO_RESPONSE", check.status());

    check = HealthAction.readPublicAccessResponse(response(502, "Bad Gateway"), URL);
    assertFalse(check.checked());
    assertEquals("502", check.status());

    check = HealthAction.readPublicAccessResponse(response(200, "<html>maintenance</html>"), URL);
    assertFalse(check.checked());
    assertFalse(check.reachable());
    assertEquals("BAD_RESPONSE", check.status());
  }
}
