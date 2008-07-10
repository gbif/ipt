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
public class DarwinCoreLocation extends BaseObject implements Comparable {
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
	
	@Id @GeneratedValue(generator="dwcid")
	@GenericGenerator(name="dwcid", strategy = "foreign", 
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
	
	
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		DarwinCoreLocation myClass = (DarwinCoreLocation) object;
		return new CompareToBuilder()
				.append(this.waterBody, myClass.waterBody)
				.append(this.validDistributionFlag,
						myClass.validDistributionFlag)
				.append(this.locality, myClass.locality)
				.append(this.island, myClass.island)
				.append(this.minimumElevationInMeters,
						myClass.minimumElevationInMeters)
				.append(this.maximumElevationInMeters,
						myClass.maximumElevationInMeters)
				.append(this.latestDateCollected, myClass.latestDateCollected)
				.append(this.id, myClass.id)
				.append(this.stateProvince, myClass.stateProvince)
				.append(this.county, myClass.county)
				.append(this.continent, myClass.continent)
				.append(this.country, myClass.country)
				.append(this.islandGroup, myClass.islandGroup)
				.append(this.maximumDepthInMeters, myClass.maximumDepthInMeters)
				.append(this.minimumDepthInMeters, myClass.minimumDepthInMeters)
				.append(this.earliestDateCollected,
						myClass.earliestDateCollected).append(this.dwc,
						myClass.dwc).append(this.higherGeography,
						myClass.higherGeography).append(this.collector,
						myClass.collector).append(this.collectingMethod,
						myClass.collectingMethod).append(this.dayOfYear,
						myClass.dayOfYear).toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof DarwinCoreLocation)) {
			return false;
		}
		DarwinCoreLocation rhs = (DarwinCoreLocation) object;
		return new EqualsBuilder().append(this.waterBody, rhs.waterBody)
				.append(this.validDistributionFlag, rhs.validDistributionFlag)
				.append(this.locality, rhs.locality).append(this.island,
						rhs.island).append(this.minimumElevationInMeters,
						rhs.minimumElevationInMeters).append(
						this.maximumElevationInMeters,
						rhs.maximumElevationInMeters).append(
						this.latestDateCollected, rhs.latestDateCollected)
				.append(this.id, rhs.id).append(this.stateProvince,
						rhs.stateProvince).append(this.county, rhs.county)
				.append(this.continent, rhs.continent).append(this.country,
						rhs.country).append(this.islandGroup, rhs.islandGroup)
				.append(this.maximumDepthInMeters, rhs.maximumDepthInMeters)
				.append(this.minimumDepthInMeters, rhs.minimumDepthInMeters)
				.append(this.earliestDateCollected, rhs.earliestDateCollected)
				.append(this.dwc, rhs.dwc).append(this.higherGeography,
						rhs.higherGeography).append(this.collector,
						rhs.collector).append(this.collectingMethod,
						rhs.collectingMethod).append(this.dayOfYear,
						rhs.dayOfYear).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(541065945, 425241663).append(this.waterBody)
				.append(this.validDistributionFlag).append(this.locality)
				.append(this.island).append(this.minimumElevationInMeters)
				.append(this.maximumElevationInMeters).append(
						this.latestDateCollected).append(this.id).append(
						this.stateProvince).append(this.county).append(
						this.continent).append(this.country).append(
						this.islandGroup).append(this.maximumDepthInMeters)
				.append(this.minimumDepthInMeters).append(
						this.earliestDateCollected).append(this.dwc).append(
						this.higherGeography).append(this.collector).append(
						this.collectingMethod).append(this.dayOfYear)
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("dwc", this.dwc).append(
				"minimumDepthInMeters", this.minimumDepthInMeters).append("id",
				this.id).append("collector", this.collector).append(
				"validDistributionFlag", this.validDistributionFlag).append(
				"country", this.country).append("earliestDateCollected",
				this.earliestDateCollected).append("continent", this.continent)
				.append("waterBody", this.waterBody).append("island",
						this.island)
				.append("stateProvince", this.stateProvince).append("locality",
						this.locality).append("county", this.county).append(
						"minimumElevationInMeters",
						this.minimumElevationInMeters).append(
						"latestDateCollected", this.latestDateCollected)
				.append("islandGroup", this.islandGroup).append("dayOfYear",
						this.dayOfYear).append("maximumDepthInMeters",
						this.maximumDepthInMeters).append("collectingMethod",
						this.collectingMethod).append(
						"maximumElevationInMeters",
						this.maximumElevationInMeters).append(
						"higherGeography", this.higherGeography).toString();
	}

	
	
}
