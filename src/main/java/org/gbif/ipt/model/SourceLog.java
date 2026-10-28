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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What was found when a source was last analysed: how it was read, what was fixed when it was uploaded, and the
 * problems found with examples. Written as JSON next to the source for the source log page.
 */
@Data
@NoArgsConstructor
public class SourceLog {

  private String source;
  private Date analysed;
  // how a text file was read, null for other sources
  private String encoding;
  private String delimiter;
  private String quote;
  private int rows;
  private int columns;
  // line breaks inside quoted values removed when the file was uploaded
  private int linesWithEmbeddedBreaksRemoved;
  private List<Problem> problems = new ArrayList<>();

  /**
   * A kind of problem: how many lines have it, the first of them, and examples of the first lines.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Problem {
    private SourceFileProblem.Type type;
    private long lines;
    private long firstLine;
    private List<Example> examples = new ArrayList<>();
  }

  /**
   * A line with the problem.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Example {
    private long line;
    // the column's number from 1, and its name if the source has a header. Null if the problem is with the whole line
    private Integer columnNumber;
    private String column;
    // the value with the problem, null if the problem is with the whole line
    private String value;
    // what's wrong, for problems that can be wrong in more than one way, see Reason. Null otherwise
    private Reason reason;
    // the number of columns on the line, for COLUMN_COUNT
    private Integer lineColumns;
  }

  public enum Reason {
    // a quoted value isn't closed on its line
    QUOTE_NOT_CLOSED("quoteNotClosed"),
    // a quoted value has more text after its closing quote
    TEXT_AFTER_QUOTE("textAfterQuote");

    private final String key;

    Reason(String key) {
      this.key = key;
    }

    /**
     * @return the key of the reason's message, after manage.source.log.reason.
     */
    public String getKey() {
      return key;
    }
  }
}
