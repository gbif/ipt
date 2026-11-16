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
package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.task.TaskMessage;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * One bulk publication run: what it publishes and how far each resource has got. It's kept in memory until the next
 * run starts, so the results can be read after leaving the page.
 */
@Getter
public class BulkPublication {

  /**
   * Which resources a run publishes.
   */
  public enum Scope {
    ALL, CHANGED, SELECTED, EXCLUDED
  }

  /**
   * Where a resource is in the run.
   */
  public enum State {
    WAITING, RUNNING, PUBLISHED, NOT_CHANGED, RECORDS_DROPPED, FAILED, CANCELLED, SKIPPED;

    public boolean isFinished() {
      return this != WAITING && this != RUNNING;
    }
  }

  /**
   * Why a resource can't be published; these resources are skipped.
   */
  public enum NotReady {
    METADATA, MAPPINGS, LOCKED, DELETED
  }

  private final Date started = new Date();
  private final String startedBy;
  private final Scope scope;
  private final List<Item> items;
  @Setter
  private volatile Date ended;

  public BulkPublication(String startedBy, Scope scope, List<Item> items) {
    this.startedBy = startedBy;
    this.scope = scope;
    this.items = Collections.unmodifiableList(items);
  }

  public boolean isFinished() {
    return ended != null;
  }

  /**
   * @return the number of resources in this state
   */
  public int count(String state) {
    State s = State.valueOf(state);
    return (int) items.stream().filter(i -> i.getState() == s).count();
  }

  public int getFinishedCount() {
    return (int) items.stream().filter(i -> i.getState().isFinished()).count();
  }

  /**
   * The order results are listed in: running first, then the worst results, the successful ones last.
   */
  public static final List<State> ORDER = List.of(State.RUNNING, State.FAILED, State.RECORDS_DROPPED, State.CANCELLED,
      State.SKIPPED, State.WAITING, State.NOT_CHANGED, State.PUBLISHED);

  /**
   * @return the state names in {@link #ORDER}, for the page
   */
  public List<String> getOrder() {
    return ORDER.stream().map(Enum::name).toList();
  }

  /**
   * @param filter only resources in this state, or all if null
   *
   * @return the resources in {@link #ORDER}, in their run order within each state
   */
  public List<Item> sorted(State filter) {
    // states change while the run goes on: read each one once, so the sort sees a consistent order
    return items.stream()
        .map(i -> Map.entry(i, i.getState()))
        .filter(e -> filter == null || e.getValue() == filter)
        .sorted(Comparator.comparingInt(e -> ORDER.indexOf(e.getValue())))
        .map(Map.Entry::getKey)
        .toList();
  }

  /**
   * One resource in a run, with what it was when the run started (title, versions, visibility) and its progress.
   */
  @Getter
  @Setter
  public static class Item {

    private final String shortname;
    private final String title;
    // dwca, dwc-dp, camtrap-dp, coldp or metadata
    private final String format;
    private final PublicationStatus status;
    private final String previousVersion;
    private final String nextVersion;
    private final int recordsDropThreshold;
    private volatile State state = State.WAITING;
    private volatile NotReady notReady;
    // the failure, as the publication reported it
    private volatile String error;
    // the step and the human-readable state of a running publication
    private volatile String step;
    private volatile String stateText;
    private volatile List<TaskMessage> messages = Collections.emptyList();
    private volatile Long records;

    public Item(String shortname, String title, String format, PublicationStatus status, String previousVersion,
                String nextVersion, int recordsDropThreshold) {
      this.shortname = shortname;
      this.title = title;
      this.format = format;
      this.status = status;
      this.previousVersion = previousVersion;
      this.nextVersion = nextVersion;
      this.recordsDropThreshold = recordsDropThreshold;
    }
  }
}
