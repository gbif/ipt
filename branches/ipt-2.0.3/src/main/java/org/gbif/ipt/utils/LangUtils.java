/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author markus
 * 
 */
public class LangUtils {
  private final static Map<String, String> common3letterLangCodes = new HashMap<String, String>();
  static {
    common3letterLangCodes.put("eng", "en");
    common3letterLangCodes.put("fra", "fr");
    common3letterLangCodes.put("fre", "fr");
    common3letterLangCodes.put("deu", "de");
    common3letterLangCodes.put("ger", "de");
    common3letterLangCodes.put("spa", "es");
    common3letterLangCodes.put("ita", "it");
    common3letterLangCodes.put("por", "pt");
  }
  
  private final static Map<String, String> common2letterLangCodes = new HashMap<String, String>();
  static {
    common2letterLangCodes.put("en", "eng");
    common2letterLangCodes.put("fr", "fre");
    common2letterLangCodes.put("de", "ger");
    common2letterLangCodes.put("es", "spa");
    common2letterLangCodes.put("it", "ita");
    common2letterLangCodes.put("pt", "por");
  }

  public static String iso2(String language) {
    if (language != null && language.length() == 2) {
      return language;
    } else if (language != null && language.length() == 3) {
      return common3letterLangCodes.get(language.toLowerCase());
    }
    return null;
  }
  
  public static String iso3(String language) {
	    if (language != null && language.length() == 3) {
	      return language;
	    } else if (language != null && language.length() == 2) {
	      return common2letterLangCodes.get(language.toLowerCase());
	    }
	    return null;
	  }
}
