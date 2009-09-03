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
package org.gbif.provider.tapir.filter;

import org.gbif.provider.tapir.ParseException;

import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public abstract class ComparisonBinaryOperator extends ComparisonOperator {
  protected String value;

  public String getValue() {
    return value;
  }

  /**
   * Sets the value to be the first value found from the params map with the
   * specified key
   * 
   * @param params the parameters to read from
   * @param key For the map
   * @throws ParseException
   */
  public void setValue(Map params, String key) throws ParseException {
    String val = (String) params.get(key);
    if (val != null) {
      setValue(val);
    } else {
      log.warn("No values in the parameters[" + params + "] for the key[" + key
          + "]");
      throw new ParseException("ERROR: No values for key[" + key + "]");
    }
  }

  public void setValue(String value) {
    log.debug("Setting value to: " + value);
    this.value = value;
  }

  @Override
  public String toHQL() {
    return String.format("%s %s '%s'", property.getHQLName(),
        getOperatorSymbol(), value);
  }

  @Override
  public String toString() {
    return String.format("%s %s '%s'", property.getQualName(),
        getOperatorSymbol(), value);
  }

}
