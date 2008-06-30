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
public class DarwinCoreTaxonomy extends BaseObject implements Comparable {
	private Long id;
	private DarwinCore dwc;
	// Taxonomic Elements
	private String scientificName;
	private String higherTaxon;
	private String kingdom;
	private String phylum;
	private String classs;
	private String order;
	private String family;
	private String genus;
	private String specificEpithet;
	private String infraspecificRank;
	private String infraspecificEpithet;
	private String authorYearOfScientificName;
	private String nomenclaturalCode;
	// Identification Elements
	private String identificationQualifer;
	
	
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
	
	
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getHigherTaxon() {
		return higherTaxon;
	}
	public void setHigherTaxon(String higherTaxon) {
		this.higherTaxon = higherTaxon;
	}
	public String getKingdom() {
		return kingdom;
	}
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}
	public String getPhylum() {
		return phylum;
	}
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}
	public String getClasss() {
		return classs;
	}
	public void setClasss(String classs) {
		this.classs = classs;
	}
	
	@Column(name="orderrr")
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getGenus() {
		return genus;
	}
	public void setGenus(String genus) {
		this.genus = genus;
	}
	public String getSpecificEpithet() {
		return specificEpithet;
	}
	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}
	public String getInfraspecificRank() {
		return infraspecificRank;
	}
	public void setInfraspecificRank(String infraspecificRank) {
		this.infraspecificRank = infraspecificRank;
	}
	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		this.infraspecificEpithet = infraspecificEpithet;
	}
	public String getAuthorYearOfScientificName() {
		return authorYearOfScientificName;
	}
	public void setAuthorYearOfScientificName(String authorYearOfScientificName) {
		this.authorYearOfScientificName = authorYearOfScientificName;
	}
	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}
	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
	public String getIdentificationQualifer() {
		return identificationQualifer;
	}
	public void setIdentificationQualifer(String identificationQualifer) {
		this.identificationQualifer = identificationQualifer;
	}
	
	
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof DarwinCoreTaxonomy)) {
			return false;
		}
		DarwinCoreTaxonomy rhs = (DarwinCoreTaxonomy) object;
		return new EqualsBuilder()
				.append(this.higherTaxon, rhs.higherTaxon)
				.append(this.identificationQualifer, rhs.identificationQualifer)
				.append(this.family, rhs.family).append(this.specificEpithet,
						rhs.specificEpithet).append(this.order, rhs.order)
				.append(this.id, rhs.id).append(this.genus, rhs.genus).append(
						this.scientificName, rhs.scientificName).append(
						this.kingdom, rhs.kingdom).append(this.phylum,
						rhs.phylum).append(this.authorYearOfScientificName,
						rhs.authorYearOfScientificName).append(
						this.infraspecificEpithet, rhs.infraspecificEpithet)
				.append(this.infraspecificRank, rhs.infraspecificRank).append(
						this.classs, rhs.classs).append(this.dwc, rhs.dwc)
				.append(this.nomenclaturalCode, rhs.nomenclaturalCode)
				.isEquals();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		DarwinCoreTaxonomy myClass = (DarwinCoreTaxonomy) object;
		return new CompareToBuilder()
				.append(this.higherTaxon, myClass.higherTaxon)
				.append(this.identificationQualifer,
						myClass.identificationQualifer)
				.append(this.family, myClass.family)
				.append(this.specificEpithet, myClass.specificEpithet)
				.append(this.order, myClass.order)
				.append(this.id, myClass.id)
				.append(this.genus, myClass.genus)
				.append(this.scientificName, myClass.scientificName)
				.append(this.kingdom, myClass.kingdom)
				.append(this.phylum, myClass.phylum)
				.append(this.authorYearOfScientificName,
						myClass.authorYearOfScientificName)
				.append(this.infraspecificEpithet, myClass.infraspecificEpithet)
				.append(this.infraspecificRank, myClass.infraspecificRank)
				.append(this.classs, myClass.classs).append(this.dwc,
						myClass.dwc).append(this.nomenclaturalCode,
						myClass.nomenclaturalCode).toComparison();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-1729505797, -1175574695).append(
				this.higherTaxon).append(this.identificationQualifer).append(
				this.family).append(this.specificEpithet).append(this.order)
				.append(this.id).append(this.genus).append(this.scientificName)
				.append(this.kingdom).append(this.phylum).append(
						this.authorYearOfScientificName).append(
						this.infraspecificEpithet).append(
						this.infraspecificRank).append(this.classs).append(
						this.dwc).append(this.nomenclaturalCode).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("identificationQualifer",
				this.identificationQualifer).append("dwc", this.dwc).append(
				"classs", this.classs).append("id", this.id).append("kingdom",
				this.kingdom).append("phylum", this.phylum).append(
				"higherTaxon", this.higherTaxon).append("genus", this.genus)
				.append("order", this.order).append("family", this.family)
				.append("infraspecificEpithet", this.infraspecificEpithet)
				.append("infraspecificRank", this.infraspecificRank).append(
						"specificEpithet", this.specificEpithet).append(
						"nomenclaturalCode", this.nomenclaturalCode).append(
						"authorYearOfScientificName",
						this.authorYearOfScientificName).append(
						"scientificName", this.scientificName).toString();
	}

	
	
}
