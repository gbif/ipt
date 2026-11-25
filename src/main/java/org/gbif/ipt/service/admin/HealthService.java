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
package org.gbif.ipt.service.admin;

import org.gbif.ipt.config.DataDir;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * The IPT's health checks: GBIF services and public access (remote, cached and run in the background about twice a
 * day), and the data directory (local, cheap).
 */
public interface HealthService {

  /**
   * How the health icon in the menu looks to admins.
   */
  enum Level {
    // nothing to show
    OK,
    // the IPT works but something needs attention: a GBIF service, public access, space for only one more publication
    WARN,
    // the IPT can't work properly: a directory it can't write, no space to publish the largest resource
    BAD;

    public String css() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  /**
   * GBIF services and the IPT's public URL, as seen from the IPT.
   *
   * @param publicAccessChecked false when the validator itself failed: nothing is known about public access then
   * @param checked             when, in epoch milliseconds
   */
  record RemoteChecks(
      String registryUrl,
      boolean registry,
      String repositoryUrl,
      boolean repository,
      String publicAccessUrl,
      boolean publicAccessChecked,
      boolean publicAccess,
      String publicAccessStatus,
      String publicAccessMessage,
      long checked) {
  }

  /**
   * The data directory: free space and permissions.
   *
   * @param publishingNeed  estimated space publishing the largest resource needs, in bytes
   * @param largestResource short name of that resource; null without resources
   * @param subResources    the worst status of the resource folders
   */
  record LocalChecks(
      long diskTotal,
      long diskFree,
      String diskLevel,
      long publishingNeed,
      @Nullable String largestResource,
      DataDir.DirStatus config,
      DataDir.DirStatus log,
      DataDir.DirStatus tmp,
      DataDir.DirStatus resources,
      DataDir.DirStatus subResources) {
  }

  /**
   * Runs the remote checks now, unless they ran only a few minutes ago and force is false.
   *
   * @param force true to run them whatever their age (an admin asking to check again)
   *
   * @return the remote checks, or null before the IPT is set up
   */
  @Nullable
  RemoteChecks refreshRemoteChecks(boolean force);

  /**
   * @return the local checks, run now
   */
  LocalChecks checkLocal();

  /**
   * @return the level for the menu icon, from the last remote checks and recent local checks; no network calls
   */
  Level getLevel();
}
