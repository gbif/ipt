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
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.util.ConfigUtil;
import org.hibernate.annotations.MapKey;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;


/**
 * An abstract datasource driven external resource 
 * @author markus
 *
 */
@Entity
public class DatasourceBasedResource extends Resource {
	private static Log log = LogFactory.getLog(DatasourceBasedResource.class);
	private String serviceName;
	private String jdbcDriverClass = "com.mysql.jdbc.Driver";
	private String jdbcUrl = "jdbc:mysql://localhost/YOUR_DATABASE";
	private String jdbcUser;
	private String jdbcPassword;
	private Date lastImport;
	private Integer lastImportSourceId;
	private int recordCount;
	private CoreViewMapping coreMapping;
	// extension mappings, not including the core mapping
	private Map<Long, ViewMapping> extensionMappings = new HashMap<Long, ViewMapping>();
	// transient properties
	private DataSource datasource;

	
	
	@Column(length=32)
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
	
	
	public Date getLastImport() {
		return lastImport;
	}
	public void setLastImport(Date lastImport) {
		this.lastImport = lastImport;
	}
	
	public Integer getLastImportSourceId() {
		return lastImportSourceId;
	}
	public void setLastImportSourceId(Integer lastImportSourceId) {
		this.lastImportSourceId = lastImportSourceId;
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	@Transient
	public Set<ViewMapping> getAllMappings() {
		Set<ViewMapping> all = new HashSet<ViewMapping>(extensionMappings.values());
		if (getCoreMapping() != null){
			all.add(getCoreMapping());
		}
		return all;
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	public CoreViewMapping getCoreMapping() {
		return coreMapping;
	}
	public void setCoreMapping(CoreViewMapping coreMapping) {
		//coreMapping.setResource(this);
		this.coreMapping = coreMapping;
	}
	
	@OneToMany(mappedBy="resource", cascade=CascadeType.ALL)
	@MapKey(columns = @Column(name = "extension_id"))
	public Map<Long, ViewMapping> getExtensionMappings() {
		return extensionMappings;
	}
	public void setExtensionMappings(Map<Long, ViewMapping> extensionMappings) {
		this.extensionMappings = extensionMappings;
	}
	public void addExtensionMapping(ViewMapping mapping) {
		mapping.setResource(this);
		this.extensionMappings.put(mapping.getExtension().getId(), mapping);
	}

	@Transient
	public ViewMapping getExtensionMapping(Extension extension) {
		ViewMapping result = null;
		for (ViewMapping vm : this.getAllMappings()){
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
				datasource = new SingleConnectionDataSource(this.jdbcDriverClass, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword(), true);
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
	public boolean isValidConnection(){
		boolean isValidConnection = false;
		try {
			DataSource dsa = getDatasource();
			if (dsa!=null){
				Connection con = dsa.getConnection();
				isValidConnection = true;
			}
		} catch (SQLException e) {
			isValidConnection = false;
		}
		return isValidConnection ;
	}
	
	@Transient
	public boolean hasData(){
		if (recordCount > 0){
			return true;
		}
		return false;
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
    	File dir = new File(ConfigUtil.getWebappDir(), String.format("data/%s", getServiceName()));
		return dir;
	}
	
	@Transient
	public String getResourceBaseUrl(){
    	return String.format("%s/data/%s", ConfigUtil.getAppBaseUrl(), getServiceName());
	}

}
