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
	protected Long regionId;
	protected String scientificName;
	protected String locality;
	protected String institutionCode;
	protected String collectionCode;
	protected String catalogNumber;
	protected String collector;
	protected String dateCollected;
	protected String basisOfRecord;
	protected Double latitude;
	protected Double longitude;
	
	public DwcRecord(String guid, Long taxonId, Long regionId, 
			String scientificName, String locality,
			String institutionCode,	String collectionCode, String catalogNumber, String collector,
			String dateCollected, String basisOfRecord, Double latitude, Double longitude) {
		super();
		this.guid = guid;
		this.taxonId = taxonId;
		this.regionId = regionId;
		this.scientificName = scientificName;
		this.locality = locality;
		this.institutionCode = institutionCode;
		this.collectionCode = collectionCode;
		this.catalogNumber = catalogNumber;
		this.collector = collector;
		this.dateCollected = dateCollected;
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

	public String getDateCollected() {
		return dateCollected;
	}

	public void setDateCollected(String dateCollected) {
		this.dateCollected = dateCollected;
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


	public Long getRegionId() {
		return regionId;
	}


	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}


	public String getLocality() {
		return locality;
	}


	public void setLocality(String locality) {
		this.locality = locality;
	}
	
}
