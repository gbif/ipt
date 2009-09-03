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

import org.gbif.provider.model.TermMapping;
import org.gbif.provider.service.TermMappingManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class TermMappingManagerHibernate extends
    GenericManagerHibernate<TermMapping> implements TermMappingManager {

  public TermMappingManagerHibernate() {
    super(TermMapping.class);
  }

  public Map<String, String> getMappingMap(Long transformationId) {
    Map<String, String> map = new HashMap<String, String>();
    if (transformationId != null) {
      List<Object[]> terms = query(
          "select tm.term, tm.targetTerm from TermMapping tm WHERE tm.transformation.id=:transformationId").setLong(
          "transformationId", transformationId).list();
      for (Object[] m : terms) {
        map.put((String) m[0], (String) m[1]);
      }
    }
    return map;
  }

  public List<TermMapping> getTermMappings(Long transformationId) {
    return query(
        "select tm from TermMapping tm WHERE tm.transformation.id=:transformationId ORDER by tm.term").setLong(
        "transformationId", transformationId).list();
  }

}
