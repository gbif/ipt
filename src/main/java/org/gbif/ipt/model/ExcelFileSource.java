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

import org.gbif.ipt.utils.IptFileUtils;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import com.github.pjfanning.xlsx.SharedStringsImplementationType;
import com.github.pjfanning.xlsx.StreamingReader;
import lombok.Getter;
import lombok.Setter;

/**
 * Uses apache POI to parse excel spreadsheets.
 * A single file can have multiple sheets which each act as a separate source.
 * The same file can therefore be used for multiple ExcelFileSource instances.
 * <p>
 * To avoid extensive memory usage we use stream reading approach with the library excel-streaming-reader.
 *
 */
public class ExcelFileSource extends SourceBase implements FileSource {

  @Serial
  private static final long serialVersionUID = 1457018220676830122L;

  private static final Logger LOG = LogManager.getLogger(ExcelFileSource.class);
  private static final String SUFFIX = ".xls";

  @Setter
  @Getter
  private int sheetIdx = 0;
  @Setter
  private int ignoreHeaderLines = 1;
  private File file;
  @Setter
  private long fileSize;
  @Setter
  private int rows;
  protected Date lastModified;
  // problems found when the source was last analysed, null if it hasn't been since they were checked for
  @Getter
  @Setter
  private List<SourceFileProblem> problems;

  public String formattedFileSize(String locale) {
    return IptFileUtils.formatSize(fileSize, 1, locale, false);
  }

  private Workbook openBook() {
    LOG.info("Opening excel workbook [{}]", file.getName());

    return StreamingReader.builder()
      .rowCacheSize(100)
      .bufferSize(4096)
      .setSharedStringsImplementationType(SharedStringsImplementationType.TEMP_FILE_BACKED)
      .setReadSharedFormulas(true)
      .setEncryptSstTempFile(true)
      .open(file);
  }

  private Sheet getSheet(Workbook book) {
    return book.getSheetAt(sheetIdx);
  }

  private class RowIterator implements ClosableReportingIterator<String[]> {

    private final Workbook book;
    private final String sourceName;
    private final Iterator<Row> iter;
    private final int rowSize;
    // DataFormatter displays data exactly as it appears in Excel
    private final DataFormatter dataFormatter = new DataFormatter();
    private boolean rowError;
    private String errorMessage;
    private Exception exception;

    RowIterator(ExcelFileSource source) {
      book = openBook();
      Sheet sheet = getSheet(book);
      iter = sheet.rowIterator();
      rowSize = source.getColumns();
      sourceName = source.getName();
      dataFormatter.setUseCachedValuesForFormulaCells(true);
    }

    RowIterator(ExcelFileSource source, int skipRows) {
      this(source);
      while (skipRows > 0) {
        iter.next();
        skipRows--;
      }
    }

    @Override
    public void close() {
      try {
        book.close();
      } catch (IOException e) {
        LOG.error("Failed to close workbook for the source {}", sourceName, e);
      }
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public String[] next() {
      String[] val = new String[rowSize];
      if (hasNext()) {
        resetReportingIterator();
        try {
          Row row = iter.next();
          for (int i = 0; i < rowSize; i++) {
            Cell c = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            val[i] = dataFormatter.formatCellValue(c);
          }
        } catch (Exception e) {
          LOG.debug("Exception caught: {}", e.getMessage(), e);
          exception = e;
          errorMessage = e.getMessage();
        }
      }
      return val;
    }

    /**
     * Reset all reporting parameters.
     */
    private void resetReportingIterator() {
      rowError = false;
      exception = null;
      errorMessage = null;
    }

    @Override
    public void remove() {
      // unsupported
    }

    @Override
    public boolean hasRowError() {
      return rowError;
    }

    @Override
    public String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public Exception getException() {
      return exception;
    }
  }

  @Override
  public ClosableReportingIterator<String[]> rowIterator() {
    try {
      return new RowIterator(this, ignoreHeaderLines);
    } catch (Exception e) {
      LOG.error("Exception while reading excel source {}", name, e);
    }
    return null;
  }

  /**
   * @return map of available sheets, keyed on sheet index
   */
  public Map<Integer, String> sheets() {
    Map<Integer, String> sheets = new HashMap<>();
    try (Workbook book = openBook()) {
      int cnt = book.getNumberOfSheets();
      for (int x = 0; x < cnt; x++) {
        sheets.put(x, book.getSheetName(x));
      }
    } catch (Exception e) {
      LOG.error("Exception while reading excel source {}", name, e);
    }
    return sheets;
  }

