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

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class In extends ComparisonOperator {
  private final List<String> literals = new LinkedList<String>();

  public In() {
    log.debug("Creating " + this.getClass().getSimpleName());
  }

  public void addValue(String value) {
    literals.add("'" + value + "'");
  }

  /**
   * Another hack for how we use digester
   */
  public void setValue(String value) {
    literals.add("'" + value + "'");
  }

  @Override
  public String toHQL() {
    return String.format("%s %s (%s)", property.getHQLName(),
        getOperatorSymbol(), StringUtils.join(literals, ","));
  }

  @Override
  public String toString() {
    return String.format("%s %s (%s)", property.getQualName(),
        getOperatorSymbol(), StringUtils.join(literals, ","));
  }

  @Override
  protected String getOperatorSymbol() {
    return "in";
  }
}
