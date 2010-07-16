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
package org.gbif.ipt.model.eml;

import java.io.Serializable;
import java.util.Date;

/**
 * TODO: Documentation.
 * 
 */
public class TimeKeyword implements Serializable {
  private Date start;
  private Date end;

  public Date getEnd() {
    return end;
  }

  public Date getStart() {
    return start;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public void setStart(Date start) {
    this.start = start;
  }

}
