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
package org.gbif.ipt.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public class StatusReport {

  private final boolean completed;
  private final Throwable exception;
  private final long timestamp;
  private final String state; // human-readable state
  // step the task is at, or failed or was cancelled at (e.g., METADATA or VALIDATING), null if the task has no steps
  private final String step;
  // when each step started (epoch milliseconds), in the order the steps ran; empty for tasks without steps
  private final Map<String, Long> stepStarts;
  private final List<TaskMessage> messages;

  public StatusReport(boolean completed, String state, List<TaskMessage> messages) {
    this(completed, state, null, messages);
  }

  public StatusReport(boolean completed, String state, String step, List<TaskMessage> messages) {
    this(completed, null, state, step, messages);
  }

  public StatusReport(Throwable exception, String state, List<TaskMessage> messages) {
    this(exception, state, null, messages);
  }

  public StatusReport(Throwable exception, String state, String step, List<TaskMessage> messages) {
    this(true, exception, state, step, messages);
  }

  public StatusReport(String state, List<TaskMessage> messages) {
    this(false, state, messages);
  }

  private StatusReport(boolean completed, Throwable exception, String state, String step, List<TaskMessage> messages) {
    this(completed, exception, state, step, messages, Collections.emptyMap());
  }

  private StatusReport(boolean completed, Throwable exception, String state, String step, List<TaskMessage> messages,
                       Map<String, Long> stepStarts) {
    this.completed = completed;
    this.exception = exception;
    this.state = state;
    this.step = step;
    this.messages = messages;
    this.stepStarts = Collections.unmodifiableMap(new LinkedHashMap<>(stepStarts));
    this.timestamp = new Date().getTime();
  }

  /**
   * @return a copy of this report with these step start times, e.g. to keep them in the final report made after the task
   */
  public StatusReport withStepStarts(Map<String, Long> starts) {
    Map<String, Long> times = starts != null ? starts : Collections.emptyMap();
    return new StatusReport(completed, exception, state, step, messages, times);
  }

  /**
   * @return true if the task was cancelled (interrupted) rather than failed
   */
  public boolean isCancelled() {
    return exception instanceof InterruptedException;
  }

  public String getExceptionMessage() {
    return exception != null ? exception.getMessage() : null;
  }

  public List<String> getExceptionStacktrace() {
    List<String> trace = new ArrayList<>();
    if (exception != null) {
      for (StackTraceElement el : exception.getStackTrace()) {
        trace.add(el.toString());
      }
    }
    return trace;
  }

  public boolean hasException() {
    return exception != null;
  }

  @Override
  public String toString() {
    return state + ":" + StringUtils.join(messages, ";");
  }

}
