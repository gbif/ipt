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

import org.h2.tools.Csv;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * TODO: Documentation.
 * 
 */
public class H2Csv {

  @Test
  public void testH2Csv() throws SQLException {
    ResultSet rs = Csv.getInstance().read("/Users/markus/Desktop/rank.csv",
        null, null);
    ResultSetMetaData meta = rs.getMetaData();
    while (rs.next()) {
      for (int i = 0; i < meta.getColumnCount(); i++) {
        System.out.println(meta.getColumnLabel(i + 1) + ": "
            + rs.getString(i + 1));
      }
      System.out.println();
    }
    rs.close();
  }
}
