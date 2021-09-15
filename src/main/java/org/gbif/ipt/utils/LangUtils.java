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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Language utility class.
 */
public class LangUtils {

  private static final Map<String, String> COMMON_3LETTER_LANG_CODE;
  private static final Map<String, String> COMMON_2LETTER_LANG_CODE;

  static {
    Map<String, String> common3letterLangCodeInternal = new HashMap<>();
    common3letterLangCodeInternal.put("eng", "en");
    common3letterLangCodeInternal.put("fra", "fr");
    common3letterLangCodeInternal.put("fre", "fr");
    common3letterLangCodeInternal.put("deu", "de");
    common3letterLangCodeInternal.put("ger", "de");
    common3letterLangCodeInternal.put("spa", "es");
    common3letterLangCodeInternal.put("ita", "it");
    common3letterLangCodeInternal.put("por", "pt");
    COMMON_3LETTER_LANG_CODE = Collections.unmodifiableMap(common3letterLangCodeInternal);

    Map<String, String> common2letterLangCodeInternal = new HashMap<>();
    common2letterLangCodeInternal.put("en", "eng");
    common2letterLangCodeInternal.put("fr", "fre");
    common2letterLangCodeInternal.put("de", "ger");
    common2letterLangCodeInternal.put("es", "spa");
    common2letterLangCodeInternal.put("it", "ita");
    common2letterLangCodeInternal.put("pt", "por");
    COMMON_2LETTER_LANG_CODE = Collections.unmodifiableMap(common2letterLangCodeInternal);
  }


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
