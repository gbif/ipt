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
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * This class can be used to encapsulate temporal converage information.
 * 
 * Note that this class is immutable. New instances can be created using the
 * create method.
 * 
 */
public class TemporalCoverage implements Serializable {

  private static final long serialVersionUID = 898101764914677290L;

  /**
   * Creates a new instance of TemporalCovereage. Throws
   * {@link NullPointerException} if the description, endDate, or startDate
   * arguments are null. Throws an {@link IllegalArgumentException} if the
   * description argument is the empty string.
   * 
   * @param description the description
   * @param endDate the end date
   * @param keywords the keywords
   * @param startDate the start date
   * @return new instance of TemporalConverage
   */
  public static TemporalCoverage create(String description, Date endDate,
      Set<String> keywords, Date startDate) {
    checkNotNull(description, "Description was null");
    checkArgument(!description.isEmpty(), "Description was empty");
    checkNotNull(endDate, "EndDate was null");
    checkNotNull(startDate, "StartDate was null");
    ImmutableSet<String> kw;
    if (keywords == null) {
      kw = ImmutableSet.of();
    } else {
      kw = ImmutableSet.copyOf(keywords);
    }
    return new TemporalCoverage(description, endDate, kw, startDate);
  }

  private final String description;
  private final Date endDate;
  private final ImmutableSet<String> keywords;
  private final Date startDate;

  private TemporalCoverage(String description, Date endDate,
      ImmutableSet<String> keywords, Date startDate) {
    this.description = description;
    this.endDate = endDate;
    this.keywords = keywords;
    this.startDate = startDate;
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
    return equal(description, o.description) && equal(endDate, o.endDate)
        && equal(keywords, o.keywords) && equal(startDate, o.startDate);
  }

  public String getDescription() {
    return description;
  }

  public Date getEndDate() {
    return new Date(endDate.getTime());
  }

  public ImmutableSet<String> getKeywords() {
    return keywords;
  }

  public Date getStartDate() {
    return new Date(startDate.getTime());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, endDate, keywords, startDate);
  }

  @Override
  public String toString() {
    return String.format(
        "Descriptioin=%s, EndDate=%s, Keywords=%s, StartDate=%s", description,
        endDate, keywords, startDate);
  }
}
