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
package org.gbif.ipt.model.factory;

import org.apache.commons.digester.CallParamRule;
import org.xml.sax.Attributes;

/**
 * Will not honor namespaces in attributes in any way
 * 
 * This class allows for getting access to attributes in elements where the
 * namespaces differ, but will ignore the namespace of the attribute
 * Additionally, it only allows for accessing a single attribute (since NS are
 * ignored) It would not be difficult to honor namespaces, but is not currently
 * needed.
 * 
 */
public class CallParamNoNSRule extends CallParamRule {
  /**
   * @param paramIndex The index to use (e.g. in the param list for a call
   *          method rule)
   * @param attributeName The local name of the attribute only
   */
  public CallParamNoNSRule(int paramIndex, String attributeName) {
    super(paramIndex, attributeName);
  }

  @Override
  public void begin(Attributes attributes) throws Exception {
    Object param = null;
    for (int i = 0; i < attributes.getLength(); i++) {

      // if it has no prefix, or has SOME prefix and ends in the attribute name
      // (___:attributeName)
      if (attributes.getQName(i).equals(attributeName)
          || attributes.getQName(i).endsWith(":" + attributeName)) {
        param = attributes.getValue(i);
        break;
      }
    }
    // add to the params stack
    if (param != null) {
      Object parameters[] = (Object[]) digester.peekParams();
      parameters[paramIndex] = param;
    }
  }
}
