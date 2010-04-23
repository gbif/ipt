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

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Encapsulates all the information for a StudyAreaDescription
 */
public class StudyAreaDescription implements Serializable {

  /**
   *  Generated
   */
  private static final long serialVersionUID = -625087801176596735L;
  private StudyAreaDescriptor name;
  private String citableClassificationSystem="false";
  private String descriptorValue;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StudyAreaDescription)) {
      return false;
    }
    StudyAreaDescription o = (StudyAreaDescription) other;
    return equal(name, o.name) && 
      equal(citableClassificationSystem, o.citableClassificationSystem) && 
      equal(descriptorValue, o.descriptorValue);
  }

  public StudyAreaDescriptor getName() {
    return name;
  }

  public String getCitableClassificationSystem() {
    return citableClassificationSystem;
  }

  public String getDescriptorValue() {
    return descriptorValue;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, citableClassificationSystem, descriptorValue);
  }

  public void setName(StudyAreaDescriptor name) {
    this.name = name;
  }

  public void setName(String nameStr) {
    name = StudyAreaDescriptor.fromString(nameStr);
  }

  public void setCitableClassificationSystem(String citableClassificationSystem) {
    this.citableClassificationSystem = citableClassificationSystem;
  }

  public void setDescriptorValue(String descriptorValue) {
    this.descriptorValue = descriptorValue;
  }

  @Override
  public String toString() {
    return String.format("Name=%s, CitableClassificationSystem=%s, DescriptorValue=%s", name, citableClassificationSystem, descriptorValue);
  }

}