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
package org.gbif.ipt.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class DataDirPermissionsTest {

  @TempDir
  Path resources;

  private final DataDir dataDir = DataDir.buildFromString(null);
  private File readOnlyFile;

  @BeforeEach
  public void setUp() throws IOException {
    Files.createDirectory(resources.resolve("b-resource"));
    Files.createDirectory(resources.resolve("a-resource"));
    readOnlyFile = Files.createFile(resources.resolve("stray.txt")).toFile();
    readOnlyFile.setWritable(false);
    // root can write anything, so read-only files and folders can't be made there
    assumeFalse(readOnlyFile.canWrite(), "file permissions not enforced (running as root?)");
  }

  @AfterEach
  public void tearDown() {
    // let @TempDir clean up
    for (File f : resources.toFile().listFiles()) {
      f.setWritable(true);
    }
  }

  @Test
  public void testStrayFilesDontCount() {
    assertEquals(DataDir.DirStatus.READ_WRITE, dataDir.getSubDirectoriesReadWriteStatus(resources.toFile()));
    assertEquals(List.of(), dataDir.getSubDirectoriesNotReadWrite(resources.toFile()));
  }

  @Test
  public void testReadOnlyFoldersAreListedSorted() {
    resources.resolve("b-resource").toFile().setWritable(false);
    resources.resolve("a-resource").toFile().setWritable(false);
    assertEquals(DataDir.DirStatus.READ_ONLY, dataDir.getSubDirectoriesReadWriteStatus(resources.toFile()));
    assertEquals(List.of("a-resource", "b-resource"), dataDir.getSubDirectoriesNotReadWrite(resources.toFile()));
  }

  @Test
  public void testMissingFolder() {
    File missing = resources.resolve("missing").toFile();
    assertEquals(DataDir.DirStatus.NOT_EXIST, dataDir.getSubDirectoriesReadWriteStatus(missing));
    assertEquals(List.of(), dataDir.getSubDirectoriesNotReadWrite(missing));
  }
}
