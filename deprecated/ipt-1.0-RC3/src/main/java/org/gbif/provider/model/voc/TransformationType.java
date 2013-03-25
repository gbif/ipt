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
package org.gbif.provider.model.voc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public enum TransformationType {
  Union("Union of columns"), Hierarchy("Hierarchy normalisation"), Lookup(
      "ID lookup"), Vocabulary("Vocabulary translation"), Sql("SQL view");

  public static final Map<Integer, String> htmlSelectMap;
  static {
    Map<Integer, String> map = new HashMap<Integer, String>();
    for (TransformationType tt : TransformationType.values()) {
      map.put(tt.ordinal(), tt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }
  public String verbatim;

  private TransformationType(String verbatim) {
    this.verbatim = verbatim;
  }

}
