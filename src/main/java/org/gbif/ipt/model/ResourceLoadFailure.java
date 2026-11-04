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

import java.util.Date;

/**
 * A resource folder in the data directory that couldn't be loaded, e.g. because of a corrupt resource.xml or a
 * missing extension. Kept in memory only, the next load starts over.
 */
public class ResourceLoadFailure {

  private final String shortname;
  private final String reason;
  private final Date failedAt;

  public ResourceLoadFailure(String shortname, String reason, Date failedAt) {
    this.shortname = shortname;
    this.reason = reason;
    this.failedAt = failedAt;
  }

  /**
   * @return the name of the resource folder, the shortname of the resource
   */
  public String getShortname() {
    return shortname;
  }

  /**
   * @return why the resource couldn't be loaded, in English: it comes from the exception, see the logs for details
   */
  public String getReason() {
    return reason;
  }

  public Date getFailedAt() {
    return failedAt;
  }
}
