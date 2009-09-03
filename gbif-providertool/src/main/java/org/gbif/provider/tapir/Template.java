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
package org.gbif.provider.tapir;

import org.gbif.provider.tapir.filter.Filter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class Template {
  // searchTemplate or inventoryTemplate
  private TapirOperation operation;
  // */filter
  private Filter filter;
  // key = inventoryTemplate/concepts/concept/@id
  // value = inventoryTemplate/concepts/concept/@tagName
  private Map<String, String> concepts = new LinkedHashMap<String, String>();
  // key = searchTemplate/orderBy/concept/@id
  // value = searchTemplate/orderBy/concept/@descend
  private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();
  // only for searches
  // searchTemplate/externalOutputModel/@location
  private String model;

  /*
   * public void setOperation(String operationAsString) { if
   * ("PING".equalsIgnoreCase(operationAsString)) {
   * setOperation(TapirOperation.ping); } else if
   * ("CAPABILITIES".equalsIgnoreCase(operationAsString)) {
   * setOperation(TapirOperation.capabilities); } else if
   * ("METADATA".equalsIgnoreCase(operationAsString)) {
   * setOperation(TapirOperation.metadata); } else if
   * ("INVENTORY".equalsIgnoreCase(operationAsString)) {
   * setOperation(TapirOperation.inventory); } else if
   * ("SEARCH".equalsIgnoreCase(operationAsString)) {
   * setOperation(TapirOperation.search); } }
   */
  public Map<String, String> getConcepts() {
    return concepts;
  }

  public Filter getFilter() {
    return filter;
  }

  public String getModel() {
    return model;
  }

  public TapirOperation getOperation() {
    return operation;
  }

  public Map<String, Boolean> getOrderBy() {
    return orderBy;
  }

  public void setConcepts(Map<String, String> concepts) {
    this.concepts = concepts;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setOperation(TapirOperation operation) {
    this.operation = operation;
  }

  // required by Digester based parsing
  public void setOrderByStringMap(Map<String, String> orderBy) {
    this.orderBy = new LinkedHashMap<String, Boolean>();
    for (String key : orderBy.keySet()) {
      if ("TRUE".equalsIgnoreCase(orderBy.get(key))) {
        this.orderBy.put(key, true);
      } else {
        this.orderBy.put(key, false);
      }
    }
  }
}
