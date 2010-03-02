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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Objects;

/**
 * This class can be used to encapsulate temporal coverage information.
 */
public class TemporalCoverage implements Serializable {
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 898101764914677290L;

	/**
	 * A single time stamp signifying the beginning of some time period. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#beginDate
	 */
	private Date startDate;

	/**
	 * A single time stamp signifying the end of some time period. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#endDate
	 */
	private Date endDate;

	/**
	 * Text description of the time period during which the collection was assembled e.g. "Victorian", 
	 * or "1922 - 1932", or "c. 1750". 
	 * @see http://rs.tdwg.org/ontology/voc/Collection#formationPeriod 
	 */
	private String formationPeriod;

	/**
	 * Time period during which biological material was alive. (for palaeontological collections).
	 * @see http://rs.tdwg.org/ontology/voc/Collection#livingTimePeriodCoverage 
	 */
	private String livingTimePeriod;

	/**
	 * Required by Struts2
	 */
	public TemporalCoverage() {
	}

	/**
	 * Utility to set the date with a textual format
	 * @param dateString To set
	 * @param format That the string is in
	 * @throws ParseException Should it be an erroneous format
	 */
	public void setStart(String start, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		startDate = sdf.parse(start);
	}

	/**
	 * Utility to set the date with a textual format
	 * @param dateString To set
	 * @param format That the string is in
	 * @throws ParseException Should it be an erroneous format
	 */
	public void setEnd(String start, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		endDate = sdf.parse(start);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TemporalCoverage)) {
			return false;
		}
		TemporalCoverage o = (TemporalCoverage) other;
		return equal(formationPeriod, o.formationPeriod) && equal(endDate, o.endDate) && equal(livingTimePeriod, o.livingTimePeriod) && equal(startDate, o.startDate);
	}

	public Date getEndDate() {
		return new Date(endDate.getTime());
	}

	public Date getStartDate() {
		return new Date(startDate.getTime());
	}

	public String getFormationPeriod() {
		return formationPeriod;
	}

	public void setFormationPeriod(String formationPeriod) {
		this.formationPeriod = formationPeriod;
	}

	public String getLivingTimePeriod() {
		return livingTimePeriod;
	}

	public void setLivingTimePeriod(String livingTimePeriod) {
		this.livingTimePeriod = livingTimePeriod;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(livingTimePeriod, endDate, formationPeriod, startDate);
	}

	@Override
	public String toString() {
		return String.format("Descriptioin=%s, EndDate=%s, Keywords=%s, StartDate=%s", formationPeriod, endDate, livingTimePeriod, startDate);
	}
}
