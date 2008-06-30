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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
public class DarwinCore extends CoreRecord{
	private DarwinCoreTaxonomy tax;
	private DarwinCoreLocation loc;
	// DarwinCore 1.4 elements
	private String globalUniqueIdentifier;
	private String basisOfRecord;
	private String institutionCode;
	private String collectionCode;
	private String catalogNumber;
	private String informationWithheld;
	private String remarks;
	// Biological Elements
	private String sex;
	private String lifeStage;
	private String attributes;
	// references elements
	private String imageURL;
	private String relatedInformation;	

	
	public static DarwinCore newInstance(){
		DarwinCore dwc = new DarwinCore();
		dwc.tax = new DarwinCoreTaxonomy();
		dwc.tax.setDwc(dwc);
		dwc.loc = new DarwinCoreLocation();
		dwc.loc.setDwc(dwc);
		return dwc;
	}
	public static DarwinCore newInstance(CoreRecord coreRec){
		DarwinCore dwc = DarwinCore.newInstance();
		dwc.setGuid(coreRec.getGuid());
		dwc.setLink(coreRec.getLink());
		dwc.setLocalId(coreRec.getLocalId());
		dwc.setModified(coreRec.getModified());
		dwc.setResource(coreRec.getResource());		
		dwc.setDeleted(false);
		for (ExtensionProperty prop : coreRec.getProperties().keySet()){
			// set all dwc properties apart from:
			// DateLastModified: managed by CoreRecord and this software
			if(prop.getName().equals("GlobalUniqueIdentifier")){
				dwc.setGlobalUniqueIdentifier(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("BasisOfRecord")){
				dwc.setBasisOfRecord(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("InstitutionCode")){
				dwc.setInstitutionCode(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("CollectionCode")){
				dwc.setCollectionCode(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("CatalogNumber")){
				dwc.setCatalogNumber(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("InformationWithheld")){
				dwc.setInformationWithheld(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Remarks")){
				dwc.setRemarks(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Sex")){
				dwc.setSex(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("LifeStage")){
				dwc.setLifeStage(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Attributes")){
				dwc.setAttributes(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("ImageURL")){
				dwc.setImageURL(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("RelatedInformation")){
				dwc.setRelatedInformation(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("HigherGeography")){
				dwc.setHigherGeography(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Continent")){
				dwc.setContinent(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("WaterBody")){
				dwc.setWaterBody(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("IslandGroup")){
				dwc.setIslandGroup(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Island")){
				dwc.setIsland(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Country")){
				dwc.setCountry(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("StateProvince")){
				dwc.setStateProvince(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("County")){
				dwc.setCounty(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Locality")){
				dwc.setLocality(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("MinimumElevationInMeters")){
				try {
					Integer val = Integer.valueOf(coreRec.getPropertyValue(prop));
					dwc.setMinimumElevationInMeters(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property MinimumElevationInMeters into Integer value");
				}
			}else if(prop.getName().equals("MaximumElevationInMeters")){
				try {
					Integer val = Integer.valueOf(coreRec.getPropertyValue(prop));
					dwc.setMaximumElevationInMeters(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property MaximumElevationInMeters into Integer value");
				}
			}else if(prop.getName().equals("MinimumDepthInMeters")){
				try {
					Integer val = Integer.valueOf(coreRec.getPropertyValue(prop));
					dwc.setMinimumDepthInMeters(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property MinimumDepthInMeters into Integer value");
				}
			}else if(prop.getName().equals("MaximumDepthInMeters")){
				try {
					Integer val = Integer.valueOf(coreRec.getPropertyValue(prop));
					dwc.setMaximumDepthInMeters(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property MaximumDepthInMeters into Integer value");
				}
			}else if(prop.getName().equals("CollectingMethod")){
				dwc.setCollectingMethod(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("ValidDistributionFlag")){
				try {
					// allow true/TRUE  but also 1
					Boolean val = Boolean.valueOf(coreRec.getPropertyValue(prop)) || coreRec.getPropertyValue(prop).equals("1");
					dwc.setValidDistributionFlag(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property ValidDistributionFlag into Boolean value");
				}
			}else if(prop.getName().equals("EarliestDateCollected")){
				dwc.setEarliestDateCollected(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("LatestDateCollected")){
				dwc.setLatestDateCollected(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("DayOfYear")){
				try {
					Integer val = Integer.valueOf(coreRec.getPropertyValue(prop));
					dwc.setDayOfYear(val);
				} catch (NumberFormatException e) {
					log.warn("Couldnt transform property DayOfYear into Integer value");
				}
			}else if(prop.getName().equals("Collector")){
				dwc.setCollector(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("ScientificName")){
				dwc.setScientificName(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("HigherTaxon")){
				dwc.setHigherTaxon(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Kingdom")){
				dwc.setKingdom(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Phylum")){
				dwc.setPhylum(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Classs")){
				dwc.setClasss(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Order")){
				dwc.setOrder(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Family")){
				dwc.setFamily(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("Genus")){
				dwc.setGenus(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("SpecificEpithet")){
				dwc.setSpecificEpithet(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("InfraspecificRank")){
				dwc.setInfraspecificRank(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("InfraspecificEpithet")){
				dwc.setInfraspecificEpithet(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("AuthorYearOfScientificName")){
				dwc.setAuthorYearOfScientificName(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("NomenclaturalCode")){
				dwc.setNomenclaturalCode(coreRec.getPropertyValue(prop));
			}else if(prop.getName().equals("IdentificationQualifer")){
				dwc.setIdentificationQualifer(coreRec.getPropertyValue(prop));
			}

		}
		return dwc;
	}
	
	
	
	@OneToOne(mappedBy="dwc", cascade=CascadeType.ALL)
	public DarwinCoreTaxonomy getTax() {
		return tax;
	}
	public void setTax(DarwinCoreTaxonomy tax) {
		this.tax = tax;
	}
	
	@OneToOne(mappedBy="dwc", cascade=CascadeType.ALL)
	public DarwinCoreLocation getLoc() {
		return loc;
	}
	public void setLoc(DarwinCoreLocation loc) {
		this.loc = loc;
	}
	
	
	public String getGlobalUniqueIdentifier() {
		return globalUniqueIdentifier;
	}
	public void setGlobalUniqueIdentifier(String globalUniqueIdentifier) {
		this.globalUniqueIdentifier = globalUniqueIdentifier;
	}
	/**no need for extra date last modified. Forward to CoreRecord property
	 * @return
	 */
	@Transient
	public Date getDateLastModified() {
		return this.getModified();
	}
	public String getBasisOfRecord() {
		return basisOfRecord;
	}
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	public String getInstitutionCode() {
		return institutionCode;
	}
	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}
	public String getCollectionCode() {
		return collectionCode;
	}
	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}
	public String getCatalogNumber() {
		return catalogNumber;
	}
	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}
	public String getInformationWithheld() {
		return informationWithheld;
	}
	public void setInformationWithheld(String informationWithheld) {
		this.informationWithheld = informationWithheld;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getLifeStage() {
		return lifeStage;
	}
	public void setLifeStage(String lifeStage) {
		this.lifeStage = lifeStage;
	}
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	public String getRelatedInformation() {
		return relatedInformation;
	}
	public void setRelatedInformation(String relatedInformation) {
		this.relatedInformation = relatedInformation;
	}
	
	
	//
	// Forwarding properties
	//
	
	// LOCATION FORWARDING
	@Transient
	public String getHigherGeography() {
		return loc.getHigherGeography();
	}
	public void setHigherGeography(String higherGeography) {
		loc.setHigherGeography(higherGeography);
	}
	@Transient
	public String getContinent() {
		return loc.getContinent();
	}
	public void setContinent(String continent) {
		loc.setContinent(continent);
	}
	@Transient
	public String getWaterBody() {
		return loc.getWaterBody();
	}
	public void setWaterBody(String waterBody) {
		loc.setWaterBody(waterBody);
	}
	@Transient
	public String getIslandGroup() {
		return loc.getIslandGroup();
	}
	public void setIslandGroup(String islandGroup) {
		loc.setIslandGroup(islandGroup);
	}
	@Transient
	public String getIsland() {
		return loc.getIsland();
	}
	public void setIsland(String island) {
		loc.setIsland(island);
	}
	@Transient
	public String getCountry() {
		return loc.getCountry();
	}
	public void setCountry(String country) {
		loc.setCountry(country);
	}
	@Transient
	public String getStateProvince() {
		return loc.getStateProvince();
	}
	public void setStateProvince(String stateProvince) {
		loc.setStateProvince(stateProvince);
	}
	@Transient
	public String getCounty() {
		return loc.getCounty();
	}
	public void setCounty(String county) {
		loc.setCounty(county);
	}
	@Transient
	public String getLocality() {
		return loc.getLocality();
	}
	public void setLocality(String locality) {
		loc.setLocality(locality);
	}
	@Transient
	public Integer getMinimumElevationInMeters() {
		return loc.getMinimumElevationInMeters();
	}
	public void setMinimumElevationInMeters(Integer minimumElevationInMeters) {
		loc.setMinimumElevationInMeters(minimumElevationInMeters);
	}
	@Transient
	public Integer getMaximumElevationInMeters() {
		return loc.getMaximumElevationInMeters();
	}
	public void setMaximumElevationInMeters(Integer maximumElevationInMeters) {
		loc.setMaximumElevationInMeters(maximumElevationInMeters);
	}
	@Transient
	public Integer getMinimumDepthInMeters() {
		return loc.getMinimumDepthInMeters();
	}
	public void setMinimumDepthInMeters(Integer minimumDepthInMeters) {
		loc.setMinimumDepthInMeters(minimumDepthInMeters);
	}
	@Transient
	public Integer getMaximumDepthInMeters() {
		return loc.getMaximumDepthInMeters();
	}
	public void setMaximumDepthInMeters(Integer maximumDepthInMeters) {
		loc.setMaximumDepthInMeters(maximumDepthInMeters);
	}
	@Transient
	public String getCollectingMethod() {
		return loc.getCollectingMethod();
	}
	public void setCollectingMethod(String collectingMethod) {
		loc.setCollectingMethod(collectingMethod);
	}
	@Transient
	public Boolean getValidDistributionFlag() {
		return loc.getValidDistributionFlag();
	}
	public void setValidDistributionFlag(Boolean validDistributionFlag) {
		loc.setValidDistributionFlag(validDistributionFlag);
	}
	@Transient
	public String getEarliestDateCollected() {
		return loc.getEarliestDateCollected();
	}
	public void setEarliestDateCollected(String earliestDateCollected) {
		loc.setEarliestDateCollected(earliestDateCollected);
	}
	@Transient
	public String getLatestDateCollected() {
		return loc.getLatestDateCollected();
	}
	public void setLatestDateCollected(String latestDateCollected) {
		loc.setLatestDateCollected(latestDateCollected);
	}
	@Transient
	public Integer getDayOfYear() {
		return loc.getDayOfYear();
	}
	public void setDayOfYear(Integer dayOfYear) {
		loc.setDayOfYear(dayOfYear);
	}
	@Transient
	public String getCollector() {
		return loc.getCollector();
	}
	public void setCollector(String collector) {
		loc.setCollector(collector);
	}


	// TAXONOMY FORWARDING
	
	@Transient
	public String getScientificName() {
		return tax.getScientificName();
	}
	public void setScientificName(String scientificName) {
		tax.setScientificName(scientificName);
	}
	@Transient
	public String getHigherTaxon() {
		return tax.getHigherTaxon();
	}
	public void setHigherTaxon(String higherTaxon) {
		tax.setHigherTaxon(higherTaxon);
	}
	@Transient
	public String getKingdom() {
		return tax.getKingdom();
	}
	public void setKingdom(String kingdom) {
		tax.setKingdom(kingdom);
	}
	@Transient
	public String getPhylum() {
		return tax.getPhylum();
	}
	public void setPhylum(String phylum) {
		tax.setPhylum(phylum);
	}
	@Transient
	public String getClasss() {
		return tax.getClasss();
	}
	public void setClasss(String classs) {
		tax.setClasss(classs);
	}
	@Transient
	public String getOrder() {
		return tax.getOrder();
	}
	public void setOrder(String order) {
		tax.setOrder(order);
	}
	@Transient
	public String getFamily() {
		return tax.getFamily();
	}
	public void setFamily(String family) {
		tax.setFamily(family);
	}
	@Transient
	public String getGenus() {
		return tax.getGenus();
	}
	public void setGenus(String genus) {
		tax.setGenus(genus);
	}
	@Transient
	public String getSpecificEpithet() {
		return tax.getSpecificEpithet();
	}
	public void setSpecificEpithet(String specificEpithet) {
		tax.setSpecificEpithet(specificEpithet);
	}
	@Transient
	public String getInfraspecificRank() {
		return tax.getInfraspecificRank();
	}
	public void setInfraspecificRank(String infraspecificRank) {
		tax.setInfraspecificRank(infraspecificRank);
	}
	@Transient
	public String getInfraspecificEpithet() {
		return tax.getInfraspecificEpithet();
	}
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		tax.setInfraspecificEpithet(infraspecificEpithet);
	}
	@Transient
	public String getAuthorYearOfScientificName() {
		return tax.getAuthorYearOfScientificName();
	}
	public void setAuthorYearOfScientificName(String authorYearOfScientificName) {
		tax.setAuthorYearOfScientificName(authorYearOfScientificName);
	}
	@Transient
	public String getNomenclaturalCode() {
		return tax.getNomenclaturalCode();
	}
	public void setNomenclaturalCode(String nomenclaturalCode) {
		tax.setNomenclaturalCode(nomenclaturalCode);
	}
	@Transient
	public String getIdentificationQualifer() {
		return tax.getIdentificationQualifer();
	}
	public void setIdentificationQualifer(String identificationQualifer) {
		tax.setIdentificationQualifer(identificationQualifer);
	}

	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", this.getId())
			.append("basisOfRecord", this.basisOfRecord)
			.append("scientificName",this.getScientificName())
			.append("localID", this.getLocalId())
			.append("deleted", this.isDeleted())
			.append("globalUniqueIdentifier", this.globalUniqueIdentifier)
			.append("institutionCode", this.institutionCode)
			.append("collectionCode",this.collectionCode)
			.append("catalogNumber",this.catalogNumber)
			.append("country",this.getCountry())
			.toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof DarwinCore)) {
			return false;
		}
		DarwinCore rhs = (DarwinCore) object;
		return new EqualsBuilder()
				.append(this.basisOfRecord, rhs.basisOfRecord).append(
						this.imageURL, rhs.imageURL).append(
						this.institutionCode, rhs.institutionCode).append(
						this.loc, rhs.loc).append(this.remarks, rhs.remarks)
				.append(this.relatedInformation, rhs.relatedInformation)
				.append(this.informationWithheld, rhs.informationWithheld)
				.append(this.lifeStage, rhs.lifeStage)
				.append(this.sex, rhs.sex).append(this.attributes,
						rhs.attributes).append(this.globalUniqueIdentifier,
						rhs.globalUniqueIdentifier).append(this.collectionCode,
						rhs.collectionCode).append(this.tax, rhs.tax).append(
						this.catalogNumber, rhs.catalogNumber).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		//FIXME: implement custom hashcode function which uses all getters!
		return new HashCodeBuilder(-1669241179, -555797071).append(
				this.basisOfRecord).append(this.imageURL).append(
				this.institutionCode).append(this.loc).append(this.remarks)
				.append(this.relatedInformation).append(
						this.informationWithheld).append(this.lifeStage)
				.append(this.sex).append(this.attributes).append(
						this.globalUniqueIdentifier)
				.append(this.collectionCode).append(this.tax).append(
						this.catalogNumber).toHashCode();
	}
	
	
}
