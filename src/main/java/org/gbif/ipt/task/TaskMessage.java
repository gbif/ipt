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

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Level;

/**
 * A message used in reporting a task's process, which has a level of severity, timestamp, and message.
 */
public class TaskMessage {

  public final Level level;
  public final long timestamp;
  public final String message;

  public TaskMessage(Level level, String message) {
    this.level = level;
    this.message = message;
    this.timestamp = new Date().getTime();
  }

  public Date getDate() {
    return new Date(timestamp);
  }

  @NotNull
  public Level getLevel() {
    return level;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  @NotNull
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return message;
  }
}
