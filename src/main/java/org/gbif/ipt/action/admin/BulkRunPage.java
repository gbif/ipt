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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;

/**
 * One page of a bulk publication's results, optionally only the resources in one state. Only this page is rendered,
 * so a run over hundreds of resources stays small when the page polls it.
 */
@Getter
public class BulkRunPage {

  public static final int SIZE = 50;

  private final List<BulkPublication.Item> items;
  // resources matching the filter, on all pages
  private final int matched;
  private final int page;
  private final int size;
  // the state shown, empty for all
  private final String filter;
  // when the run ended, read before the sort: a run finishing meanwhile shows as running and is polled once more
  private final Date ended;

  private BulkRunPage(List<BulkPublication.Item> items, int matched, int page, int size, String filter, Date ended) {
    this.items = items;
    this.matched = matched;
    this.page = page;
    this.size = size;
    this.filter = filter;
    this.ended = ended;
  }

  public boolean isFinished() {
    return ended != null;
  }

  /**
   * @param run    the run, or null
   * @param filter a {@link BulkPublication.State} name, or empty or unknown for all
   * @param page   1-based page; out of range pages show the last one
   * @param all    every matching resource on one page (for the copied report)
   */
  public static BulkRunPage of(BulkPublication run, String filter, int page, boolean all) {
    Date ended = run != null ? run.getEnded() : null;
    BulkPublication.State state = null;
    if (filter != null && !filter.isEmpty()) {
      try {
        state = BulkPublication.State.valueOf(filter);
      } catch (IllegalArgumentException e) {
        // unknown: show all
      }
    }
    List<BulkPublication.Item> sorted = run != null ? run.sorted(state) : Collections.emptyList();
    int size = all ? Math.max(1, sorted.size()) : SIZE;
    int pages = Math.max(1, (sorted.size() + size - 1) / size);
    int p = Math.min(Math.max(1, page), pages);
    List<BulkPublication.Item> items = sorted.subList((p - 1) * size, Math.min(sorted.size(), p * size));
    return new BulkRunPage(items, sorted.size(), p, size, state != null ? state.name() : "", ended);
  }
}
