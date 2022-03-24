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
package peformance;

import org.gbif.utils.file.FileUtils;
import org.gbif.utils.text.LineComparator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.UUID;

/**
 * This is just a utility class to test performance of sorting files of IDs.
 */
public class SortTest {

  public static void main(String[] args) {
    try {
      File in = new File("/tmp/unsorted.txt");
      File out = new File("/tmp/sorted.txt");
      File out2 = new File("/tmp/sorted2.txt");

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(in), "UTF-8"));
      int numLines = 1000;
      System.out.println("Writing " + numLines + " random UUID lines to sort");
      for (int i = 0; i < numLines; i++) {
        writer.write(numLines - i + " " + UUID.randomUUID() + "\n");
      }
      writer.close();

      FileUtils utils = new FileUtils();
      long time = System.currentTimeMillis();
      System.out.println("Sorting " + numLines);
      utils.sort(in, out, "UTF-8", 0, "\t", '"', "\n", 0);
      System.out.println("Sorted " + numLines + " in " + (System.currentTimeMillis() - time) / 1000 + " secs");

      System.out.println("Sorting " + numLines + " in vanilla Java");
      Comparator<String> lineComparator = new LineComparator(0, "\t");
      time = System.currentTimeMillis();
      utils.sortInJava(in, out2, "UTF-8", lineComparator, 1);
      System.out.println("Sorted " + numLines + " in " + (System.currentTimeMillis() - time) / 1000 + " secs");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
