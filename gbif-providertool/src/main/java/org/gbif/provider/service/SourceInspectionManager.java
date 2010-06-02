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
package org.gbif.provider.service;

import com.google.common.collect.ImmutableList;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.SourceBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public interface SourceInspectionManager {

  /**
   * An interface that specifies a file header.
   */
  public static interface HeaderSpec {

    /**
     * Returns the character that separates fields values in the header, such as
     * ',', '\t', '|', etc.
     */
    char getFieldSeparatorChar();

    /**
     * Returns the file containing the specified header.
     */
    File getFile();

    /**
     * Returns the file character encoding.
     */
    Charset getFileEncoding();

    /**
     * Returns the number of lines to skip, starting with the first line. For
     * example, if a file has a header and getNumberOfLinesToSkip is 9, then the
     * 10th line in the file would be processed as the header line.
     * 
     * @return int
     */
    int getNumberOfLinesToSkip();

    /**
     * Returns true if a header exists in the file, and false otherwise. If a
     * header doesn't exist, a generated header of the form Column-00i will be
     * generated.
     */
    boolean headerExists();
  }

  List<String> getAllTables(DataResource resource) throws SQLException;

  /**
   * scans a single column of a source and returns the list of distinct values
   * found.
   * 
   * @param source
   * @param column
   * @return
   * @throws Exception
   */
  Set<String> getDistinctValues(SourceBase source, String column)
      throws Exception;

  // ImmutableList<String> getHeader(File file, Charset encoding, char
  // separator,
  // boolean declaredHeader) throws IOException;

  ImmutableList<String> getHeader(HeaderSpec spec) throws IOException;

  /**
   * @param sourceBase source either to file or SQL statement
   * @return list of column headers only (same first row in getPreview())
   * @throws Exception
   */
  List<String> getHeader(SourceBase sourceBase) throws Exception;

  /**
   * @param source either to file or SQL statement.
   * @return a list of PREVIEW_SIZE (~5) rows plus a first header row of strings
   *         that contains the column names as TABLE.COLUMNNAME
   * @throws Exception
   */
  List<List<? extends Object>> getPreview(SourceBase source) throws Exception;
}
