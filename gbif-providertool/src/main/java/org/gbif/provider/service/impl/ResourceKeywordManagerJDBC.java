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
package org.gbif.provider.service.impl;

import org.gbif.provider.service.ResourceKeywordManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceKeywordManagerJDBC extends BaseManagerJDBC implements
    ResourceKeywordManager {

  public List<String> getAlphabet() {
    String sql = "SELECT distinct upper(left(keywords_element,1)) FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 order by 1";
    return executeListAsString(sql);
  }

  public Map<String, Integer> getCloud() {
    String sql = "SELECT count(*), keywords_element FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 GROUP BY keywords_element order by 1 desc limit 1";
    double maxCnt = executeCount(sql);
    if (maxCnt < 1.0) {
      maxCnt = 1.0;
    }
    sql = String.format(
        "SELECT keywords_element, count(*)*%s FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 GROUP BY keywords_element order by 2 desc limit 50",
        9.0 / maxCnt);
    Map<String, Integer> map = executeMap(sql);
    List<String> keys = new ArrayList<String>(map.keySet());
    Collections.sort(keys);
    Map<String, Integer> sortedmap = new LinkedHashMap<String, Integer>();
    for (String k : keys) {
      sortedmap.put(k, map.get(k));
    }
    return sortedmap;
  }

  public List<String> getKeywords(String prefix) {
    String sql = "SELECT keywords_element FROM resource_keywords join resource res on resource_fk=res.id WHERE res.status>=1 and keywords_element like '"
        + prefix + "%' order by 1";
    return executeListAsString(sql);
  }
}
