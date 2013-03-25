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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;

/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
@Table(name="dwcore_ext") 
public class DarwinCoreExtended implements ResourceRelatedObject{
	private Long id;
	private DarwinCore dwc;
	private OccurrenceResource resource;
	// Locality Elements
	private String higherGeography;
	private String continent;
	private String waterBody;
	private String islandGroup;
	private String island;
	private String country;
	private String stateProvince;
	private String county;
	@Lob
	private String locality;
	private String minimumElevationInMeters;
	private String maximumElevationInMeters;
	private String minimumDepthInMeters;
	private String maximumDepthInMeters;
	// Taxonomic Elements apart from ScientificName
	private String specificEpithet;
	private String infraspecificRank;
	private String infraspecificEpithet;
	private String authorYearOfScientificName;
	private String nomenclaturalCode;
	private String higherTaxon;
	private String kingdom;
	private String phylum;
	private String classs;
	private String order;
	private String family;
	private String genus;
	
	
	
	@Id
	@GeneratedValue(generator="dwcid")
	@GenericGenerator(name="dwcid", strategy = "foreign", 
			parameters={@Parameter (name="property", value = "dwc")}
	)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional=false) 
	@PrimaryKeyJoinColumn
	public DarwinCore getDwc() {
		return dwc;
	}
	public void setDwc(DarwinCore dwc) {
		this.dwc = dwc;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public OccurrenceResource getResource() {
		return resource;
	}
	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}

	@Lob
	public String getHigherGeography() {
		return higherGeography;
	}
	public void setHigherGeography(String higherGeography) {
		this.higherGeography = higherGeography;
	}
	@Column(length = 64)
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

	@Column(length = 64)
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
	@Column(length = 64)
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

	@Lob
	public String getHigherTaxon() {
		return higherTaxon;
	}
	public void setHigherTaxon(String higherTaxon) {
		this.higherTaxon = higherTaxon;
	}
	@Column(length=64)
	public String getKingdom() {
		return kingdom;
	}
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}
	@Column(length=64)
	public String getPhylum() {
		return phylum;
	}
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}
	@Column(length=64)
	public String getClasss() {
		return classs;
	}
	public void setClasss(String classs) {
		this.classs = classs;
	}
	
	@Column(length=128, name="orderrr")
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}

	@Column(length=128)
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	@Column(length=64)
	@org.hibernate.annotations.Index(name="genus")
	public String getGenus() {
		return genus;
	}
	public void setGenus(String genus) {
		this.genus = genus;
	}
	@Column(length=128)
	public String getSpecificEpithet() {
		return specificEpithet;
	}
	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}
	@Column(length=128)
	public String getInfraspecificRank() {
		return infraspecificRank;
	}
	public void setInfraspecificRank(String infraspecificRank) {
		this.infraspecificRank = infraspecificRank;
	}
	@Column(length=128)
	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		this.infraspecificEpithet = infraspecificEpithet;
	}
	@Column(length=128)
	public String getAuthorYearOfScientificName() {
		return authorYearOfScientificName;
	}
	public void setAuthorYearOfScientificName(String authorYearOfScientificName) {
		this.authorYearOfScientificName = authorYearOfScientificName;
	}
	@Column(length=64)
	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}
	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}		
}
