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

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.SourceBase;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public interface SourceInspectionManager {
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

  /**
   * @param source source either to file or SQL statement
   * @return list of column headers only (same first row in getPreview())
   * @throws Exception
   */
  List<String> getHeader(SourceBase source) throws Exception;

  /**
   * @param source either to file or SQL statement.
   * @return a list of PREVIEW_SIZE (~5) rows plus a first header row of strings
   *         that contains the column names as TABLE.COLUMNNAME
   * @throws Exception
   */
  List<List<? extends Object>> getPreview(SourceBase source) throws Exception;
}
