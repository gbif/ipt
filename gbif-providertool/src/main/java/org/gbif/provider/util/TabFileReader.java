/*
 * Copyright 2009 GBIF.
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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple tab file reader that iterates through a tab file and returns row by
 * row and assures all rows have the same number of columns. Will throw a
 * MalformedTabFileException otherwise. The header is not returned as a regular
 * row through the iterator, but can be retrieved via the getHeader() method at
 * any time.
 * 
 */
/**
 * TODO: Documentation.
 * 
 */
public class TabFileReader implements Iterator<String[]> {
  private static Pattern tabPattern = Pattern.compile("\t");
  private final Log log = LogFactory.getLog(TabFileReader.class);
  private final LineIterator it;
  private final File file;
  private String[] header;
  private boolean returnHeaderRow;
  private String separator;

  public TabFileReader(File file, boolean returnHeaderRow) throws IOException,
      MalformedTabFileException {
    this.it = FileUtils.lineIterator(file, "UTF-8");
    this.file = file;
    // read header
    this.returnHeaderRow = returnHeaderRow;
    if (it.hasNext()) {
      String line = it.nextLine();
      header = tabPattern.split(line);
    } else {
      throw new MalformedTabFileException();
    }
  }

  /**
   * @param source
   * @param b
   * @param separator
   * @throws IOException
   * @throws MalformedTabFileException
   */
  public TabFileReader(File file, boolean returnHeaderRow, String separator)
      throws IOException, MalformedTabFileException {
    this.it = FileUtils.lineIterator(file, "UTF-8");
    this.file = file;
    this.separator = separator;
    // read header
    this.returnHeaderRow = returnHeaderRow;
    if (it.hasNext()) {
      String line = it.nextLine();
      header = lineAsArray(line, separator);
    } else {
      throw new MalformedTabFileException();
    }
  }

  public void close() throws IOException {
    LineIterator.closeQuietly(it);
  }

  public File getFile() {
    return file;
  }

  public String[] getHeader() {
    return header;
  }

  public boolean hasNext() {
    return returnHeaderRow || it.hasNext();
  }

  public String[] next() {
    String[] columns;
    if (returnHeaderRow) {
      columns = header;
      returnHeaderRow = false;
    } else {
      String line = it.nextLine();
      columns = lineAsArray(line, separator);
    }
    if (columns.length > header.length) {
      List<String> cl = Arrays.asList(columns);
      cl.subList(0, header.length - 1);
      log.warn("row contains more columns than header");
      columns = cl.toArray(new String[header.length - 1]);
    } else if (columns.length < header.length) {
      // pad missing columns with nulls
      columns = (String[]) ArrayUtils.addAll(columns, new String[header.length
          - columns.length]);
    }
    return columns;
  }

  public void remove() {
    it.remove();
  }

  /**
   * @param line
   * @param separator
   * @return String[]
   */
  private String[] lineAsArray(String line, String separator) {
    List<String> list = Lists.newArrayList(Splitter.on(separator).trimResults().split(
        line));
    String[] array = new String[list.size()];
    int i = 0;
    for (String s : list) {
      array[i++] = s;
    }
    return array;
  }

}
