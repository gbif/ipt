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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.util.AppConfig;
import org.hibernate.annotations.MapKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;


/**
 * An abstract datasource driven external resource 
 * @author markus
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DatasourceBasedResource extends Resource {
	private static Log log = LogFactory.getLog(DatasourceBasedResource.class);
	private String serviceName;
	private String jdbcDriverClass = "com.mysql.jdbc.Driver";
	private String jdbcUrl = "jdbc:mysql://localhost/YOUR_DATABASE";
	private String jdbcUser;
	private String jdbcPassword;
	private UploadEvent lastUpload;
	private Integer uploadScheduleIntervalInDays;
	private ViewCoreMapping coreMapping;
	// extension mappings, not including the core mapping
	private Map<Long, ViewExtensionMapping> extensionMappings = new HashMap<Long, ViewExtensionMapping>();
	// transient properties
	private DataSource datasource;

	
	
	@Column(length=32, unique=true)
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		if (serviceName!=null){
			serviceName=serviceName.toLowerCase().trim().replace(" ", "_");
		}
		this.serviceName = serviceName;
	}
	@Override
	public void setTitle(String title) {
		this.title = title;
		if (serviceName == null){
			setServiceName(title);
		}
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
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="last_upload_event_fk", nullable=true) 
	public UploadEvent getLastUpload() {
		return lastUpload;
	}
	public void setLastUpload(UploadEvent lastUpload) {
		this.lastUpload = lastUpload;
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
	public Map<Long, ViewExtensionMapping> getExtensionMappings() {
		return extensionMappings;
	}
	public void setExtensionMappings(Map<Long, ViewExtensionMapping> extensionMappings) {
		this.extensionMappings = extensionMappings;
	}
	public void addExtensionMapping(ViewExtensionMapping mapping) {
		mapping.setResource(this);
		this.extensionMappings.put(mapping.getExtension().getId(), mapping);
	}

	@Transient
	public ViewMappingBase getExtensionMapping(Extension extension) {
		ViewMappingBase result = null;
		for (ViewMappingBase vm : this.getAllMappings()){
			if(vm.getExtension().equals(extension)){
				result = vm;
				break;
			}
		}
		return result;
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
				System.err.print("ClassNotFoundException: ");
				System.err.println(e.getMessage());
			} catch (Exception e) {
				datasource = null;
				log.debug(String.format("Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s, Password=%s", this.jdbcDriverClass, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword()), e);
			}			
		}else{
			datasource = null;
		}
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
	public boolean hasData(){
		if (lastUpload != null && lastUpload.getRecordsUploaded()>0){
			return true;
		}
		return false;
	}
	
	@Transient
	public int getRecordCount(){
		if (lastUpload != null){
			return lastUpload.getRecordsUploaded();
		}
		return 0;
	}
	@Transient
	public Date getLastUploadDate(){
		if (lastUpload != null){
			return lastUpload.getExecutionDate();
		}
		return null;
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
	
	
	@Transient
	public File getDataDir(){
    	File dir = new File(AppConfig.getDataDir(), getId().toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
		return dir;
	}

	@Transient
	public File getSourceDataDir(){
    	File dir = new File(AppConfig.getDataDir(), String.format("sourcedata/%s", getId()));
        if (!dir.exists()) {
            dir.mkdirs();
        }
		return dir;
	}
	
	@Transient
    public File getDumpFile(Extension extension) throws IOException{    	
		File file = new File(getDataDir(), String.format("%s.txt", extension.getTablename()));
		return file;
	}    

	@Transient
    public File getDumpSourceFile(Extension extension) throws IOException{    	
		File file = new File(getSourceDataDir(), String.format("%s.txt", extension.getTablename()));
		return file;
	}    

	@Transient
    public File getDumpArchiveFile(){
		File file = new File(getDataDir(), "data.zip");
		return file;    	
    }

	@Transient
    public File getLogoFile(){
		File file = new File(getDataDir(), "logo.jpg");
		return file;    	
    }

	@Transient
	public String getResourceBaseUrl(){
		String base = AppConfig.getAppBaseUrl();
    	return String.format("%s/data/%s", base, getId().toString());
	}

	@Transient
    public String getDetailsUrl(String guid){
		String base = AppConfig.getAppBaseUrl();
    	return String.format("%s/%s/detail.html", getResourceBaseUrl(), guid);
    }

	@Transient
    public String getDumpArchiveUrl(){
		return String.format("%s/data.zip", getResourceBaseUrl());
    }

	@Transient
	public boolean isUploading(){
		//FIXME: implement this via lastUploadLog or new isUploading boolean flag
		return false;
	}
	
	public Integer getUploadScheduleIntervalInDays() {
		return uploadScheduleIntervalInDays;
	}
	public void setUploadScheduleIntervalInDays(Integer uploadScheduleIntervalInDays) {
		this.uploadScheduleIntervalInDays = uploadScheduleIntervalInDays;
	}
	
}
