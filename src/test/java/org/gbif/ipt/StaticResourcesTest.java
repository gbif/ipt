/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class StaticResourcesTest {

  /**
   * Tomcat converts static resources from the system encoding to UTF-8.  The system
   * encoding on Linux is usually UTF-8, but not so on Windows, where we may have
   * corrupt files.
   * <p>
   * This can be overridden with a fileEncoding parameter in the server's web.xml, but
   * it is easier for deployment if we either
   * <ul>
   *   <li>use UTF-8 BOMs, which will prevent Tomcat from corrupting the file
   *   <li>escape all non-ASCII static resource files
   * </ul>
   * <p>
   * This test checks that all static resources take one of these options.
   * <p>
   * One way to add a BOM to a file is <code>sed -i '1s/^/\xef\xbb\xbf/' file</code>.
   */
  @Test
  public void checkStaticResourcesEncodingTest() throws Exception {
    String[] ignoreExtensions = new String[] {
      ".eot", ".otf", ".ttf", ".woff", ".woff2", // Fonts
      ".gif", ".png", ".svg", // Images
      ".ftl" // Not served as static resources by Tomcat
    };

    Files.walk(Paths.get("src/main/webapp"))
     .filter(p -> p.toFile().isFile()
       && Arrays.stream(ignoreExtensions).noneMatch(x -> p.getFileName().toString().endsWith(x)))
     .forEach(
       p -> {
         try {
           if (!isAsciiOrHasBOM(p)) {
             fail("File "+p+" is not ASCII, and does not have a byte order mark.");
           }
         } catch (IOException e) {
           fail("Exception when checking encoding of file " + p + ".");
         }
       }
     );
  }

  private boolean isAsciiOrHasBOM(Path p) throws IOException {
    // Check for byte order mark
    byte[] bom = new byte[3];
    try (InputStream is = new FileInputStream(p.toFile())) {
      is.read(bom);

      if (bom[0] == (0xEF-256) && bom[1] == (0xBB-256) && bom[2] == (0xBF-256)) {
        return true;
      }

      // Check for non-ASCII bytes in the first three
      if (bom[0] < 0 || bom[1] < 0 || bom[2] < 0) {
        return false;
      }

      // Check for non-ASCII bytes in the remainder of the file
      int c;
      while ((c = is.read()) > 0) {
        if (c > 0x7F) {
          return false;
        }
      }
      return true;
    }
  }
}
