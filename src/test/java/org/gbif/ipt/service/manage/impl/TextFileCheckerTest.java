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
import org.gbif.ipt.model.SourceFileProblem.Type;
import org.gbif.ipt.model.SourceLog;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextFileCheckerTest {

  @TempDir
  File tmpDir;

  private TextFileChecker.Result check(byte[] content, Charset charset, int headerLines) throws IOException {
    File file = new File(tmpDir, "file.txt");
    Files.write(file.toPath(), content);
    return new TextFileChecker("\t", '"', headerLines).check(file, charset);
  }

  private TextFileChecker.Result check(String content, int headerLines) throws IOException {
    return check(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, headerLines);
  }

  private static SourceFileProblem problem(TextFileChecker.Result result, Type type) {
    return result.getSummary().stream().filter(p -> p.getType() == type).findFirst().orElse(null);
  }

  /**
   * @return the examples as in the text log, with a first line of two columns
   */
  private static List<String> examples(TextFileChecker.Result result, Type type) {
    SourceLog log = new SourceLog();
    log.setColumns(2);
    return result.getExamples(type).stream().map(e -> SourceManagerImpl.describeExample(e, log))
      .collect(Collectors.toList());
  }

  @ParameterizedTest
  @MethodSource("invalidCharacterCases")
  public void testInvalidCharacterLine(byte[] content, Charset charset, long expectedLine) throws IOException {
    TextFileChecker.Result result = check(content, charset, 0);
    SourceFileProblem problem = problem(result, Type.INVALID_CHARACTERS);
    assertEquals(1, problem.getLines());
    assertEquals(expectedLine, problem.getFirstLine());
    // the replacement for the invalid bytes isn't reported again as a damaged value
    assertNull(problem(result, Type.DAMAGED_VALUES));
  }

  static Stream<Arguments> invalidCharacterCases() {
    return Stream.of(
      // invalid byte on the first line
      Arguments.of(new byte[]{'a', (byte) 0xAC, '\n', 'b', '\n'}, StandardCharsets.UTF_8, 1L),
      // \r\n, \n and \r line endings
      Arguments.of(new byte[]{'a', '\r', '\n', 'b', '\r', '\n', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      Arguments.of(new byte[]{'a', '\n', 'b', '\n', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      Arguments.of(new byte[]{'a', '\r', 'b', '\r', (byte) 0xAC}, StandardCharsets.UTF_8, 3L),
      // a multi-byte sequence cut off at the end of the file
      Arguments.of(new byte[]{'a', '\n', 'b', '\n', 'c', (byte) 0xC3}, StandardCharsets.UTF_8, 3L),
      // a lone surrogate in UTF-16LE on the second line
      Arguments.of(new byte[]{'a', 0, '\n', 0, 'b', 0, 0, (byte) 0xD8, 'c', 0}, StandardCharsets.UTF_16LE, 2L),
      // a byte windows-1252 doesn't define
      Arguments.of(new byte[]{'a', '\n', (byte) 0x81}, Charset.forName("windows-1252"), 2L));
  }

  @Test
  public void testInvalidCharactersCountsLinesAndShowsTheValue() throws IOException {
    byte[] content = {'i', 'd', '\t', 'n', 'a', 'm', 'e', '\n',
                      '1', '\t', 'a', (byte) 0xAC, (byte) 0xAC, '\n',
                      '2', '\t', 'b', '\n',
                      '3', '\t', (byte) 0xFF, '\n'};
    TextFileChecker.Result result = check(content, StandardCharsets.UTF_8, 1);
    SourceFileProblem problem = problem(result, Type.INVALID_CHARACTERS);
    assertEquals(2, problem.getLines());
    assertEquals(2, problem.getFirstLine());
    assertEquals(Arrays.asList("Line 2, column name: a��", "Line 4, column name: �"),
                 examples(result, Type.INVALID_CHARACTERS));
    // the replacements don't change the number of columns
    assertNull(problem(result, Type.COLUMN_COUNT));
  }

  @Test
  public void testNoProblems() throws IOException {
    TextFileChecker.Result result = check(
      "id\tlocality\tremarks\n1\tDragør\t\"a, \"\"quoted\"\" word\"\n2\tØstfold\t\n3\t\t\"\"\n"
      + "4\tSão Paulo\thttps://example.org/a?b=c\n", 1);
    assertTrue(result.getProblems().isEmpty());
  }

  @Test
  public void testColumnCount() throws IOException {
    TextFileChecker.Result result = check("id\tname\n1\ta\n2\tb\textra\n3\n4\td\n", 1);
    SourceFileProblem problem = problem(result, Type.COLUMN_COUNT);
    assertEquals(2, problem.getLines());
    assertEquals(3, problem.getFirstLine());
    assertEquals(Arrays.asList("Line 3: 3 columns, the first line has 2", "Line 4: 1 columns, the first line has 2"),
                 examples(result, Type.COLUMN_COUNT));
  }

  @Test
  public void testUnreadableQuotes() throws IOException {
    TextFileChecker.Result result = check(
      "id\tremarks\n1\t\"first\" and more\n2\t\"not closed\n3\t\"fine\"\n4\tquote \" inside unquoted\n", 1);
    SourceFileProblem problem = problem(result, Type.UNREADABLE_QUOTES);
    assertEquals(2, problem.getLines());
    assertEquals(2, problem.getFirstLine());
    assertEquals(Arrays.asList("Line 2, column remarks: text after the closing quote: \"first\" and more",
                               "Line 3, column remarks: the quoted value isn't closed on the line: \"not closed"),
                 examples(result, Type.UNREADABLE_QUOTES));
  }

  @Test
  public void testDamagedValues() throws IOException {
    TextFileChecker.Result result = check(
      "id\tlocality\n1\tÃ˜stfold\n2\tDragÃ¸r\n3\tSkakbr?t\n4\tit’s\n5\titâ€™s\n6\tbad �\n7\twhat?\n8\t??\n", 1);
    SourceFileProblem problem = problem(result, Type.DAMAGED_VALUES);
    assertEquals(5, problem.getLines());
    assertEquals(2, problem.getFirstLine());
    List<String> examples = examples(result, Type.DAMAGED_VALUES);
    assertEquals(Arrays.asList("Line 2, column locality: Ã˜stfold", "Line 3, column locality: DragÃ¸r",
                               "Line 4, column locality: Skakbr?t", "Line 6, column locality: itâ€™s",
                               "Line 7, column locality: bad �"), examples);
  }

  @Test
  public void testEmptyLinesWithoutHeader() throws IOException {
    TextFileChecker.Result result = check("1\ta\n\n2\tb\n\n\n3\tc", 0);
    SourceFileProblem problem = problem(result, Type.EMPTY_LINES);
    assertEquals(3, problem.getLines());
    assertEquals(2, problem.getFirstLine());
    assertEquals(Arrays.asList("Line 2", "Line 4", "Line 5"), examples(result, Type.EMPTY_LINES));
    assertNull(problem(result, Type.COLUMN_COUNT));
  }

  @Test
  public void testRowsAndColumns() throws IOException {
    TextFileChecker.Result result = check("id\tname\n1\ta\n\n2\tb\n", 1);
    assertEquals(2, result.getColumns());
    assertEquals(2, result.getRows());

    // the first line gives the columns, even when it's empty
    result = check("\nid\tname\n1\ta\n", 1);
    assertEquals(0, result.getColumns());
    assertEquals(2, result.getRows());
    assertEquals(2, problem(result, Type.COLUMN_COUNT).getLines());
    assertNull(problem(result, Type.EMPTY_LINES));

    // the first line after the header lines is a row even when it's empty, and isn't skipped
    result = check("id\tname\n\n1\ta\n\n", 1);
    assertEquals(2, result.getRows());
    assertEquals(Arrays.asList("Line 2: 0 columns, the first line has 2"), examples(result, Type.COLUMN_COUNT));
    assertEquals(Arrays.asList("Line 4"), examples(result, Type.EMPTY_LINES));

    result = check("", 1);
    assertEquals(0, result.getColumns());
    assertEquals(0, result.getRows());
  }

  /**
   * The rows, columns and empty lines are those the IPT reads (see CSVReader).
   */
  @ParameterizedTest
  @MethodSource("countCases")
  public void testCountsMatchTheReader(String content) throws IOException {
    File file = new File(tmpDir, "file.txt");
    Files.write(file.toPath(), content.getBytes(StandardCharsets.ISO_8859_1));
    for (int headerLines = 0; headerLines <= 3; headerLines++) {
      assertCountsMatch(file, StandardCharsets.UTF_8, "\t", headerLines);
    }
  }

  static Stream<String> countCases() {
    String longLine = "x".repeat(8191);
    // ISO-8859-1, to write the bytes as they are
    return Stream.of("", "\n", "\n\n\n", "id", "id\tname", "id\tname\n", "id\tname\n1\ta\n2\tb",
                     "id\tname\r\n1\ta\r\n\r\n2\tb\r\n", "id\tname\r1\ta\r\r2\tb", "\nid\tname\n1\ta",
                     "id\tname\n\n1\ta\n\n", "id\tname\n\n\n\n1\ta", "h1\nh2\n\n\n1\ta\n\n2\tb",
                     "id\tname\n1\n1\ta\tb\n", "id\t\"a\tb\"\n1\t\"x\ty\"\n\"z\"q\t1\n\"open\n",
                     "id\tname\n1\t\u00ff\n\n2\t\u00c3\n\u00c3\n", longLine + "\r\n1\n\n" + longLine + "\r\n2");
  }

  @Test
  public void testCountsMatchTheReaderForTestFiles() throws IOException {
    List<Path> files;
    try (Stream<Path> paths = Files.walk(Paths.get("src/test/resources"))) {
      files = paths.filter(Files::isRegularFile)
        .filter(f -> f.toString().matches(".*\\.(txt|csv|tsv|tab)$"))
        .collect(Collectors.toList());
    }
    assertTrue(files.size() > 10);
    for (Path f : files) {
      for (String delimiter : Arrays.asList("\t", ",")) {
        for (int headerLines = 0; headerLines <= 1; headerLines++) {
          assertCountsMatch(f.toFile(), StandardCharsets.UTF_8, delimiter, headerLines);
        }
      }
    }
  }

  private static void assertCountsMatch(File file, Charset charset, String delimiter, int headerLines)
    throws IOException {
    TextFileChecker.Result result = new TextFileChecker(delimiter, '"', headerLines).check(file, charset);
    CSVReader reader = CSVReaderFactory.build(file, charset.name(), delimiter, '"', headerLines);
    try {
      while (reader.hasNext()) {
        reader.next();
      }
    } finally {
      reader.close();
    }
    String what = file + " with " + headerLines + " header line(s), delimiter '" + delimiter + "'";
    assertEquals(reader.header == null ? 0 : reader.header.length, result.getColumns(), what);
    assertEquals(reader.getReadRows(), result.getRows(), what);
    List<Long> emptyLines = reader.getEmptyLines().stream().sorted().limit(TextFileChecker.MAX_EXAMPLES)
      .map(Integer::longValue).collect(Collectors.toList());
    assertEquals(emptyLines, result.getExamples(Type.EMPTY_LINES).stream().map(SourceLog.Example::getLine)
      .collect(Collectors.toList()), what);
  }

  @Test
  public void testExamplesAreCapped() throws IOException {
    StringBuilder sb = new StringBuilder("id\tname\n");
    for (int i = 0; i < TextFileChecker.MAX_EXAMPLES + 5; i++) {
      sb.append(i).append('\n');
    }
    TextFileChecker.Result result = check(sb.toString(), 1);
    assertEquals(TextFileChecker.MAX_EXAMPLES + 5, problem(result, Type.COLUMN_COUNT).getLines());
    assertEquals(TextFileChecker.MAX_EXAMPLES, result.getExamples(Type.COLUMN_COUNT).size());
  }

  @Test
  public void testQuoteProblemWithMultiCharacterAndEmptyDelimiter() {
    TextFileChecker checker = new TextFileChecker("||", '"', 0);
    assertNull(checker.quoteProblem("\"a||b\"||c||\"d\""));
    assertEquals(SourceLog.Reason.TEXT_AFTER_QUOTE, checker.quoteProblem("\"a\"|b").getReason());

    checker = new TextFileChecker("", '"', 0);
    assertNull(checker.quoteProblem("\"a b\""));
    assertEquals(SourceLog.Reason.TEXT_AFTER_QUOTE, checker.quoteProblem("\"a\" b").getReason());
  }
}
