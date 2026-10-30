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

import java.io.Serial;
import java.io.Serializable;

/**
 * A kind of problem found in a file source when it was last analysed: how many lines have it, and the first of
 * them. The details, with examples, are in the source log.
 */
public class SourceFileProblem implements Serializable {

  @Serial
  private static final long serialVersionUID = 4182736590214857731L;

  public enum Type {
    // characters that aren't valid in the source's encoding
    INVALID_CHARACTERS("invalidCharacters"),
    // rows with a different number of columns than the header
    COLUMN_COUNT("columnCount"),
    // quoted values that aren't closed, or have more text after the closing quote
    UNREADABLE_QUOTES("unreadableQuotes"),
    // values that look damaged by encoding problems, like Ã¸ for ø
    DAMAGED_VALUES("damagedValues"),
    // values in an Excel sheet to the right of the first row's last column, which aren't read
    CELLS_BEYOND_COLUMNS("cellsBeyondColumns"),
    // empty lines, or empty rows in an Excel sheet, which are skipped
    EMPTY_LINES("emptyLines");

    private final String key;

    Type(String key) {
      this.key = key;
    }

    /**
     * @return the key of the problem's message, after manage.source.problem.
     */
    public String getKey() {
      return key;
    }
  }

  /**
   * @return the key of the message saying what the problem is and how to fix it. A URL source's is fixed in the file at
   *         the URL, since its settings are taken from the file each time it's downloaded. Empty lines need no fixing.
   *         An Excel sheet's empty lines are rows
   */
  public static String messageKey(Type type, Source source) {
    String prefix = "";
    if (source instanceof UrlSource && type != Type.EMPTY_LINES) {
      prefix = "url.";
    } else if (source instanceof ExcelFileSource && type == Type.EMPTY_LINES) {
      prefix = "excel.";
    }
    return "manage.source.problem." + prefix + type.getKey();
  }

  private Type type;
  private long lines;
  private long firstLine;

  public SourceFileProblem(Type type, long lines, long firstLine) {
    this.type = type;
    this.lines = lines;
    this.firstLine = firstLine;
  }

  public Type getType() {
    return type;
  }

  /**
   * @return the number of lines with the problem
   */
  public long getLines() {
    return lines;
  }

  /**
   * @return the first line with the problem, counting from 1
   */
  public long getFirstLine() {
    return firstLine;
  }
}
