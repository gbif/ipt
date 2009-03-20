/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.util.Constants;

/**
 * External datasource driven resource representing a taxonomic checklist
 * @author markus
 *
 */
@Entity
public class ChecklistResource extends DataResource {
	private static final String DWC_GUID_PROPERTY = "TaxonID";
	public static final String DWC_GROUP = "Taxon";
	
	private int numCommonNames;
	private int numCommonNameLanguages;
	private int numDistributions;
	private int numDistributionRegions;
	
	public int getNumCommonNames() {
		return numCommonNames;
	}

	public void setNumCommonNames(int numCommonNames) {
		this.numCommonNames = numCommonNames;
	}

	public int getNumCommonNameLanguages() {
		return numCommonNameLanguages;
	}

	public void setNumCommonNameLanguages(int numCommonNameLanguages) {
		this.numCommonNameLanguages = numCommonNameLanguages;
	}

	public int getNumDistributions() {
		return numDistributions;
	}

	public void setNumDistributions(int numDistributions) {
		this.numDistributions = numDistributions;
	}

	public int getNumDistributionRegions() {
		return numDistributionRegions;
	}

	public void setNumDistributionRegions(int numDistributionRegions) {
		this.numDistributionRegions = numDistributionRegions;
	}

	@Override
	public void resetStats() {
		numCommonNameLanguages=0;
		numCommonNames=0;
		numDistributionRegions=0;
		numDistributions=0;
		super.resetStats();
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}

	@Override
	@Transient
	public String getDwcGuidPropertyName() {
		return DWC_GUID_PROPERTY;
	}

}
