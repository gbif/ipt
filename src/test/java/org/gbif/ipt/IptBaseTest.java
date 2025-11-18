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
package org.gbif.ipt;

import org.gbif.ipt.config.LoggingConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IptBaseTest {

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @BeforeAll
  static void setupLogging() {
    LoggingConfiguration.logDirectory = System.getProperty("java.io.tmpdir") + "/testlogs/";
    // Make sure the directory exists
    new File(LoggingConfiguration.logDirectory).mkdirs();
  }

  @AfterEach
  void checkForStrayDirsAndFiles(TestInfo testInfo) throws Exception {
    String[] expectedJunk = { "event", "res1", "amphibians", "${test.datadir}", "admin.log", "debug.log" };
    boolean junkFound = false;
    List<String> junk = new ArrayList<>();

    for (String name : expectedJunk) {
      File f = new File(name);
      if (f.exists()) {
        System.err.printf("Test '%s' created junk " + (f.isDirectory() ? "directory" : "file") + ": '%s'%n",
            testInfo.getDisplayName(), name);

        // Delete it to keep the environment clean
        deleteRecursively(f);

        junk.add(name);
        junkFound = true;
      }
    }

    if (junkFound) {
      throw new AssertionError("Stray directories or files found: " + junk);
    }
  }

  private void deleteRecursively(File file) throws Exception {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File sub : files) {
          deleteRecursively(sub);
        }
      }
    }
    Files.deleteIfExists(file.toPath());
  }
}
