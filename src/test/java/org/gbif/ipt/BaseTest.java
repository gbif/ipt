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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.nio.file.Files;

public class BaseTest {

  @BeforeAll
  static void setupLogging() {
    LoggingConfiguration.logDirectory = System.getProperty("java.io.tmpdir") + "/testlogs/";
    // Make sure the directory exists
    new File(LoggingConfiguration.logDirectory).mkdirs();
  }

  @BeforeEach
  void logTestInfo(TestInfo testInfo) {
    System.out.printf("Running: %s%n", testInfo.getDisplayName());
    System.out.printf("CWD: %s%n", new File(".").getAbsolutePath());
  }

  @AfterEach
  void checkForStrayDirs(TestInfo testInfo) throws Exception {
    String[] junkDirs = { "res1", "amphibians" };

    for (String name : junkDirs) {
      File f = new File(name);
      if (f.exists()) {
        System.err.printf("⚠️  Test '%s' created junk directory: '%s'%n",
            testInfo.getDisplayName(), name);

        // Delete it to keep the environment clean
        deleteRecursively(f);

        throw new AssertionError("Stray directory found: " + name);
      }
    }
  }

  private void deleteRecursively(File file) throws Exception {
    if (file.isDirectory()) {
      for (File sub : file.listFiles()) {
        deleteRecursively(sub);
      }
    }
    Files.deleteIfExists(file.toPath());
  }
}
