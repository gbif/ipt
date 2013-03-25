/***************************************************************************
 * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
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
package org.gbif.provider.geotools;

/**
 * @author trobertson
 */
public class DwcRecord {
	protected String guid;
	protected Long taxonId;
	protected Long taxonLft;
	protected Long taxonRgt;
	protected Long regionId;
	protected Long regionLft;
	protected Long regionRgt;
	protected String scientificName;
	protected String family;
	protected String typeStatus;
	protected String locality;
	protected String institutionCode;
	protected String collectionCode;
	protected String catalogNumber;
	protected String collector;
	protected String earliestDateCollected;
	protected String basisOfRecord;
	protected Double latitude;
	protected Double longitude;
	
	public DwcRecord(String guid, Long taxonId, Long taxonLft, Long taxonRgt, Long regionId, Long regionLft, Long regionRgt, 
			String scientificName, String locality, String family, String typeStatus,
			String institutionCode,	String collectionCode, String catalogNumber, String collector,
			String earliestDateCollected, String basisOfRecord, Double latitude, Double longitude) {
		super();
		this.guid = guid;
		this.taxonId = taxonId;
		this.taxonLft = taxonLft;
		this.taxonRgt = taxonRgt;
		this.regionId = regionId;
		this.regionLft = regionLft;
		this.regionRgt = regionRgt;
		this.scientificName = scientificName;
		this.family = family;
		this.typeStatus = typeStatus;
		this.locality = locality;
		this.institutionCode = institutionCode;
		this.collectionCode = collectionCode;
		this.catalogNumber = catalogNumber;
		this.collector = collector;
		this.earliestDateCollected = earliestDateCollected;
		this.basisOfRecord = basisOfRecord;
		this.latitude = latitude;
		this.longitude = longitude;
	}


	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}

	public String getTypeStatus() {
		return typeStatus;
	}
	public void setTypeStatus(String typeStatus) {
		this.typeStatus = typeStatus;
	}

	public String getBasisOfRecord() {
		return basisOfRecord;
	}
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getEarliestDateCollected() {
		return earliestDateCollected;
	}
	public void setEarliestDateCollected(String earliestDateCollected) {
		this.earliestDateCollected = earliestDateCollected;
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

	public String getCollector() {
		return collector;
	}

	public void setCollector(String collector) {
		this.collector = collector;
	}


	public Long getTaxonId() {
		return taxonId;
	}
	public void setTaxonId(Long taxonId) {
		this.taxonId = taxonId;
	}

	public Long getTaxonLft() {
		return taxonLft;
	}

	public void setTaxonLft(Long taxonLft) {
		this.taxonLft = taxonLft;
	}

	public Long getTaxonRgt() {
		return taxonRgt;
	}

	public void setTaxonRgt(Long taxonRgt) {
		this.taxonRgt = taxonRgt;
	}


	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}

	public Long getRegionLft() {
		return regionLft;
	}

	public void setRegionLft(Long regionLft) {
		this.regionLft = regionLft;
	}

	public Long getRegionRgt() {
		return regionRgt;
	}

	public void setRegionRgt(Long regionRgt) {
		this.regionRgt = regionRgt;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}
	
}
