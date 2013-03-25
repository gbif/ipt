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

import org.gbif.provider.model.dto.StatsCount;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class StatsUtils {
  public static List<StatsCount> getDataMap(List<Object[]> idValueCountRows) {
    List<StatsCount> data = new ArrayList<StatsCount>();
    for (Object[] row : idValueCountRows) {
      Long id = null;
      Object value;
      Long count;
      if (row.length == 2) {
        value = row[0];
        count = (Long) row[1];
      } else {
        id = (Long) row[0];
        value = row[1];
        try {
          count = (Long) row[2];
        } catch (ClassCastException e) {
          count = Long.valueOf(row[2].toString());
        }
      }
      String label = null;
      if (value != null) {
        label = value.toString();
      }
      if (StringUtils.trimToNull(label) == null) {
        label = "?";
      }
      data.add(new StatsCount(id, label, value, count));
    }
    // sort data
    Collections.sort(data);
    return data;
  }
}
