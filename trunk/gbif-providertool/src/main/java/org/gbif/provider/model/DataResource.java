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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.MapKey;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;


/**
 * An abstract resource which contains data in addition to metadata 
 * @author markus
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DataResource extends Resource {
	private static Log log = LogFactory.getLog(DataResource.class);
	private String jdbcDriverClass = "com.mysql.jdbc.Driver";
	private String jdbcUrl = "jdbc:mysql://localhost/YOUR_DATABASE";
	private String jdbcUser;
	private String jdbcPassword;
	private Set<SourceBase> sources;
	private UploadEvent lastUpload;
	private ViewCoreMapping coreMapping;
	// extension mappings, not including the core mapping
	private Map<Long, ViewExtensionMapping> extensionMappings = new HashMap<Long, ViewExtensionMapping>();
	//
	// transient properties
	//
	private DataSource datasource;
	// counts by interpreted rank
	private int numSpecies;
	private int numGenera;
	private int numFamilies;
	private int numOrders;
	private int numClasses;
	private int numPhyla;
	private int numKingdoms;
	// other counts
	private int numTaxa;
	private int numTerminalTaxa;
	private int numAccepted;
	private int numSynonyms;

	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="last_upload_event_fk", nullable=true) 
	public UploadEvent getLastUpload() {
		return lastUpload;
	}
	public void setLastUpload(UploadEvent lastUpload) {
		this.lastUpload = lastUpload;
	}
	
	@Transient
	public int getRecTotal() {
		return getCoreMapping().getRecTotal();
	}
	public void setRecTotal(int recTotal) {
		getCoreMapping().setRecTotal(recTotal);
	}
	
	@Transient
	public Set<ViewMappingBase> getAllMappings() {
		Set<ViewMappingBase> all = new HashSet<ViewMappingBase>(extensionMappings.values());
		if (getCoreMapping() != null){
			all.add(getCoreMapping());
		}
		return all;
	}
	
	@OneToOne(cascade=CascadeType.ALL, mappedBy="resource")
//    @JoinColumn(insertable=false, updatable=false)
//    @org.hibernate.annotations.Where(clause="mapping_type='CORE'")        
	public ViewCoreMapping getCoreMapping() {
		return coreMapping;
	}
	public void setCoreMapping(ViewCoreMapping coreMapping) {
		this.coreMapping = coreMapping;
	}
	


	@OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name="resource_fk", insertable=false, updatable=false)
    @org.hibernate.annotations.Where(clause="mapping_type='EXT'")        
	@MapKey(columns = @Column(name = "extension_fk"))
	public Map<Long, ViewExtensionMapping> getExtensionMappingsMap() {
		return extensionMappings;
	}
	public void setExtensionMappingsMap(Map<Long, ViewExtensionMapping> extensionMappings) {
		this.extensionMappings = extensionMappings;
	}
	@Transient
	public List<ViewExtensionMapping> getExtensionMappings() {
		return new ArrayList<ViewExtensionMapping>(extensionMappings.values());
	}
	public void addExtensionMapping(ViewExtensionMapping mapping) {
		mapping.setResource(this);
		this.extensionMappings.put(mapping.getExtension().getId(), mapping);
	}
	public void removeExtensionMapping(ViewMappingBase mapping) {
		this.extensionMappings.remove(mapping.getExtension().getId());
	}

	@Transient
	public ViewMappingBase getExtensionMapping(Extension extension) {
		if (extension.equals(getCoreMapping().getExtension())){
			return getCoreMapping();
		}
		return this.extensionMappings.get(extension.getId());
	}
	
	@Column(length=64)
	public String getJdbcDriverClass() {
		return jdbcDriverClass;
	}
	public void setJdbcDriverClass(String jdbcDriverClass) {
		this.jdbcDriverClass = jdbcDriverClass;
	}
	
	@Column(length=128)
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	
	@Column(length=64)
	public String getJdbcUser() {
		return jdbcUser;
	}
	public void setJdbcUser(String jdbcUser) {
		this.jdbcUser = jdbcUser;
	}
	
	@Column(length=64)
	public String getJdbcPassword() {
		return jdbcPassword;
	}
	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="resource")
	public Set<SourceBase> getSources() {
		return sources;
	}
	public void setSources(Set<SourceBase> sources) {
		this.sources = sources;
	}
    
	@Transient
	public DataSource getDatasource() {
		if (datasource == null){
			this.udpateDatasource();
		}
		return datasource;
	}
	public void udpateDatasource() {
		if (this.getJdbcUrl() != null && jdbcDriverClass != null){			
			try {
				Class.forName(this.jdbcDriverClass);	
				Driver driver = DriverManager.getDriver(this.getJdbcUrl());				
				datasource = new SimpleDriverDataSource(driver, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword());
			} catch(java.lang.ClassNotFoundException e) {
				datasource = null;
				log.debug(String.format("Couldnt load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s", this.jdbcDriverClass, this.getJdbcUrl()), e);
			} catch (Exception e) {
				datasource = null;
				log.debug(String.format("Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s, Password=%s", this.jdbcDriverClass, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword()), e);
			}			
		}else{
			datasource = null;
		}
	}	
	
	@Transient
	public boolean hasDbConnection(){
		boolean hasDbConnection = false;
		try {
			DataSource dsa = getDatasource();
			if (dsa!=null){
				Connection con = dsa.getConnection();
				hasDbConnection = true;
			}
		} catch (SQLException e) {
			hasDbConnection = false;
		}
		return hasDbConnection ;
	}
	
	@Transient
	public boolean hasMetadata(){
		boolean result = false;
		if (getTitle() != null && getTitle().trim().length() > 0){
			result = true;
		}
		return result;
	}


	@Transient
	public boolean hasData(){
		if (lastUpload != null && lastUpload.getRecordsUploaded()>0){
			return true;
		}
		return false;
	}
	
	@Transient
	public Date getLastUploadDate(){
		if (lastUpload != null){
			return lastUpload.getExecutionDate();
		}
		return null;
	}
	
	@Transient
	@Override
	public boolean isDataResource(){
		return true;
	}

	/**
	 * Checks to see whether a resource has the minimal mappings to proceed with an upload
	 * @return
	 */
	@Transient
	public boolean hasMinimalMapping() {
		boolean result = false;
		if (coreMapping.getPropertyMappings().size() > 0){
			result = true;
		}
		return result;
	}
		
	
	/**
	 * Reset all cached stats, log events etc, so that it looks as if the resource was just created (doesnt change the created timestamp though)
	 */
	public void resetStats(){
		numSpecies=0;
		numGenera=0;
		numFamilies=0;
		numOrders=0;
		numClasses=0;
		numPhyla=0;
		numKingdoms=0;

		numTaxa=0;
		numTerminalTaxa=0;
		numAccepted=0;
		numSynonyms=0;

		lastUpload=null;
		setRecTotal(0);
	}
	
	public int getNumSpecies() {
		return numSpecies;
	}
	public void setNumSpecies(int numSpecies) {
		this.numSpecies = numSpecies;
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
	public int getNumTaxa() {
		return numTaxa;
	}
	public void setNumTaxa(int numTaxa) {
		this.numTaxa = numTaxa;
	}
	public int getNumTerminalTaxa() {
		return numTerminalTaxa;
	}
	public void setNumTerminalTaxa(int numTerminalTaxa) {
		this.numTerminalTaxa = numTerminalTaxa;
	}	
	public int getNumAccepted() {
		return numAccepted;
	}
	public void setNumAccepted(int numAccepted) {
		this.numAccepted = numAccepted;
	}
	public int getNumSynonyms() {
		return numSynonyms;
	}
	public void setNumSynonyms(int numSynonyms) {
		this.numSynonyms = numSynonyms;
	}	
	
}
