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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.model.dto.StatsCount;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DatasourceBasedResource {
	public static final Long CORE_EXTENSION_ID = 1l;
	private BBox bbox;
	// cached statistics
	private int recTotal;
	private int recWithCoordinates;
	private int recWithCountry;
	private int recWithAltitude;
	private int recWithDate;
	private int numCountries;
	// distinct number of Region entities
	private int numRegions;
	private int numTerminalRegions;
	private Map<String, Long> numTaxaByCountry = new HashMap<String, Long>();
	
	private int numGenera;
	private int numFamilies;
	private int numOrders;
	private int numClasses;
	private int numPhyla;
	private int numKingdoms;
	// distinct number of Taxon entities
	private int numTaxa;
	private int numTerminalTaxa;
	
	public static OccurrenceResource newInstance(Extension core){
		OccurrenceResource resource =  new OccurrenceResource();
		// ensure that core mapping exists
		ViewCoreMapping coreVM = new ViewCoreMapping();
		coreVM.setExtension(core);
		coreVM.setResource(resource);
		resource.setCoreMapping(coreVM);
		resource.resetStats();
		return resource;
	}

	public BBox getBbox() {
		return bbox;
	}
	public void setBbox(BBox bbox) {
		this.bbox = bbox;
	}

	public int getRecTotal() {
		return recTotal;
	}

	public void setRecTotal(int recTotal) {
		this.recTotal = recTotal;
		// also set the same number in core mapping
		getCoreMapping().setRecTotal(recTotal);
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

	
	@CollectionOfElements
	@MapKey(columns = @Column(name = "country", length=64))
	public Map<String, Long> getNumTaxaByCountry() {
		return numTaxaByCountry;
	}
	public void setNumTaxaByCountry(Map<String, Long> numTaxaByCountry) {
		this.numTaxaByCountry = numTaxaByCountry;
	}
	@Transient
	public List<StatsCount> getTaxaStatsByCountry() {
		List<StatsCount> taxaStatsByCountry = new ArrayList<StatsCount>();
		for (String country : numTaxaByCountry.keySet()){
			taxaStatsByCountry.add(new StatsCount(country, numTaxaByCountry.get(country)));
		}
		return taxaStatsByCountry;
	}


	
	public int getNumRegions() {
		return numRegions;
	}

	public void setNumRegions(int numRegions) {
		this.numRegions = numRegions;
	}

	public int getNumTaxa() {
		return numTaxa;
	}

	public void setNumTaxa(int numTaxa) {
		this.numTaxa = numTaxa;
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

	
	public int getNumTerminalRegions() {
		return numTerminalRegions;
	}

	public void setNumTerminalRegions(int numTerminalRegions) {
		this.numTerminalRegions = numTerminalRegions;
	}

	public int getNumTerminalTaxa() {
		return numTerminalTaxa;
	}

	public void setNumTerminalTaxa(int numTerminalTaxa) {
		this.numTerminalTaxa = numTerminalTaxa;
	}


    
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}

	public void resetStats() {
		recTotal=0;
		recWithCoordinates=0;
		recWithCountry=0;
		recWithAltitude=0;
		recWithDate=0;
		numCountries=0;
		numRegions=0;
		numTaxaByCountry.clear();
		
		numTaxa=0;
		numGenera=0;
		numFamilies=0;
		numOrders=0;
		numClasses=0;
		numPhyla=0;
		numKingdoms=0;
		
		setLastUpload(null);		
	}
}
