package org.gbif.ipt.model;

import org.gbif.ipt.utils.FileUtils;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Uses apache POI to parse excel spreadsheets.
 * A single file can have multiple sheets which each act as a separate source.
 * The same file can therefore be used for multiple ExcelFileSource instances.
 * POI usage example, see http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/ss/examples/ToCSV.java
 */
public class ExcelFileSource extends SourceBase implements FileSource {

  private static final Logger LOG = Logger.getLogger(ExcelFileSource.class);
  private static final String SUFFIX = ".xls";

  private int sheetIdx = 0;
  private int ignoreHeaderLines = 0;
  private File file;
  private long fileSize;
  private int rows;
  protected Date lastModified;

  private String escape(String x) {
    if (x == null) {
      return null;
    }
    return x.replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r")
      .replaceAll("\\f", "\\\\f");
  }

  public File getFile() {
    return file;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getFileSizeFormatted() {
    return FileUtils.formatSize(fileSize, 1);
  }

  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  public void setIgnoreHeaderLines(int ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public int getSheetIdx() {
    return sheetIdx;
  }

  public void setSheetIdx(int sheetIdx) {
    this.sheetIdx = sheetIdx;
  }

  private Workbook openBook() throws IOException {
    LOG.info("Opening excel workbook [" + file.getName() + "]");
    try {
      FileInputStream fis = new FileInputStream(file);
      return WorkbookFactory.create(fis);
    } catch (InvalidFormatException e) {
      throw new IOException("Cannot open invalid excel speadsheet", e);
    }
  }

  private Sheet getSheet() throws IOException {
    Workbook book = openBook();
    return book.getSheetAt(sheetIdx);
  }

  public int getRows() {
    return rows;
  }

  private static String cellToString (Cell cell) {
    //TODO: deal with different types
    return cell.toString();
  }

  private class RowIterator implements ClosableReportingIterator<String[]> {

    private final Sheet sheet;  // 0 based
    private final Iterator<Row> iter;
    private final int rowSize;
    private boolean rowError;
    private String errorMessage;
    private Exception exception;

    RowIterator(ExcelFileSource source) throws IOException, InvalidFormatException {
      sheet = getSheet();
      iter = sheet.rowIterator();
      rowSize = source.getColumns();
    }

    RowIterator(ExcelFileSource source, int skipRows) throws IOException, InvalidFormatException {
      this(source);
      while (skipRows > 0) {
        iter.next();
        skipRows--;
      }
    }

    public void close() {
      // nothing to do
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public String[] next() {
      //TODO: log empty or irregular rows, setting rowError to true and populating errorMessage
      String[] val = new String[rowSize];
      if (hasNext()) {
        resetReportingIterator();
        try {
          Row row = iter.next();
          for (int i = 0; i < rowSize; i++) {
            Cell c = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
            val[i] = cellToString(c);
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

    public void remove() {
      // unsupported
    }

    public boolean hasRowError() {
      return rowError;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public Exception getException() {
      return exception;
    }
  }

  public ClosableReportingIterator<String[]> rowIterator() {
    try {
      return new RowIterator(this, ignoreHeaderLines);
    } catch (Exception e) {
      LOG.error("Exception while reading excel source "+ name, e);
    }
    return null;
  }

  /**
   * @return list of available sheets, keyed on sheet index
   */
  public Map<Integer, String> sheets() throws IOException {
    Workbook book = openBook();
    int cnt = book.getNumberOfSheets();
    Map<Integer, String> sheets = Maps.newHashMap();
    for (int x = 0; x < cnt; x++) {
      sheets.put(x, book.getSheetName(x));
    }
    return sheets;
  }

  public List<String> columns() {
    if (rows > 0) {
      try {
        if (ignoreHeaderLines > 0) {
          return Lists.newArrayList(new RowIterator(this, ignoreHeaderLines-1).next());

        } else {
          List<String> columnList = Lists.newArrayList();
          for (int x = 1; x <= columns; x++) {
            columnList.add("Column #" + x);
          }
          return columnList;
        }

      } catch (Exception e) {
        LOG.error("Exception while reading excel source "+ name, e);
      }
    }

    // no rows, no columns
    return Lists.newArrayList();
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public void setIgnoreHeaderLines(Integer ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines == null ? 0 : ignoreHeaderLines;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getPreferredFileSuffix() {
    return SUFFIX;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  public Set<Integer> analyze() throws IOException {
    setFileSize(getFile().length());
    // find row size
    Sheet sheet = getSheet();
    setRows(sheet.getPhysicalNumberOfRows());

    Iterator<Row> iter = sheet.rowIterator();
    if (iter.hasNext()) {
      setColumns(iter.next().getLastCellNum());
      setReadable(true);
    } else {
      setColumns(0);
      setReadable(false);
    }

    //TODO: report empty or irregular rows
    return Sets.newHashSet();
  }

  private String unescape(String x) {
    if (x == null) {
      return null;
    }
    return x.replaceAll("\\\\t", String.valueOf('\t')).replaceAll("\\\\n", String.valueOf('\n'))
      .replaceAll("\\\\r", String.valueOf('\r')).replaceAll("\\\\f", String.valueOf('\f'));
  }

}
