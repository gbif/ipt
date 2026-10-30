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

import org.gbif.ipt.model.SourceFileProblem.Type;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelFileSourceTest {

  @TempDir
  File tmpDir;

  private interface SheetWriter {
    void write(Sheet sheet);
  }

  private ExcelFileSource source(int headerLines, SheetWriter writer) throws IOException {
    File file = new File(tmpDir, "book.xlsx");
    try (Workbook book = new XSSFWorkbook(); OutputStream out = new FileOutputStream(file)) {
      writer.write(book.createSheet("data"));
      book.write(out);
    }
    ExcelFileSource source = new ExcelFileSource();
    source.setName("book");
    source.setFile(file);
    source.setIgnoreHeaderLines(headerLines);
    return source;
  }

  // rowIndex from 0, as in POI
  private static void row(Sheet sheet, int rowIndex, String... values) {
    Row row = sheet.createRow(rowIndex);
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        row.createCell(i).setCellValue(values[i]);
      }
    }
  }

  private static SourceLog.Problem problem(List<SourceLog.Problem> problems, Type type) {
    return problems.stream().filter(p -> p.getType() == type).findFirst().orElse(null);
  }

  private static List<Long> lines(SourceLog.Problem problem) {
    return problem.getExamples().stream().map(SourceLog.Example::getLine).collect(Collectors.toList());
  }

  @Test
  public void testNoProblems() throws IOException {
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id", "name", "country");
      row(sheet, 1, "1", "a", "DK");
      // fewer cells than the first row are read as empty values
      row(sheet, 2, "2", "b");
    });

    List<SourceLog.Problem> problems = source.analyze();

    assertTrue(problems.isEmpty());
    assertEquals(3, source.getColumns());
    assertEquals(3, source.getRows());
    assertTrue(source.isReadable());
  }

  @Test
  public void testCellsBeyondColumns() throws IOException {
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id", "name");
      row(sheet, 1, "1", "a", "extra", "more");
      // a blank cell to the right is no problem, as nothing is lost
      row(sheet, 2, "2", "b", " ");
      row(sheet, 3, "3", null, null, "far");
    });

    SourceLog.Problem problem = problem(source.analyze(), Type.CELLS_BEYOND_COLUMNS);

    assertEquals(2, problem.getLines());
    // rows as numbered in Excel
    assertEquals(2, problem.getFirstLine());
    assertEquals(List.of(2L, 4L), lines(problem));
    // the first value that isn't read
    SourceLog.Example first = problem.getExamples().get(0);
    assertEquals(3, first.getColumnNumber());
    assertEquals("C", first.getColumn());
    assertEquals("extra", first.getValue());
    assertEquals("D", problem.getExamples().get(1).getColumn());
    assertEquals("far", problem.getExamples().get(1).getValue());
  }

  @Test
  public void testLongValuesAreShortened() throws IOException {
    String longValue = "x".repeat(200);
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id");
      row(sheet, 1, "1", longValue);
    });

    SourceLog.Problem problem = problem(source.analyze(), Type.CELLS_BEYOND_COLUMNS);

    String value = problem.getExamples().get(0).getValue();
    assertEquals(SourceLog.MAX_VALUE_LENGTH, value.length());
    assertEquals("x".repeat(SourceLog.MAX_VALUE_LENGTH - 3) + "...", value);
  }

  @Test
  public void testValuesBeyondColumnsAreNotRead() throws IOException {
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id", "name");
      row(sheet, 1, "1", "a", "extra");
    });
    source.analyze();

    // what the check reports is what reading the source loses
    try (ClosableReportingIterator<String[]> iter = source.rowIterator()) {
      assertEquals(List.of("1", "a"), List.of(iter.next()));
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Test
  public void testEmptyRows() throws IOException {
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id", "name");
      row(sheet, 1, "1", "a");
      // rows 3 and 4 are missing from the sheet
      row(sheet, 4, "2", "b");
      // a row with only blank cells
      row(sheet, 5, " ", "");
      // a row with values only to the right of the columns is empty too, as none of its values are read
      row(sheet, 6, null, null, "extra");
      row(sheet, 7, "3", "c");
    });

    List<SourceLog.Problem> problems = source.analyze();

    SourceLog.Problem empty = problem(problems, Type.EMPTY_LINES);
    assertEquals(4, empty.getLines());
    assertEquals(3, empty.getFirstLine());
    assertEquals(List.of(3L, 4L, 6L, 7L), lines(empty));
    assertNull(empty.getExamples().get(0).getColumnNumber());
    assertEquals(List.of(7L), lines(problem(problems, Type.CELLS_BEYOND_COLUMNS)));
    // the rows in the sheet, the header and empty ones included, as before
    assertEquals(6, source.getRows());
  }

  @Test
  public void testRowsBeforeTheDataAreNotChecked() throws IOException {
    // the missing first row and the empty second one are header lines
    ExcelFileSource source = source(2, sheet -> {
      row(sheet, 1, "", "");
      row(sheet, 2, "id", "name", "notes");
      row(sheet, 3, "1", "a");
    });

    assertTrue(source.analyze().isEmpty());
  }

  @Test
  public void testNoHeaderLines() throws IOException {
    ExcelFileSource source = source(0, sheet -> {
      row(sheet, 0, "1", "a");
      row(sheet, 2, "2", "b", "extra");
    });

    List<SourceLog.Problem> problems = source.analyze();

    assertEquals(List.of(2L), lines(problem(problems, Type.EMPTY_LINES)));
    assertEquals(List.of(3L), lines(problem(problems, Type.CELLS_BEYOND_COLUMNS)));
  }

  @Test
  public void testExamplesAreLimited() throws IOException {
    int missing = SourceLog.MAX_EXAMPLES + 50;
    ExcelFileSource source = source(1, sheet -> {
      row(sheet, 0, "id");
      row(sheet, 1, "1");
      row(sheet, missing + 2, "2");
    });

    SourceLog.Problem empty = problem(source.analyze(), Type.EMPTY_LINES);

    assertEquals(missing, empty.getLines());
    assertEquals(SourceLog.MAX_EXAMPLES, empty.getExamples().size());
  }

  @Test
  public void testEmptySheet() throws IOException {
    ExcelFileSource source = source(1, sheet -> {
    });

    assertTrue(source.analyze().isEmpty());
    assertEquals(0, source.getColumns());
    assertEquals(0, source.getRows());
    assertFalse(source.isReadable());
  }

  @Test
  public void testMessageKeys() {
    ExcelFileSource source = new ExcelFileSource();
    assertEquals("manage.source.problem.excel.emptyLines", SourceFileProblem.messageKey(Type.EMPTY_LINES, source));
    assertEquals("manage.source.problem.cellsBeyondColumns",
                 SourceFileProblem.messageKey(Type.CELLS_BEYOND_COLUMNS, source));
  }
}
