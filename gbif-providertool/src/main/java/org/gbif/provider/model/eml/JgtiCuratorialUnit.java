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
 * This class can be used to encapsulate information about a JGTI curatorial
 * unit.
 * 
 * Note that this class is immutable. New instances can be created using the
 * create method.
 * 
 */
public class JgtiCuratorialUnit implements Serializable {

  private static final long serialVersionUID = 4302214747473277031L;

  /**
   * Creates a new JgtiCuratorialUnit instance. Throws
   * {@link NullPointerException} if any of the arguments are null. Throws
   * {@link IllegalArgumentException} if rangeStart, rangeEnd, or
   * uncertaintyMeasure is the empty string.
   * 
   * @param unit the unit
   * @param rangeStart the range start
   * @param rangeEnd the range end
   * @param uncertaintyMeasure the uncertainty measure
   * @return new instance of JgtiCuratorialUnit
   */
  public static JgtiCuratorialUnit create(String rangeEnd, String rangeStart,
      String uncertaintyMeasure, Integer unit) {
    checkNotNull(rangeStart, "Range start was null");
    checkArgument(!rangeStart.isEmpty(), "Range start was empty");
    checkNotNull(rangeEnd, "Range end was null");
    checkArgument(!rangeEnd.isEmpty(), "Range end was empty");
    checkNotNull(uncertaintyMeasure, "Uncertainty measure was null");
    checkArgument(!uncertaintyMeasure.isEmpty(),
        "Uncertainty measure was empty");
    checkNotNull(unit, "Unit was null");
    return new JgtiCuratorialUnit(unit, rangeStart, rangeEnd,
        uncertaintyMeasure);
  }

  private final String rangeEnd;
  private final String rangeStart;
  private final String uncertaintyMeasure;
  private final Integer unit;

  private JgtiCuratorialUnit(int unit, String rangeEnd, String rangeStart,
      String uncertaintyMeasure) {
    this.rangeEnd = rangeEnd;
    this.rangeStart = rangeStart;
    this.uncertaintyMeasure = uncertaintyMeasure;
    this.unit = unit;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof JgtiCuratorialUnit)) {
      return false;
    }
    JgtiCuratorialUnit o = (JgtiCuratorialUnit) other;
    return equal(rangeEnd, o.rangeEnd) && equal(rangeStart, o.rangeStart)
        && equal(uncertaintyMeasure, o.uncertaintyMeasure)
        && equal(unit, o.unit);
  }

  public String getRangeEnd() {
    return rangeEnd;
  }

  public String getRangeStart() {
    return rangeStart;
  }

  public String getUncertaintyMeasure() {
    return uncertaintyMeasure;
  }

  public Integer getUnit() {
    return unit;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rangeEnd, rangeStart, uncertaintyMeasure, unit);
  }

  @Override
  public String toString() {
    return String.format(
        "RangeEnd=%s, RangeStart=%s, UncertaintyMeasure=%s, Unit=%d", rangeEnd,
        rangeStart, uncertaintyMeasure, unit);
  }
}
