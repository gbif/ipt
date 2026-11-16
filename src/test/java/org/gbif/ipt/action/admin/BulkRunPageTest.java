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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.model.BulkPublication.Item;
import org.gbif.ipt.model.BulkPublication.State;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BulkRunPageTest {

  /**
   * 120 published resources and 3 failed ones: failures first, 50 a page.
   */
  private BulkPublication run() {
    List<Item> items = new ArrayList<>();
    for (int i = 0; i < 123; i++) {
      Item item = new Item("r" + i, "Resource " + i, "dwca", PublicationStatus.PUBLIC, "1.0", "1.1", 10);
      item.setState(i % 41 == 40 ? State.FAILED : State.PUBLISHED);
      items.add(item);
    }
    return new BulkPublication("Admin", BulkPublication.Scope.ALL, items);
  }

  @Test
  public void pagesWorstFirst() {
    BulkRunPage first = BulkRunPage.of(run(), null, 1, false);
    assertEquals(123, first.getMatched());
    assertEquals(50, first.getItems().size());
    assertEquals(State.FAILED, first.getItems().get(0).getState());
    assertEquals("r40", first.getItems().get(0).getShortname());
    assertEquals(State.PUBLISHED, first.getItems().get(3).getState());

    BulkRunPage last = BulkRunPage.of(run(), "", 3, false);
    assertEquals(23, last.getItems().size());
    // beyond the last page: the last page
    assertEquals(3, BulkRunPage.of(run(), null, 9, false).getPage());
    assertEquals(1, BulkRunPage.of(run(), null, -1, false).getPage());
  }

  @Test
  public void filtersByState() {
    BulkRunPage failed = BulkRunPage.of(run(), "FAILED", 1, false);
    assertEquals(3, failed.getMatched());
    assertEquals("FAILED", failed.getFilter());
    // unknown filters show everything
    assertEquals(123, BulkRunPage.of(run(), "<script>", 1, false).getMatched());
    assertEquals("", BulkRunPage.of(run(), "<script>", 1, false).getFilter());
  }

  @Test
  public void allOnOnePage() {
    assertEquals(123, BulkRunPage.of(run(), null, 1, true).getItems().size());
    assertEquals(0, BulkRunPage.of(null, null, 1, false).getMatched());
  }
}