  @Override
  public List<String> columns() {
    if (rows > 0) {
      try (RowIterator iter = new RowIterator(this, ignoreHeaderLines - 1)) {
        if (ignoreHeaderLines > 0) {
          return new ArrayList<>(Arrays.asList(iter.next()));

        } else {
          List<String> columnList = new ArrayList<>();
          for (int x = 1; x <= columns; x++) {
            columnList.add("Column #" + x);
          }
          return columnList;
        }

      } catch (Exception e) {
        LOG.error("Exception while reading excel source {}", name, e);
      }
    }

    // no rows, no columns
    return new ArrayList<>();
  }

  /**
   * Reads the sheet once to count its rows and columns, and checks the rows after the header lines for problems:
   * values to the right of the first row's last column, which aren't read, and empty rows, which are skipped. Empty
   * rows are rows with no values in the columns, and rows missing from the sheet between two rows. Rows are numbered
   * as in Excel.
   *
   * @return the problems found, with up to {@value SourceLog#MAX_EXAMPLES} examples of each
   */
  public List<SourceLog.Problem> analyze() throws IOException {
    setFileSize(getFile().length());
    SourceLog.Problem cellsBeyondColumns =
      new SourceLog.Problem(SourceFileProblem.Type.CELLS_BEYOND_COLUMNS, 0, 0, new ArrayList<>());
    SourceLog.Problem emptyRows = new SourceLog.Problem(SourceFileProblem.Type.EMPTY_LINES, 0, 0, new ArrayList<>());
    DataFormatter dataFormatter = new DataFormatter();
    dataFormatter.setUseCachedValuesForFormulaCells(true);

    try (Workbook book = openBook()) {
      Iterator<Row> iter = getSheet(book).rowIterator();
      int physicalNumberOfRows = 0;
      int lastRowNum = -1;
      while (iter.hasNext()) {
        Row row = iter.next();
        physicalNumberOfRows++;
        if (physicalNumberOfRows == 1) {
          // a row without cells has -1
          setColumns(Math.max(0, row.getLastCellNum()));
        }

        if (physicalNumberOfRows > ignoreHeaderLines) {
          if (physicalNumberOfRows > 1) {
            for (int missing = lastRowNum + 1; missing < row.getRowNum(); missing++) {
              found(emptyRows, missing + 1, null, null);
            }
          }
          // checks the columns RowIterator reads only until one isn't blank, and stops at the first value beyond them
          // that isn't blank
          boolean empty = true;
          for (Cell cell : row) {
            if (cell.getColumnIndex() < columns) {
              if (empty && StringUtils.isNotBlank(dataFormatter.formatCellValue(cell))) {
                empty = false;
              }
            } else {
              String value = dataFormatter.formatCellValue(cell);
              if (StringUtils.isNotBlank(value)) {
                found(cellsBeyondColumns, row.getRowNum() + 1, cell.getColumnIndex(), value);
                break;
              }
            }
          }
          if (empty) {
            found(emptyRows, row.getRowNum() + 1, null, null);
          }
        }
        lastRowNum = row.getRowNum();
      }

      if (physicalNumberOfRows == 0) {
        setColumns(0);
      }
      setRows(physicalNumberOfRows);
      setReadable(physicalNumberOfRows > 0);
    }

    List<SourceLog.Problem> problems = new ArrayList<>();
    for (SourceLog.Problem problem : Arrays.asList(cellsBeyondColumns, emptyRows)) {
      if (problem.getLines() > 0) {
        problems.add(problem);
      }
    }
    return problems;
  }

  /**
   * @param columnIndex the cell's column from 0, or null if the problem is with the whole row
   */
  private static void found(SourceLog.Problem problem, long rowNumber, Integer columnIndex, String value) {
    if (problem.getLines() == 0) {
      problem.setFirstLine(rowNumber);
    }
    problem.setLines(problem.getLines() + 1);
    if (problem.getExamples().size() < SourceLog.MAX_EXAMPLES) {
      problem.getExamples().add(columnIndex == null
        ? new SourceLog.Example(rowNumber, null, null, null, null, null)
        : new SourceLog.Example(rowNumber, columnIndex + 1, CellReference.convertNumToColString(columnIndex),
                                StringUtils.abbreviate(value, SourceLog.MAX_VALUE_LENGTH), null, null));
    }
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.EXCEL_FILE;
  }

  @Override
  public String getPreferredFileSuffix() {
    return SUFFIX;
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public long getFileSize() {
    return fileSize;
  }

  @Override
  public void setFile(File file) {
    this.file = file;
  }

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public int getRows() {
    return rows;
  }

  @Override
  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  @Override
  public Date getLastModified() {
    return lastModified;
  }
}
