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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.gbif.provider.util.ConfigUtil;
import org.gbif.provider.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MapKeyManyToMany;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DatasourceBasedResource {
	public static final Long CORE_EXTENSION_ID = 1l;
	
	// cached statistics
	private int recTotal;
	private int recWithCoordinates;
	private int recWithCountry;
	private int recWithAltitude;
	private int recWithDate;
	private int numCountries;
	
	private int numTerminalTaxa;
	private int numSpecies;
	private int numGenera;
	private int numFamilies;
	private int numOrders;
	private int numClasses;
	private int numPhyla;
	private int numKingdoms;
	
	public static OccurrenceResource newInstance(Extension core){
		OccurrenceResource resource =  new OccurrenceResource();
		// ensure that core mapping exists
		ViewCoreMapping coreVM = new ViewCoreMapping();
		coreVM.setExtension(core);
		coreVM.setResource(resource);
		resource.setCoreMapping(coreVM);
		return resource;
	}

	public int getRecTotal() {
		return recTotal;
	}

	public void setRecTotal(int recTotal) {
		this.recTotal = recTotal;
	}

	public int getRecWithCoordinates() {
		return recWithCoordinates;
	}

	public void setRecWithCoordinates(int recWithCoordinates) {
		this.recWithCoordinates = recWithCoordinates;
	}

	public int getRecWithCountry() {
		return recWithCountry;
	}

	public void setRecWithCountry(int recWithCountry) {
		this.recWithCountry = recWithCountry;
	}

	public int getRecWithAltitude() {
		return recWithAltitude;
	}

	public void setRecWithAltitude(int recWithAltitude) {
		this.recWithAltitude = recWithAltitude;
	}

	public int getRecWithDate() {
		return recWithDate;
	}

	public void setRecWithDate(int recWithDate) {
		this.recWithDate = recWithDate;
	}


	public int getNumCountries() {
		return numCountries;
	}

	public void setNumCountries(int numCountries) {
		this.numCountries = numCountries;
	}

	public int getNumTerminalTaxa() {
		return numTerminalTaxa;
	}

	public void setNumTerminalTaxa(int numTerminalTaxa) {
		this.numTerminalTaxa = numTerminalTaxa;
	}

	public int getNumGenera() {
		return numGenera;
	}

	public void setNumGenera(int numGenera) {
		this.numGenera = numGenera;
	}

	public int getNumFamilies() {
		return numFamilies;
	}

	public void setNumFamilies(int numFamilies) {
		this.numFamilies = numFamilies;
	}

	public int getNumOrders() {
		return numOrders;
	}

	public void setNumOrders(int numOrders) {
		this.numOrders = numOrders;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	public int getNumPhyla() {
		return numPhyla;
	}

	public void setNumPhyla(int numPhyla) {
		this.numPhyla = numPhyla;
	}

	public int getNumKingdoms() {
		return numKingdoms;
	}

	public void setNumKingdoms(int numKingdoms) {
		this.numKingdoms = numKingdoms;
	}

	public int getNumSpecies() {
		return numSpecies;
	}

	public void setNumSpecies(int numSpecies) {
		this.numSpecies = numSpecies;
	}

	@Transient
	public String getTapirEndpoint(){
		return String.format("%s/tapir", getResourceBaseUrl());
	}
	
	@Transient
	public String getWfsEndpoint(){
		return String.format("%s/wfs", getResourceBaseUrl());
	}
	    

    
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}
}
