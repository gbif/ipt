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
package org.gbif.provider.model.eml;

import org.gbif.provider.model.BBox;

import java.io.Serializable;

/**
 * TODO: Documentation.
 * 
 */
public class GeoKeyword implements Serializable {
  private String description;
  private BBox boundingCoordinates = BBox.newWorldInstance();

  public BBox getBoundingCoordinates() {
    return boundingCoordinates;
  }

  public String getDescription() {
    return description;
  }

  public void setBoundingCoordinates(BBox boundingCoordinates) {
    this.boundingCoordinates = boundingCoordinates;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
