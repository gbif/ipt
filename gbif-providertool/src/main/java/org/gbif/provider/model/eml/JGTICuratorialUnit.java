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

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * This class can be used to encapsulate information about a curatorial unit.
 */
public class JGTICuratorialUnit implements Serializable {
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 4302214747473277031L;

	private String rangeEnd;
	private String rangeStart;
	private String uncertaintyMeasure;
	private Integer unit;
	
	/**
	 * Required by Struts2
	 */
	public JGTICuratorialUnit() {
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JGTICuratorialUnit)) {
			return false;
		}
		JGTICuratorialUnit o = (JGTICuratorialUnit) other;
		return equal(rangeEnd, o.rangeEnd) && equal(rangeStart, o.rangeStart) && equal(uncertaintyMeasure, o.uncertaintyMeasure) && equal(unit, o.unit);
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
	
	public void setRangeEnd(String rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	public void setUncertaintyMeasure(String uncertaintyMeasure) {
		this.uncertaintyMeasure = uncertaintyMeasure;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(rangeEnd, rangeStart, uncertaintyMeasure, unit);
	}

	@Override
	public String toString() {
		return String.format("RangeEnd=%s, RangeStart=%s, UncertaintyMeasure=%s, Unit=%d", rangeEnd, rangeStart, uncertaintyMeasure, unit);
	}
}
