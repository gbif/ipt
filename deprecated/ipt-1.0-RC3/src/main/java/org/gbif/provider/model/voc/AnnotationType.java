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
public enum AnnotationType {
  Resource, WrongDatatype, BadReference, BadCoreRecord, BadExtensionRecord, UnknownVocTerm, TrimmedData, HumanComment, AmbigousTaxon, BadPointer;

  public static final Map<String, String> htmlSelectMap;
  static {
    Map<String, String> map = new HashMap<String, String>();
    for (AnnotationType et : AnnotationType.values()) {
      map.put(et.toString(), "annotationType." + et.toString());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }
}
