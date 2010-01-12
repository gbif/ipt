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

/**
 * TODO: Documentation.
 * 
 */
public class Like extends ComparisonBinaryOperator {

  public Like() {
    log.debug("Creating " + this.getClass().getSimpleName());
  }

  @Override
  public void setValue(String value) {
    if (value != null) {
      // replace TAPIR wildcard with H2 wildcard
      value = value.replace("*", "%");
    }
    super.setValue(value);
  }

  @Override
  protected String getOperatorSymbol() {
    return "like";
  }
}
