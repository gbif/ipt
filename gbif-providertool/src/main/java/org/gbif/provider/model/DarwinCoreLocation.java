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
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.appfuse.model.BaseObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
public class DarwinCoreLocation {
	private Long id;
	private DarwinCore dwc;
	// derived typed properties
	private Integer minimumElevationInMetersAsInteger;
	private Integer maximumElevationInMetersAsInteger;
	private Integer minimumDepthInMetersAsInteger;
	private Integer maximumDepthInMetersAsInteger;

	// Locality Elements
	private String higherGeography;
	private String continent;
	private String waterBody;
	private String islandGroup;
	private String island;
	private String country;
	private String stateProvince;
	private String county;
	private String locality;
	private String minimumElevationInMeters;
	private String maximumElevationInMeters;
	private String minimumDepthInMeters;
	private String maximumDepthInMeters;
	// Collecting Event Elements	
	private String collectingMethod;
	private String validDistributionFlag;
	private String earliestDateCollected;
	private String latestDateCollected;
	private String dayOfYear;
	private String collector;	
	
	@Id @GeneratedValue(generator="dwcidloc")
	@GenericGenerator(name="dwcidloc", strategy = "foreign", 
			parameters={@Parameter (name="property", value = "dwc")}
	)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne
	public DarwinCore getDwc() {
		return dwc;
	}
	public void setDwc(DarwinCore dwc) {
		this.dwc = dwc;
	}
	
	
	public String getHigherGeography() {
		return higherGeography;
	}
	public void setHigherGeography(String higherGeography) {
		this.higherGeography = higherGeography;
	}
	@Column(length = 128)
	public String getContinent() {
		return continent;
	}
	public void setContinent(String continent) {
		this.continent = continent;
	}
	@Column(length = 255)
	public String getWaterBody() {
		return waterBody;
	}
	public void setWaterBody(String waterBody) {
		this.waterBody = waterBody;
	}
	@Column(length = 255)
	public String getIslandGroup() {
		return islandGroup;
	}
	public void setIslandGroup(String islandGroup) {
		this.islandGroup = islandGroup;
	}
	@Column(length = 255)
	public String getIsland() {
		return island;
	}
	public void setIsland(String island) {
		this.island = island;
	}
	@Column(length = 128)
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	@Column(length = 128)
	public String getStateProvince() {
		return stateProvince;
	}
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	@Column(length = 255)
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	@Lob
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}

	public Integer getMinimumElevationInMetersAsInteger() {
		return minimumElevationInMetersAsInteger;
	}
	public void setMinimumElevationInMetersAsInteger(
			Integer minimumElevationInMetersAsInteger) {
		this.minimumElevationInMetersAsInteger = minimumElevationInMetersAsInteger;
	}
	public Integer getMaximumElevationInMetersAsInteger() {
		return maximumElevationInMetersAsInteger;
	}
	public void setMaximumElevationInMetersAsInteger(
			Integer maximumElevationInMetersAsInteger) {
		this.maximumElevationInMetersAsInteger = maximumElevationInMetersAsInteger;
	}
	public Integer getMinimumDepthInMetersAsInteger() {
		return minimumDepthInMetersAsInteger;
	}
	public void setMinimumDepthInMetersAsInteger(
			Integer minimumDepthInMetersAsInteger) {
		this.minimumDepthInMetersAsInteger = minimumDepthInMetersAsInteger;
	}
	public Integer getMaximumDepthInMetersAsInteger() {
		return maximumDepthInMetersAsInteger;
	}
	public void setMaximumDepthInMetersAsInteger(
			Integer maximumDepthInMetersAsInteger) {
		this.maximumDepthInMetersAsInteger = maximumDepthInMetersAsInteger;
	}
	@Column(length = 32)
	public String getMinimumElevationInMeters() {
		return minimumElevationInMeters;
	}
	public void setMinimumElevationInMeters(String minimumElevationInMeters) {
		this.minimumElevationInMeters = minimumElevationInMeters;
	}
	@Column(length = 32)
	public String getMaximumElevationInMeters() {
		return maximumElevationInMeters;
	}
	public void setMaximumElevationInMeters(String maximumElevationInMeters) {
		this.maximumElevationInMeters = maximumElevationInMeters;
	}
	@Column(length = 32)
	public String getMinimumDepthInMeters() {
		return minimumDepthInMeters;
	}
	public void setMinimumDepthInMeters(String minimumDepthInMeters) {
		this.minimumDepthInMeters = minimumDepthInMeters;
	}
	@Column(length = 32)
	public String getMaximumDepthInMeters() {
		return maximumDepthInMeters;
	}
	public void setMaximumDepthInMeters(String maximumDepthInMeters) {
		this.maximumDepthInMeters = maximumDepthInMeters;
	}
	@Column(length = 255)
	public String getCollectingMethod() {
		return collectingMethod;
	}
	public void setCollectingMethod(String collectingMethod) {
		this.collectingMethod = collectingMethod;
	}
	@Column(length = 16)
	public String getValidDistributionFlag() {
		return validDistributionFlag;
	}
	public void setValidDistributionFlag(String validDistributionFlag) {
		this.validDistributionFlag = validDistributionFlag;
	}
	
	@Column(length = 64)
	public String getEarliestDateCollected() {
		return earliestDateCollected;
	}
	public void setEarliestDateCollected(String earliestDateCollected) {
		this.earliestDateCollected = earliestDateCollected;
	}
	@Column(length = 64)
	public String getLatestDateCollected() {
		return latestDateCollected;
	}
	public void setLatestDateCollected(String latestDateCollected) {
		this.latestDateCollected = latestDateCollected;
	}
	
	@Column(length = 16)
	public String getDayOfYear() {
		return dayOfYear;
	}
	public void setDayOfYear(String dayOfYear) {
		this.dayOfYear = dayOfYear;
	}
	
	@Column(length = 128)
	public String getCollector() {
		return collector;
	}
	public void setCollector(String collector) {
		this.collector = collector;
	}
	
	
}
