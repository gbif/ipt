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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.SourceFileProblem;
import org.gbif.ipt.model.SourceLog;
import org.gbif.ipt.utils.EncodingDamage;
import org.gbif.ipt.model.SourceFileProblem.Type;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;

/**
 * Checks a delimited text file line by line for problems that make it read differently than the publisher expects.
 * Lines are split and values tokenized the way the IPT reads the file (see org.gbif.utils.file.csv.CSVReader): lines
 * end at \n, \r or \r\n, and the values of a line are split on the delimiter, honouring the quote.
 */
class TextFileChecker {

  // examples kept per kind of problem, for the source log
  static final int MAX_EXAMPLES = 100;
  // longest value shown in an example
  private static final int MAX_VALUE_LENGTH = 80;
  private static final char REPLACEMENT = EncodingDamage.REPLACEMENT;

  private final String delimiter;
  private final Character quote;
  private final int headerLines;
  private final StrTokenizer tokenizer;
  private final Map<Type, SourceLog.Problem> found = new EnumMap<>(Type.class);

  private String[] header;
  private long line;

  /**
   * The problems found, in the order of {@link Type}, with up to {@link #MAX_EXAMPLES} examples of each.
   */
  static class Result {
    private final List<SourceLog.Problem> problems;

    Result(List<SourceLog.Problem> problems) {
      this.problems = problems;
    }

    List<SourceLog.Problem> getProblems() {
      return problems;
    }

    /**
     * @return the problems without their examples, to keep on the source
     */
    List<SourceFileProblem> getSummary() {
      return problems.stream()
        .map(p -> new SourceFileProblem(p.getType(), p.getLines(), p.getFirstLine()))
        .collect(Collectors.toList());
    }

    /**
     * @return the examples of the problem, empty if it wasn't found
     */
    List<SourceLog.Example> getExamples(Type type) {
      return problems.stream().filter(p -> p.getType() == type).findFirst()
        .map(SourceLog.Problem::getExamples).orElse(new ArrayList<>());
    }
  }

  TextFileChecker(String delimiter, Character quote, int headerLines) {
    this.delimiter = StringUtils.defaultString(delimiter);
    this.quote = quote;
    this.headerLines = headerLines;
    tokenizer = new StrTokenizer();
    tokenizer.setDelimiterString(this.delimiter);
    if (quote != null) {
      tokenizer.setQuoteChar(quote);
    }
    tokenizer.setIgnoreEmptyTokens(false);
  }

  /**
   * Checks the file. Bytes that aren't valid in the charset are read as U+FFFD, as the IPT reads them.
   */
  Result check(File file, Charset charset) throws IOException {
    CharsetDecoder decoder = charset.newDecoder()
      .onMalformedInput(CodingErrorAction.REPORT)
      .onUnmappableCharacter(CodingErrorAction.REPORT);
    ByteBuffer bytes = ByteBuffer.allocate(8192);
    CharBuffer chars = CharBuffer.allocate(8192);
    StringBuilder current = new StringBuilder();
    boolean invalid = false;
    boolean previousCr = false;
    boolean endOfInput = false;
    line = 1;

    try (ReadableByteChannel channel = Files.newByteChannel(file.toPath())) {
      while (true) {
        if (!endOfInput && channel.read(bytes) == -1) {
          endOfInput = true;
        }
        bytes.flip();
        while (true) {
          CoderResult result = decoder.decode(bytes, chars, endOfInput);
          chars.flip();
          while (chars.hasRemaining()) {
            char c = chars.get();
            if (c == '\r' || (c == '\n' && !previousCr)) {
              checkLine(current.toString(), invalid);
              current.setLength(0);
              invalid = false;
            } else if (c != '\n') {
              current.append(c);
            }
            previousCr = c == '\r';
          }
          chars.clear();
          if (result.isError()) {
            // skip the bytes, as the IPT replaces them
            current.append(REPLACEMENT);
            invalid = true;
            previousCr = false;
            bytes.position(bytes.position() + result.length());
          } else if (result.isUnderflow()) {
            break;
          }
        }
        bytes.compact();
        if (endOfInput) {
          break;
        }
      }
    }
    // like BufferedReader, a last line without a line break is a line, and a line break at the end doesn't start one
    if (current.length() > 0 || invalid) {
      checkLine(current.toString(), invalid);
    }

    return new Result(new ArrayList<>(found.values()));
  }

