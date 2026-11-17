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
 * The installation description shown on the home page: {@link HomeAction#descriptionLead}.
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
}
