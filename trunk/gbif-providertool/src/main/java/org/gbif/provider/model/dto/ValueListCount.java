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
package org.gbif.provider.model.dto;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class ValueListCount {
  private List<Object> values;
  private Long count;

  public ValueListCount(List<Object> values) {
    super();
    this.values = values;
  }

  public ValueListCount(Long count, List<Object> values) {
    super();
    this.values = values;
    this.count = count;
  }

  public Long getCount() {
    return count;
  }

  public List<Object> getValues() {
    return values;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public void setValues(List<Object> values) {
    this.values = values;
  }

}
