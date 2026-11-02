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
package org.gbif.ipt.action.manage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoPublishActionTest {

  /**
   * Multi-label domains such as mail.huji.ac.il used to be rejected (#3263).
   */
  @ParameterizedTest
  @ValueSource(strings = {
      "biogis@mail.huji.ac.il",
      "nnhc@mail.huji.ac.il",
      "someone@example.co.uk",
      "first.last+ipt@dept.uni.example.org",
      "admin@example.org"
  })
  public void testValidEmailsAccepted(String email) {
    assertTrue(AutoPublishAction.findInvalidEmails(Collections.singletonList(email)).isEmpty(),
        email + " should be accepted");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "not-an-email",
      "a@b",
      "a@@example.org",
      "@example.org",
      "someone@",
      "some one@example.org"
  })
  public void testInvalidEmailsRejected(String email) {
    assertEquals(Collections.singletonList(email),
        AutoPublishAction.findInvalidEmails(Collections.singletonList(email)));
  }

  @Test
  public void testFindInvalidEmailsReturnsOnlyInvalidOnesInOrder() {
    List<String> emails = Arrays.asList("ok@mail.huji.ac.il", "bad", "ok@example.org", "a@b");
    assertEquals(Arrays.asList("bad", "a@b"), AutoPublishAction.findInvalidEmails(emails));
  }

  @Test
  public void testParseFailureEmailsTrimsDeduplicatesAndDropsBlanks() {
    String raw = " biogis@mail.huji.ac.il , nnhc@mail.huji.ac.il,, ,biogis@mail.huji.ac.il ";
    assertEquals(Arrays.asList("biogis@mail.huji.ac.il", "nnhc@mail.huji.ac.il"),
        AutoPublishAction.parseFailureEmails(raw));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", " , ,"})
  public void testParseFailureEmailsBlankInput(String raw) {
    assertTrue(AutoPublishAction.parseFailureEmails(raw).isEmpty());
  }
}
