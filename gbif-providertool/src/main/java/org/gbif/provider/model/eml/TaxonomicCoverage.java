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
import java.util.Set;

/**
 * This class can be used to encapsulate taxonomic coverage information.
 * 
 * Note that this class is immuatable. New instances can be created using the
 * create method.
 * 
 */
public class TaxonomicCoverage implements Serializable {

  private static final long serialVersionUID = 6440261277409707797L;

  /**
   * Creates a new instance of TaxonomicCoverage. Throws
   * {@link NullPointerException} if the description argument is null. Throws
   * {@link IllegalArgumentException} if the description argument is empty.
   * 
   * @param description the description
   * @param idReference the identification reference
   * @param keywords the keywords
   * @return a new instance of TaxonomicCoverage
   */
  public static TaxonomicCoverage create(String description,
      String idReference, Set<TaxonKeyword> keywords) {
    checkNotNull(description, "Description was null");
    checkArgument(!description.isEmpty(), "Description was empty");
    ImmutableSet<TaxonKeyword> kw;
    if (keywords == null) {
      kw = ImmutableSet.of();
    } else {
      kw = ImmutableSet.copyOf(keywords);
    }
    return new TaxonomicCoverage(description, idReference, kw);
  }

  private final String description;
  private final String idReference;
  private final ImmutableSet<TaxonKeyword> keywords;

  private TaxonomicCoverage(String description, String idReference,
      ImmutableSet<TaxonKeyword> keywords) {
    this.description = description;
    this.idReference = idReference;
    this.keywords = keywords;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TaxonomicCoverage)) {
      return false;
    }
    TaxonomicCoverage o = (TaxonomicCoverage) other;
    return equal(description, o.description)
        && equal(idReference, o.idReference) && equal(keywords, o.keywords);
  }

  public String getDescription() {
    return description;
  }

  public String getIdReference() {
    return idReference;
  }

  public ImmutableSet<TaxonKeyword> getKeywords() {
    return keywords;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, idReference, keywords);
  }

  @Override
  public String toString() {
    return String.format("Description=%s, IdReference=%s, Keywords=[%s]",
        description, idReference, keywords);
  }
}