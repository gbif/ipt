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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.model.dto.StatsCount;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DataResource {
	private static final String DWC_GUID_PROPERTY = "SampleID";
	private BBox bbox = new BBox();
	private Integer featureHash;
	// cached statistics
	private int recWithCoordinates;
	private int recWithCountry;
	private int recWithAltitude;
	private int recWithDate;
	// distinct number of Region entities
	private int numRegions;
	private int numTerminalRegions;
	private int numCountries;
	
	public BBox getBbox() {
		return bbox;
	}
	public void setBbox(BBox bbox) {
		this.bbox = bbox;
	}

	public int getRecWithCoordinates() {
		return recWithCoordinates;
	}

	public void setRecWithCoordinates(int recWithCoordinates) {
		this.recWithCoordinates = recWithCoordinates;
	}

	public int getRecWithCountry() {
		return recWithCountry;
	}

	public void setRecWithCountry(int recWithCountry) {
		this.recWithCountry = recWithCountry;
	}

	public int getRecWithAltitude() {
		return recWithAltitude;
	}

	public void setRecWithAltitude(int recWithAltitude) {
		this.recWithAltitude = recWithAltitude;
	}

	public int getRecWithDate() {
		return recWithDate;
	}

	public void setRecWithDate(int recWithDate) {
		this.recWithDate = recWithDate;
	}


	public int getNumCountries() {
		return numCountries;
	}

	public void setNumCountries(int numCountries) {
		this.numCountries = numCountries;
	}
	
	public int getNumRegions() {
		return numRegions;
	}

	public void setNumRegions(int numRegions) {
		this.numRegions = numRegions;
	}

	public int getNumTerminalRegions() {
		return numTerminalRegions;
	}

	public void setNumTerminalRegions(int numTerminalRegions) {
		this.numTerminalRegions = numTerminalRegions;
	}

	public Integer getFeatureHash() {
		return featureHash;
	}

	public void setFeatureHash(Integer featureHash) {
		this.featureHash = featureHash;
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}


	@Override
	public void resetStats() {
		bbox = new BBox();
		recWithCoordinates=0;
		recWithCountry=0;
		recWithAltitude=0;
		recWithDate=0;
		numCountries=0;
		numRegions=0;
		numTerminalRegions=0;
		
		super.resetStats();
	}
	
	@Override
	@Transient
	public String getDwcGuidPropertyName() {
		return DWC_GUID_PROPERTY;
	}
}
