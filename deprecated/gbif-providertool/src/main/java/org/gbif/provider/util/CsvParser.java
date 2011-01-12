/**
 * Copyright 2005 Bytecode Pty Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple CSV parser released under a commercial-friendly license. This
 * just implements splitting a single line into fields.
 * 
 * @author Glen Smith
 * @author Rainer Pruy
 * 
 */
public class CsvParser {

  private final char separator;

  private final char quotechar;

  private final char escape;

  private final boolean strictQuotes;

  private String pending;

  /** The default separator to use if none is supplied to the constructor. */
  public static final char DEFAULT_SEPARATOR = ',';

  public static final int INITIAL_READ_SIZE = 128;

  /**
   * The default quote character to use if none is supplied to the constructor.
   */
  public static final char DEFAULT_QUOTE_CHARACTER = '"';

  /**
   * The default escape character to use if none is supplied to the constructor.
   */
  public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

  /**
   * The default strict quote behavior to use if none is supplied to the
   * constructor
   */
  public static final boolean DEFAULT_STRICT_QUOTES = false;

  /**
   * Constructs CsvParser using a comma for the separator.
   */
  public CsvParser() {
    this(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Constructs CsvParser with supplied separator.
   * 
   * @param separator the delimiter to use for separating entries.
   */
  public CsvParser(char separator) {
    this(separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Constructs CsvParser with supplied separator and quote char.
   * 
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   */
  public CsvParser(char separator, char quotechar) {
    this(separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   */
  public CsvParser(char separator, char quotechar, char escape) {
    this(separator, quotechar, escape, DEFAULT_STRICT_QUOTES);
  }

  /**
   * Constructs CSVReader with supplied separator and quote char. Allows setting
   * the "strict quotes" flag
   * 
   * @param separator the delimiter to use for separating entries
   * @param quotechar the character to use for quoted elements
   * @param escape the character to use for escaping a separator or quote
   * @param strictQuotes if true, characters outside the quotes are ignored
   */
  public CsvParser(char separator, char quotechar, char escape,
      boolean strictQuotes) {
    this.separator = separator;
    this.quotechar = quotechar;
    this.escape = escape;
    this.strictQuotes = strictQuotes;
  }

  /**
   * 
   * @return true if something was left over from last call(s)
   */
  public boolean isPending() {
    return pending != null;
  }

  public String[] parseLine(String nextLine) throws IOException {
    return parseLine(nextLine, false);
  }

  public String[] parseLineMulti(String nextLine) throws IOException {
    return parseLine(nextLine, true);
  }

  /**
   * precondition: the current character is an escape
   * 
   * @param nextLine the current line
   * @param inQuotes true if the current context is quoted
   * @param i current index in line
   * @return true if the following character is a quote
   */
  protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes,
      int i) {
    return inQuotes // we are in quotes, therefore there can be escaped quotes
        // in here.
        && nextLine.length() > (i + 1) // there is indeed another character to
        // check.
        && (nextLine.charAt(i + 1) == quotechar || nextLine.charAt(i + 1) == this.escape);
  }

  /**
   * precondition: the current character is a quote or an escape
   * 
   * @param nextLine the current line
   * @param inQuotes true if the current context is quoted
   * @param i current index in line
   * @return true if the following character is a quote
   */
  private boolean isNextCharacterEscapedQuote(String nextLine,
      boolean inQuotes, int i) {
    return inQuotes // we are in quotes, therefore there can be escaped quotes
        // in here.
        && nextLine.length() > (i + 1) // there is indeed another character to
        // check.
        && nextLine.charAt(i + 1) == quotechar;
  }

  /**
   * Parses an incoming String and returns an array of elements.
   * 
   * @param nextLine the string to parse
   * @param multi
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private String[] parseLine(String nextLine, boolean multi) throws IOException {

    if (!multi && pending != null) {
      pending = null;
    }

    if (nextLine == null) {
      if (pending != null) {
        String s = pending;
        pending = null;
        return new String[]{s};
      } else {
        return null;
      }
    }

    List<String> tokensOnThisLine = new ArrayList<String>();
    StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
    boolean inQuotes = false;
    if (pending != null) {
      sb.append(pending);
      pending = null;
      inQuotes = true;
    }
    for (int i = 0; i < nextLine.length(); i++) {

      char c = nextLine.charAt(i);
      if (c == this.escape) {
        if (isNextCharacterEscapable(nextLine, inQuotes, i)) {
          sb.append(nextLine.charAt(i + 1));
          i++;
        }
      } else if (c == quotechar) {
        if (isNextCharacterEscapedQuote(nextLine, inQuotes, i)) {
          sb.append(nextLine.charAt(i + 1));
          i++;
        } else {
          inQuotes = !inQuotes;
          // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
          if (!strictQuotes) {
            if (i > 2 // not on the beginning of the line
                && nextLine.charAt(i - 1) != this.separator // not at the
                // beginning of an
                // escape sequence
                && nextLine.length() > (i + 1)
                && nextLine.charAt(i + 1) != this.separator // not at the end of
            // an escape
            // sequence
            ) {
              sb.append(c);
            }
          }
        }
      } else if (c == separator && !inQuotes) {
        tokensOnThisLine.add(sb.toString());
        sb = new StringBuilder(INITIAL_READ_SIZE); // start work on next token
      } else {
        if (!strictQuotes || inQuotes) {
          sb.append(c);
        }
      }
    }
    // line is done - check status
    if (inQuotes) {
      if (multi) {
        // continuing a quoted section, re-append newline
        sb.append("\n");
        pending = sb.toString();
        sb = null; // this partial content is not to be added to field list yet
      } else {
        throw new IOException("Un-terminated quoted field at end of CSV line");
      }
    }
    if (sb != null) {
      tokensOnThisLine.add(sb.toString());
    }
    return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);

  }
}
