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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * This class can be used to encapsulate information about a sampling method.
 * 
 * Note that this class is immuatable. New instances can be created using the
 * create method.
 * 
 */
public class Method implements Serializable {

  private static final long serialVersionUID = 2725055780405284137L;

  /**
   * Creates a new Method instance. Throws {@link NullPointerException} if any
   * of the arguments are null. Throws {@link IllegalArgumentException} if any
   * of the arguments are the empty string.
   * 
   * @param maintenance the maintenance
   * @param purpose the purpose
   * @param qualityControl the quality control
   * @param sampleDescription the sample description
   * @return new instance of Method
   */
  public static Method create(String maintenance, String purpose,
      String qualityControl, String sampleDescription) {
    checkNotNull(maintenance, "Maintenance was null");
    checkArgument(!maintenance.isEmpty(), "Maintenance was empty");
    checkNotNull(purpose, "Purpose was null");
    checkArgument(!purpose.isEmpty(), "Purpose was empty");
    checkNotNull(qualityControl, "Quality control was null");
    checkArgument(!qualityControl.isEmpty(), "Quality control was empty");
    checkNotNull(sampleDescription, "Sample description was null");
    checkArgument(!sampleDescription.isEmpty(), "Sample description was empty");
    return new Method(maintenance, purpose, qualityControl, sampleDescription);
  }

  private final String maintenance;
  private final String purpose;
  private final String qualityControl;
  private final String sampleDescription;

  private Method(String maintenance, String purpose, String qualityControl,
      String sampleDescription) {
    this.maintenance = maintenance;
    this.purpose = purpose;
    this.qualityControl = qualityControl;
    this.sampleDescription = sampleDescription;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Method)) {
      return false;
    }
    Method o = (Method) other;
    return equal(maintenance, o.maintenance) && equal(purpose, o.purpose)
        && equal(qualityControl, o.qualityControl)
        && equal(sampleDescription, o.sampleDescription);
  }

  public String getMaintenance() {
    return maintenance;
  }

  public String getPurpose() {
    return purpose;
  }

  public String getQualityControl() {
    return qualityControl;
  }

  public String getSampleDescription() {
    return sampleDescription;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(maintenance, purpose, qualityControl,
        sampleDescription);
  }

  @Override
  public String toString() {
    return String.format(
        "Maintenance=%s, Purpose=%s, QualityControl=%s, SampleDescription=%s",
        maintenance, purpose, qualityControl, sampleDescription);
  }
}
