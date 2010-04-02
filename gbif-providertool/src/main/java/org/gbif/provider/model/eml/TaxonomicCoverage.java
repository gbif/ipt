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

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The description of the Taxonomic scope that the resource covers 
 */
public class TaxonomicCoverage implements Serializable {
	/**
	 * Generated 
	 */
	private static final long serialVersionUID = -1550877218411220807L;
	
	/**
	 * A description of the range of taxa addressed in the data set or collection
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#generalTaxonomicCoverage
	 */
	private String description;
	
	/**
	 * Structures keywords for coverage
	 */
	private List<TaxonKeyword> keywords = Lists.newArrayList();

	/**
	 * Required for struts2 params-interceptor, Digester and deserializing from XML
	 */
	public TaxonomicCoverage() {
	}

	/**
	 * Utility to add a keyword to the encapsulated keywords
	 * @param keyword to add
	 */
	public void addTaxonKeyword(TaxonKeyword keyword) {
		keywords.add(keyword);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the keywords
	 */
	public List<TaxonKeyword> getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(List<TaxonKeyword> keywords) {
		this.keywords = keywords;
	}
}