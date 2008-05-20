/**
 * 
 */
package org.gbif.provider.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	private Integer minimumElevationInMeters;
	private Integer maximumElevationInMeters;
	private Integer minimumDepthInMeters;
	private Integer maximumDepthInMeters;
	// Collecting Event Elements	
	private String collectingMethod;
	private Boolean validDistributionFlag;
	private Date earliestDateCollected;
	private Date latestDateCollected;
	private Integer dayOfYear;
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
	public String getContinent() {
		return continent;
	}
	public void setContinent(String continent) {
		this.continent = continent;
	}
	public String getWaterBody() {
		return waterBody;
	}
	public void setWaterBody(String waterBody) {
		this.waterBody = waterBody;
	}
	public String getIslandGroup() {
		return islandGroup;
	}
	public void setIslandGroup(String islandGroup) {
		this.islandGroup = islandGroup;
	}
	public String getIsland() {
		return island;
	}
	public void setIsland(String island) {
		this.island = island;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getStateProvince() {
		return stateProvince;
	}
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public Integer getMinimumElevationInMeters() {
		return minimumElevationInMeters;
	}
	public void setMinimumElevationInMeters(Integer minimumElevationInMeters) {
		this.minimumElevationInMeters = minimumElevationInMeters;
	}
	public Integer getMaximumElevationInMeters() {
		return maximumElevationInMeters;
	}
	public void setMaximumElevationInMeters(Integer maximumElevationInMeters) {
		this.maximumElevationInMeters = maximumElevationInMeters;
	}
	public Integer getMinimumDepthInMeters() {
		return minimumDepthInMeters;
	}
	public void setMinimumDepthInMeters(Integer minimumDepthInMeters) {
		this.minimumDepthInMeters = minimumDepthInMeters;
	}
	public Integer getMaximumDepthInMeters() {
		return maximumDepthInMeters;
	}
	public void setMaximumDepthInMeters(Integer maximumDepthInMeters) {
		this.maximumDepthInMeters = maximumDepthInMeters;
	}
	public String getCollectingMethod() {
		return collectingMethod;
	}
	public void setCollectingMethod(String collectingMethod) {
		this.collectingMethod = collectingMethod;
	}
	public Boolean getValidDistributionFlag() {
		return validDistributionFlag;
	}
	public void setValidDistributionFlag(Boolean validDistributionFlag) {
		this.validDistributionFlag = validDistributionFlag;
	}
	public Date getEarliestDateCollected() {
		return earliestDateCollected;
	}
	public void setEarliestDateCollected(Date earliestDateCollected) {
		this.earliestDateCollected = earliestDateCollected;
	}
	public Date getLatestDateCollected() {
		return latestDateCollected;
	}
	public void setLatestDateCollected(Date latestDateCollected) {
		this.latestDateCollected = latestDateCollected;
	}
	public Integer getDayOfYear() {
		return dayOfYear;
	}
	public void setDayOfYear(Integer dayOfYear) {
		this.dayOfYear = dayOfYear;
	}
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
				.append(this.stateProvince, myClass.stateProvince)
				.append(this.county, myClass.county)
				.append(this.continent, myClass.continent)
				.append(this.country, myClass.country)
				.append(this.islandGroup, myClass.islandGroup)
				.append(this.maximumDepthInMeters, myClass.maximumDepthInMeters)
				.append(this.minimumDepthInMeters, myClass.minimumDepthInMeters)
				.append(this.earliestDateCollected,
						myClass.earliestDateCollected).append(this.id,
						myClass.id).append(this.higherGeography,
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
		return new EqualsBuilder().append(
				this.waterBody, rhs.waterBody).append(
				this.validDistributionFlag, rhs.validDistributionFlag).append(
				this.locality, rhs.locality).append(this.island, rhs.island)
				.append(this.minimumElevationInMeters,
						rhs.minimumElevationInMeters).append(
						this.maximumElevationInMeters,
						rhs.maximumElevationInMeters).append(
						this.latestDateCollected, rhs.latestDateCollected)
				.append(this.stateProvince,
						rhs.stateProvince).append(this.county, rhs.county)
				.append(this.continent, rhs.continent).append(this.country,
						rhs.country).append(this.islandGroup, rhs.islandGroup)
				.append(this.maximumDepthInMeters, rhs.maximumDepthInMeters)
				.append(this.minimumDepthInMeters, rhs.minimumDepthInMeters)
				.append(this.earliestDateCollected, rhs.earliestDateCollected)
				.append(this.id, rhs.id).append(this.higherGeography,
						rhs.higherGeography).append(this.collector,
						rhs.collector).append(this.collectingMethod,
						rhs.collectingMethod).append(this.dayOfYear,
						rhs.dayOfYear).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(2290355, 213631307).append(this.waterBody).append(
				this.validDistributionFlag).append(this.locality).append(
				this.island).append(this.minimumElevationInMeters).append(
				this.maximumElevationInMeters).append(this.latestDateCollected)
				.append(this.stateProvince).append(this.county)
				.append(this.continent).append(this.country).append(
						this.islandGroup).append(this.maximumDepthInMeters)
				.append(this.minimumDepthInMeters).append(
						this.earliestDateCollected).append(this.id).append(
						this.higherGeography).append(this.collector).append(
						this.collectingMethod).append(this.dayOfYear)
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("dwc", this.id).append(
				"minimumDepthInMeters", this.minimumDepthInMeters).append("collector", this.collector).append(
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