  /**
   * @param text    the line, without its line break
   * @param invalid true if the line has bytes that aren't valid in the charset, read as U+FFFD
   */
  private void checkLine(String text, boolean invalid) {
    try {
      if (text.isEmpty()) {
        add(Type.EMPTY_LINES, new SourceLog.Example(line, null, null, null, null, null));
        return;
      }

      tokenizer.reset(text);
      String[] values = tokenizer.getTokenArray();
      if (header == null) {
        header = values;
      }

      if (invalid) {
        add(Type.INVALID_CHARACTERS, example(values, v -> v.indexOf(REPLACEMENT) >= 0));
      }

      // the first line sets the number of columns, as it does for the source
      boolean headerLine = line <= Math.max(1, headerLines);
      if (!headerLine && values.length != header.length) {
        add(Type.COLUMN_COUNT, new SourceLog.Example(line, null, null, null, null, values.length));
      }

      if (quote != null) {
        SourceLog.Example quoteProblem = quoteProblem(text);
        if (quoteProblem != null) {
          add(Type.UNREADABLE_QUOTES, quoteProblem);
        }
      }

      if (EncodingDamage.mayLookDamaged(values)) {
        SourceLog.Example damaged = example(values, v -> EncodingDamage.looksDamaged(v, invalid));
        if (damaged != null) {
          add(Type.DAMAGED_VALUES, damaged);
        }
      }
    } finally {
      line++;
    }
  }

  private void add(Type type, SourceLog.Example example) {
    SourceLog.Problem problem = found.computeIfAbsent(type, t -> new SourceLog.Problem(t, 0, line, new ArrayList<>()));
    problem.setLines(problem.getLines() + 1);
    if (problem.getExamples().size() < MAX_EXAMPLES) {
      problem.getExamples().add(example);
    }
  }

  /**
   * @return an example of the first value that matches, or null if none
   */
  private SourceLog.Example example(String[] values, Predicate<String> matches) {
    for (int i = 0; i < values.length; i++) {
      if (matches.test(values[i])) {
        return example(i, values[i], null);
      }
    }
    return null;
  }

  private SourceLog.Example example(int column, String value, SourceLog.Reason reason) {
    String name = null;
    if (headerLines > 0 && header != null && column < header.length && StringUtils.isNotBlank(header[column])) {
      name = header[column];
    }
    return new SourceLog.Example(line, column + 1, name, StringUtils.abbreviate(value, MAX_VALUE_LENGTH), reason, null);
  }

  /**
   * Reads the line's values as RFC 4180 does: a value that starts with the quote ends at the next quote that isn't
   * doubled, which must be followed by the delimiter or the end of the line. Records can't span lines in the IPT.
   *
   * @return an example of the first quoted value that can't be read, or null if all can be
   */
  SourceLog.Example quoteProblem(String text) {
    char q = quote;
    int i = 0;
    int column = 0;
    while (true) {
      if (i < text.length() && text.charAt(i) == q) {
        int start = i;
        boolean closed = false;
        i++;
        while (i < text.length()) {
          if (text.charAt(i) == q) {
            if (i + 1 < text.length() && text.charAt(i + 1) == q) {
              i += 2;
              continue;
            }
            closed = true;
            i++;
            break;
          }
          i++;
        }
        if (!closed) {
          return example(column, text.substring(start), SourceLog.Reason.QUOTE_NOT_CLOSED);
        }
        if (i < text.length() && (delimiter.isEmpty() || !text.startsWith(delimiter, i))) {
          int end = delimiter.isEmpty() ? -1 : text.indexOf(delimiter, i);
          return example(column, text.substring(start, end < 0 ? text.length() : end),
                         SourceLog.Reason.TEXT_AFTER_QUOTE);
        }
      } else {
        int next = delimiter.isEmpty() ? -1 : text.indexOf(delimiter, i);
        if (next < 0) {
          return null;
        }
        i = next;
      }
      if (i >= text.length()) {
        return null;
      }
      i += delimiter.length();
      column++;
    }
  }
}
