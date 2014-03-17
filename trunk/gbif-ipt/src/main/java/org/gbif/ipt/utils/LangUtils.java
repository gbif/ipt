/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.utils;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Language utility class.
 */
public class LangUtils {

  private static final Map<String, String> COMMON_3LETTER_LANG_CODE = new ImmutableMap.Builder<String, String>()
    .put("eng", "en").put("fra", "fr").put("fre", "fr").put("deu", "de").put("ger", "de").put("spa", "es")
    .put("ita", "it").put("por", "pt").build();

  private static final Map<String, String> COMMON_2LETTER_LANG_CODE = new ImmutableMap.Builder<String, String>()
    .put("en", "eng").put("fr", "fre").put("de", "ger").put("es", "spa").put("it", "ita").put("pt", "por").build();


  private LangUtils() {
    // private constructor
  }

  public static String iso2(String language) {
    if (language != null && language.length() == 2) {
      return language;
    } else if (language != null && language.length() == 3) {
      return COMMON_3LETTER_LANG_CODE.get(language.toLowerCase());
    }
    return null;
  }

  public static String iso3(String language) {
    if (language != null && language.length() == 3) {
      return language;
    } else if (language != null && language.length() == 2) {
      return COMMON_2LETTER_LANG_CODE.get(language.toLowerCase());
    }
    return null;
  }
}
