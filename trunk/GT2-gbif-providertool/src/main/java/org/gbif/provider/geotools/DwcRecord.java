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
	protected Integer resourceId;
	protected String kingdom;
	protected String phylum;
	protected String klass;
	protected String order;
	protected String family;
	protected String genus;
	protected String scientificName;
	protected String basisOfRecord;
	protected Double latitude;
	protected Double longitude;
	
	/**
	 * @param dataResourceId
	 * @param kingdom
	 * @param phylum
	 * @param klass
	 * @param order
	 * @param family
	 * @param genus
	 * @param species
	 * @param scientificName
	 * @param basisOfRecord
	 * @param latitude
	 * @param longitude
	 */
	public DwcRecord(Integer resourceId, String kingdom, String phylum,
			String klass, String order, String family, String genus,
			String scientificName, String basisOfRecord,
			Double latitude, Double longitude) {
		this.resourceId = resourceId;
		this.kingdom = kingdom;
		this.phylum = phylum;
		this.klass = klass;
		this.order = order;
		this.family = family;
		this.genus = genus;
		this.scientificName = scientificName;
		this.basisOfRecord = basisOfRecord;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public Integer getResourceId() {
		return resourceId;
	}
	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
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
	public String getKlass() {
		return klass;
	}
	public void setKlass(String klass) {
		this.klass = klass;
	}
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
	
}
