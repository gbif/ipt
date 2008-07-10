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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.MapKey;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;


/**
 * An abstract datasource driven external resource 
 * @author markus
 *
 */
@Entity
public class DatasourceBasedResource extends Resource {
	private String serviceName;
	private String jdbcDriverClass = "jdbc:mysql://localhost/providertoolkit";
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private Date lastImport;
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
	
	public int getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	@Transient
	public Map<Long, ViewMapping> getAllMappings() {
		Map<Long, ViewMapping> all = new HashMap<Long, ViewMapping>(extensionMappings);
		all.put(getCoreMapping().getExtension().getId(), getCoreMapping());
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
	public DataSource getDatasource() {
		if (datasource == null){
			this.udpateDatasource();
		}
		return datasource;
	}
	public void udpateDatasource() {
		String driverClassName = "org.postgresql.Driver";
		driverClassName="com.mysql.jdbc.Driver";
		if (this.getJdbcUrl() != null){
			datasource = new SingleConnectionDataSource(driverClassName, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword(), true);			
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

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals2(Object o) {
		if (o == this){
			return true;
		}
		if (!(o instanceof DatasourceBasedResource)) {
			return false;
		}
        final DatasourceBasedResource resource = (DatasourceBasedResource) o;
        
        return this.hashCode() == resource.hashCode();
	}
	

}
