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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.appfuse.model.BaseObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
@Table(name="dwcore_tax") 
@Indexed
public class DarwinCoreTaxonomy  {
	@DocumentId
	private Long id;
	@ContainedIn
	private DarwinCore dwc;
	// Taxonomic Elements
    @Field(index=Index.TOKENIZED, store=Store.YES)
	private String scientificName;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String higherTaxon;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String kingdom;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String phylum;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String classs;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String order;
    @Field(index=Index.TOKENIZED, store=Store.NO)
	private String family;
	private String genus;
	private String specificEpithet;
	private String infraspecificRank;
	private String infraspecificEpithet;
	private String authorYearOfScientificName;
	private String nomenclaturalCode;
	// Identification Elements
	private String identificationQualifer;
	
	
	@Id
	@GeneratedValue(generator="dwcidtax")
	@GenericGenerator(name="dwcidtax", strategy = "foreign", 
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
	@Column(length=64)
	public String getIdentificationQualifer() {
		return identificationQualifer;
	}
	public void setIdentificationQualifer(String identificationQualifer) {
		this.identificationQualifer = identificationQualifer;
	}
	
	
}
