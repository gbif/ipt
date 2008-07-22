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
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.job.RdbmsUploadJob;

import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;


/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
public class DarwinCore extends CoreRecord{
	private static I18nLog logdb = I18nLogFactory.getLog(DarwinCore.class);
	
	private DarwinCoreTaxonomy tax;
	private DarwinCoreLocation loc;
	// calculated fields
	private float latitudeAsFloat;
	private float longitudeAsFloat;
	private Taxon taxon;
	
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
	public static DarwinCore newInstance(ImportRecord iRec){
		DarwinCore dwc = DarwinCore.newInstance();
		dwc.setGuid(iRec.getGuid());
		dwc.setLink(iRec.getLink());
		dwc.setLocalId(iRec.getLocalId());
		dwc.setModified(iRec.getModified());
		dwc.setDeleted(false);
		for (ExtensionProperty prop : iRec.getProperties().keySet()){
			// set all dwc properties apart from:
			// DateLastModified: managed by CoreRecord and this software
			String val = iRec.getPropertyValue(prop);
			String propName = prop.getName();
			if(propName.equals("GlobalUniqueIdentifier")){
				dwc.setGlobalUniqueIdentifier(val);
			}else if(propName.equals("BasisOfRecord")){
				dwc.setBasisOfRecord(val);
			}else if(propName.equals("InstitutionCode")){
				dwc.setInstitutionCode(val);
			}else if(propName.equals("CollectionCode")){
				dwc.setCollectionCode(val);
			}else if(propName.equals("CatalogNumber")){
				dwc.setCatalogNumber(val);
			}else if(propName.equals("InformationWithheld")){
				dwc.setInformationWithheld(val);
			}else if(propName.equals("Remarks")){
				dwc.setRemarks(val);
			}else if(propName.equals("Sex")){
				dwc.setSex(val);
			}else if(propName.equals("LifeStage")){
				dwc.setLifeStage(val);
			}else if(propName.equals("Attributes")){
				dwc.setAttributes(val);
			}else if(propName.equals("ImageURL")){
				dwc.setImageURL(val);
			}else if(propName.equals("RelatedInformation")){
				dwc.setRelatedInformation(val);
			}else if(propName.equals("HigherGeography")){
				dwc.setHigherGeography(val);
			}else if(propName.equals("Continent")){
				dwc.setContinent(val);
			}else if(propName.equals("WaterBody")){
				dwc.setWaterBody(val);
			}else if(propName.equals("IslandGroup")){
				dwc.setIslandGroup(val);
			}else if(propName.equals("Island")){
				dwc.setIsland(val);
			}else if(propName.equals("Country")){
				dwc.setCountry(val);
			}else if(propName.equals("StateProvince")){
				dwc.setStateProvince(val);
			}else if(propName.equals("County")){
				dwc.setCounty(val);
			}else if(propName.equals("Locality")){
				dwc.setLocality(val);
			}else if(propName.equals("MinimumElevationInMeters")){
				dwc.setMinimumElevationInMeters(val);
				// try to convert into proper type
				try {
					Integer typedVal = Integer.valueOf(val);
					dwc.setMinimumElevationInMetersAsInteger(typedVal);
				} catch (NumberFormatException e) {
					logdb.warn("Couldnt transform value '{0}' for property MinimumElevationInMeters into Integer value", val, e);
				}
			}else if(propName.equals("MaximumElevationInMeters")){
				dwc.setMaximumElevationInMeters(val);
				// try to convert into proper type
				try {
					Integer typedVal = Integer.valueOf(val);
					dwc.setMaximumElevationInMetersAsInteger(typedVal);
				} catch (NumberFormatException e) {
					logdb.warn("Couldnt transform value '{0}' for property MaximumElevationInMeters into Integer value", val, e);
				}
			}else if(propName.equals("MinimumDepthInMeters")){
				dwc.setMinimumDepthInMeters(val);
				// try to convert into proper type
				try {
					Integer typedVal = Integer.valueOf(val);
					dwc.setMinimumDepthInMetersAsInteger(typedVal);
				} catch (NumberFormatException e) {
					logdb.warn("Couldnt transform value '{0}' for property MinimumDepthInMeters into Integer value", val, e);
				}
			}else if(propName.equals("MaximumDepthInMeters")){
				dwc.setMaximumDepthInMeters(val);
				// try to convert into proper type
				try {
					Integer typedVal = Integer.valueOf(val);
					dwc.setMaximumDepthInMetersAsInteger(typedVal);
				} catch (NumberFormatException e) {
					logdb.warn("Couldnt transform value '{0}' for property MaximumDepthInMeters into Integer value", val, e);
				}
			}else if(propName.equals("CollectingMethod")){
				dwc.setCollectingMethod(val);
			}else if(propName.equals("ValidDistributionFlag")){
				dwc.setValidDistributionFlag(val);
			}else if(propName.equals("EarliestDateCollected")){
				dwc.setEarliestDateCollected(val);
			}else if(propName.equals("LatestDateCollected")){
				dwc.setLatestDateCollected(val);
			}else if(propName.equals("DayOfYear")){
				dwc.setDayOfYear(val);
			}else if(propName.equals("Collector")){
				dwc.setCollector(val);
			}else if(propName.equals("ScientificName")){
				dwc.setScientificName(val);
			}else if(propName.equals("HigherTaxon")){
				dwc.setHigherTaxon(val);
			}else if(propName.equals("Kingdom")){
				dwc.setKingdom(val);
			}else if(propName.equals("Phylum")){
				dwc.setPhylum(val);
			}else if(propName.equals("Classs")){
				dwc.setClasss(val);
			}else if(propName.equals("Order")){
				dwc.setOrder(val);
			}else if(propName.equals("Family")){
				dwc.setFamily(val);
			}else if(propName.equals("Genus")){
				dwc.setGenus(val);
			}else if(propName.equals("SpecificEpithet")){
				dwc.setSpecificEpithet(val);
			}else if(propName.equals("InfraspecificRank")){
				dwc.setInfraspecificRank(val);
			}else if(propName.equals("InfraspecificEpithet")){
				dwc.setInfraspecificEpithet(val);
			}else if(propName.equals("AuthorYearOfScientificName")){
				dwc.setAuthorYearOfScientificName(val);
			}else if(propName.equals("NomenclaturalCode")){
				dwc.setNomenclaturalCode(val);
			}else if(propName.equals("IdentificationQualifer")){
				dwc.setIdentificationQualifer(val);
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
	
	
	@ManyToOne(optional = true)
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	public float getLatitudeAsFloat() {
		return latitudeAsFloat;
	}
	public void setLatitudeAsFloat(float latitudeAsFloat) {
		this.latitudeAsFloat = latitudeAsFloat;
	}
	public float getLongitudeAsFloat() {
		return longitudeAsFloat;
	}
	public void setLongitudeAsFloat(float longitudeAsFloat) {
		this.longitudeAsFloat = longitudeAsFloat;
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
	public Integer getMinimumElevationInMetersAsInteger() {
		return loc.getMinimumElevationInMetersAsInteger();
	}
	public void setMinimumElevationInMetersAsInteger(Integer minimumElevationInMeters) {
		loc.setMinimumElevationInMetersAsInteger(minimumElevationInMeters);
	}
	@Transient
	public Integer getMaximumElevationInMetersAsInteger() {
		return loc.getMaximumElevationInMetersAsInteger();
	}
	public void setMaximumElevationInMetersAsInteger(Integer maximumElevationInMeters) {
		loc.setMaximumElevationInMetersAsInteger(maximumElevationInMeters);
	}
	@Transient
	public Integer getMinimumDepthInMetersAsInteger() {
		return loc.getMinimumDepthInMetersAsInteger();
	}
	public void setMinimumDepthInMetersAsInteger(Integer minimumDepthInMeters) {
		loc.setMinimumDepthInMetersAsInteger(minimumDepthInMeters);
	}
	@Transient
	public Integer getMaximumDepthInMetersAsInteger() {
		return loc.getMaximumDepthInMetersAsInteger();
	}
	public void setMaximumDepthInMetersAsInteger(Integer maximumDepthInMeters) {
		loc.setMaximumDepthInMetersAsInteger(maximumDepthInMeters);
	}
	@Transient
	public String getMinimumElevationInMeters() {
		return loc.getMinimumElevationInMeters();
	}
	public void setMinimumElevationInMeters(String minimumElevationInMeters) {
		loc.setMinimumElevationInMeters(minimumElevationInMeters);
	}
	@Transient
	public String getMaximumElevationInMeters() {
		return loc.getMaximumElevationInMeters();
	}
	public void setMaximumElevationInMeters(String maximumElevationInMeters) {
		loc.setMaximumElevationInMeters(maximumElevationInMeters);
	}
	@Transient
	public String getMinimumDepthInMeters() {
		return loc.getMinimumDepthInMeters();
	}
	public void setMinimumDepthInMeters(String minimumDepthInMeters) {
		loc.setMinimumDepthInMeters(minimumDepthInMeters);
	}
	@Transient
	public String getMaximumDepthInMeters() {
		return loc.getMaximumDepthInMeters();
	}
	public void setMaximumDepthInMeters(String maximumDepthInMeters) {
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
	public String getValidDistributionFlag() {
		return loc.getValidDistributionFlag();
	}
	public void setValidDistributionFlag(String validDistributionFlag) {
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
	public String getDayOfYear() {
		return loc.getDayOfYear();
	}
	public void setDayOfYear(String dayOfYear) {
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
		if (object == this) {
			return true;
		}
		if (!(object instanceof DarwinCore)) {
			return false;
		}
		DarwinCore dwc = (DarwinCore) object;
        return this.hashCode() == dwc.hashCode();		
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        // core record
        result = (getGuid() != null ? getGuid().hashCode() : 0);
        result = 31 * result + (getLink() != null ? getLink().hashCode() : 0);
        result = 31 * result + (getLocalId() != null ? getLocalId().hashCode() : 0);
        // this dwc class
        result = 31 * result + (globalUniqueIdentifier != null ? globalUniqueIdentifier.hashCode() : 0);
        result = 31 * result + (basisOfRecord != null ? basisOfRecord.hashCode() : 0);
        result = 31 * result + (institutionCode != null ? institutionCode.hashCode() : 0);
        result = 31 * result + (collectionCode != null ? collectionCode.hashCode() : 0);
        result = 31 * result + (catalogNumber != null ? catalogNumber.hashCode() : 0);
        result = 31 * result + (informationWithheld != null ? informationWithheld.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (lifeStage != null ? lifeStage.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        result = 31 * result + (relatedInformation != null ? relatedInformation.hashCode() : 0);
        // dwc location
        result = 31 * result + (getHigherGeography() != null ? getHigherGeography().hashCode() : 0);
        result = 31 * result + (getContinent() != null ? getContinent().hashCode() : 0);
        result = 31 * result + (getWaterBody() != null ? getWaterBody().hashCode() : 0);
        result = 31 * result + (getIslandGroup() != null ? getIslandGroup().hashCode() : 0);
        result = 31 * result + (getIsland() != null ? getIsland().hashCode() : 0);
        result = 31 * result + (getCountry() != null ? getCountry().hashCode() : 0);
        result = 31 * result + (getStateProvince() != null ? getStateProvince().hashCode() : 0);
        result = 31 * result + (getCounty() != null ? getCounty().hashCode() : 0);
        result = 31 * result + (getLocality() != null ? getLocality().hashCode() : 0);
        result = 31 * result + (getMinimumElevationInMeters() != null ? getMinimumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumElevationInMeters() != null ? getMaximumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getMinimumDepthInMeters() != null ? getMinimumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumDepthInMeters() != null ? getMaximumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getCollectingMethod() != null ? getCollectingMethod().hashCode() : 0);
        result = 31 * result + (getValidDistributionFlag() != null ? getValidDistributionFlag().hashCode() : 0);
        result = 31 * result + (getEarliestDateCollected() != null ? getEarliestDateCollected().hashCode() : 0);
        result = 31 * result + (getLatestDateCollected() != null ? getLatestDateCollected().hashCode() : 0);
        result = 31 * result + (getDayOfYear() != null ? getDayOfYear().hashCode() : 0);
        result = 31 * result + (getCollector() != null ? getCollector().hashCode() : 0);
        // dwc taxonomy
        result = 31 * result + (getScientificName() != null ? getScientificName().hashCode() : 0);
        result = 31 * result + (getHigherTaxon() != null ? getHigherTaxon().hashCode() : 0);
        result = 31 * result + (getKingdom() != null ? getKingdom().hashCode() : 0);
        result = 31 * result + (getPhylum() != null ? getPhylum().hashCode() : 0);
        result = 31 * result + (getClasss() != null ? getClasss().hashCode() : 0);
        result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
        result = 31 * result + (getFamily() != null ? getFamily().hashCode() : 0);
        result = 31 * result + (getGenus() != null ? getGenus().hashCode() : 0);
        result = 31 * result + (getSpecificEpithet() != null ? getSpecificEpithet().hashCode() : 0);
        result = 31 * result + (getInfraspecificRank() != null ? getInfraspecificRank().hashCode() : 0);
        result = 31 * result + (getSpecificEpithet() != null ? getSpecificEpithet().hashCode() : 0);
        result = 31 * result + (getInfraspecificRank() != null ? getInfraspecificRank().hashCode() : 0);
        result = 31 * result + (getInfraspecificEpithet() != null ? getInfraspecificEpithet().hashCode() : 0);
        result = 31 * result + (getAuthorYearOfScientificName() != null ? getAuthorYearOfScientificName().hashCode() : 0);
        result = 31 * result + (getNomenclaturalCode() != null ? getNomenclaturalCode().hashCode() : 0);
        result = 31 * result + (getIdentificationQualifer() != null ? getIdentificationQualifer().hashCode() : 0);
        
        return result;
	}
	
	
}
