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
package org.gbif.ipt.action.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The installation description shown on the home page: {@link HomeAction#descriptionLead} and
 * {@link HomeAction#descriptionFull}.
 */
class HomeActionTest {

  @Test
  void descriptionLeadIsTheFirstLine() {
    assertEquals("Hosted by GBIF.", HomeAction.descriptionLead("Hosted by GBIF.\r\nContacts: https://example.org"));
  }

  @Test
  void descriptionLeadDropsMarkupAndSkipsLinesWithoutText() {
    assertEquals("Hosted by GBIF & partners.", HomeAction.descriptionLead("<p>\n  <b>Hosted</b> by GBIF &amp; partners.</p>"));
  }

  @Test
  void descriptionLeadOfBlankDescriptionIsNull() {
    assertNull(HomeAction.descriptionLead(null));
    assertNull(HomeAction.descriptionLead("  \n "));
    assertNull(HomeAction.descriptionLead("<p></p>"));
  }

  @Test
  void descriptionFullIsNullWhenTheFirstLineSaysItAll() {
    assertNull(HomeAction.descriptionFull(null));
    assertNull(HomeAction.descriptionFull("Hosted by GBIF."));
    assertNull(HomeAction.descriptionFull("<p><b>Hosted</b> by GBIF.</p>\n"));
  }

  @Test
  void descriptionFullKeepsLineBreaksOfPlainText() {
    assertEquals("Hosted by GBIF.<br>Contacts: https://example.org",
        oneLine(HomeAction.descriptionFull("Hosted by GBIF.\r\nContacts: https://example.org\n")));
  }

  @Test
  void descriptionFullKeepsSafeMarkupOnly() {
    String full = HomeAction.descriptionFull("<p>Hosted by GBIF.</p>\n<p onclick=\"x()\">More <a href=\"https://gbif.org\">here</a><script>x()</script></p>");
    assertEquals("<p>Hosted by GBIF.</p><p>More <a href=\"https://gbif.org\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">here</a></p>", oneLine(full));
  }

  /** Jsoup's output is pretty-printed; the line breaks between tags don't matter in HTML. */
  private static String oneLine(String html) {
    return html.replaceAll("\\s*\\R\\s*", "");
  }
}
