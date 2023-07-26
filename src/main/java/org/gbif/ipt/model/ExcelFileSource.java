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

import org.gbif.ipt.utils.FileUtils;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.github.pjfanning.xlsx.SharedStringsImplementationType;
import com.github.pjfanning.xlsx.StreamingReader;

/**
 * Uses apache POI to parse excel spreadsheets.
 * A single file can have multiple sheets which each act as a separate source.
 * The same file can therefore be used for multiple ExcelFileSource instances.
 * <p>
 * To avoid extensive memory usage we use stream reading approach with the library excel-streaming-reader.
 *
 */
public class ExcelFileSource extends SourceBase implements FileSource {

  private static final Logger LOG = LogManager.getLogger(ExcelFileSource.class);
  private static final String SUFFIX = ".xls";

  private int sheetIdx = 0;
  private int ignoreHeaderLines = 0;
  private File file;
  private long fileSize;
  private int rows;
  protected Date lastModified;

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public long getFileSize() {
    return fileSize;
  }

  public String getFileSizeFormatted() {
    return FileUtils.formatSize(fileSize, 1, true);
  }

  @Override
  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  public void setIgnoreHeaderLines(int ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines;
  }

  @Override
  public Date getLastModified() {
    return lastModified;
  }

  public int getSheetIdx() {
    return sheetIdx;
  }

  public void setSheetIdx(int sheetIdx) {
    this.sheetIdx = sheetIdx;
  }

  private Workbook openBook() {
    LOG.info("Opening excel workbook [" + file.getName() + "]");

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

  @Override
  public int getRows() {
    return rows;
  }

  private class RowIterator implements ClosableReportingIterator<String[]> {

    private final Iterator<Row> iter;
    private final int rowSize;
    // DataFormatter displays data exactly as it appears in Excel
    private final DataFormatter dataFormatter = new DataFormatter();
    private boolean rowError;
    private String errorMessage;
    private Exception exception;

    RowIterator(ExcelFileSource source) throws IOException {
      try (Workbook book = openBook()) {
        Sheet sheet = getSheet(book);
        iter = sheet.rowIterator();
        rowSize = source.getColumns();
        dataFormatter.setUseCachedValuesForFormulaCells(true);
      }
    }

    RowIterator(ExcelFileSource source, int skipRows) throws IOException, InvalidFormatException {
      this(source);
      while (skipRows > 0) {
        iter.next();
        skipRows--;
      }
    }

    @Override
    public void close() {
      // nothing to do
    }

    @Override
    public boolean hasNext() {
      return iter.hasNext();
    }

    @Override
    public String[] next() {
      //TODO: log empty or irregular rows, setting rowError to true and populating errorMessage
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
          LOG.debug("Exception caught: " + e.getMessage(), e);
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
      LOG.error("Exception while reading excel source " + name, e);
    }
    return null;
  }

  /**
   * @return list of available sheets, keyed on sheet index
   */
  public Map<Integer, String> sheets() {
    Map<Integer, String> sheets = new HashMap<>();
    try (Workbook book = openBook()) {
      int cnt = book.getNumberOfSheets();
      for (int x = 0; x < cnt; x++) {
        sheets.put(x, book.getSheetName(x));
      }
    } catch (Exception e) {
      LOG.error("Exception while reading excel source " + name, e);
    }
    return sheets;
  }

  @Override
  public List<String> columns() {
    if (rows > 0) {
      try {
        if (ignoreHeaderLines > 0) {
          return new ArrayList<>(Arrays.asList(new RowIterator(this, ignoreHeaderLines - 1).next()));

        } else {
          List<String> columnList = new ArrayList<>();
          for (int x = 1; x <= columns; x++) {
            columnList.add("Column #" + x);
          }
          return columnList;
        }

      } catch (Exception e) {
        LOG.error("Exception while reading excel source " + name, e);
      }
    }

    // no rows, no columns
    return new ArrayList<>();
  }

  @Override
  public void setFile(File file) {
    this.file = file;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public void setIgnoreHeaderLines(Integer ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines == null ? 0 : ignoreHeaderLines;
  }

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public String getPreferredFileSuffix() {
    return SUFFIX;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  @Override
  public Set<Integer> analyze() throws IOException {
    setFileSize(getFile().length());
    // find row size
    try (Workbook book = openBook()) {
      Sheet sheet = getSheet(book);
      int physicalNumberOfRows = 0;

      Iterator<Row> iter = sheet.rowIterator();
      if (iter.hasNext()) {
        physicalNumberOfRows++;
        setColumns(iter.next().getLastCellNum());
        setReadable(true);
      } else {
        setColumns(0);
        setReadable(false);
      }

      while (iter.hasNext()) {
        physicalNumberOfRows++;
        iter.next();
      }

      setRows(physicalNumberOfRows);
    }

    //TODO: report empty or irregular rows
    return new HashSet<>();
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.EXCEL_FILE;
  }
}
