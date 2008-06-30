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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;
import javax.persistence.CascadeType;

import org.gbif.provider.util.Constants;
import org.hibernate.annotations.MapKey;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.appfuse.model.Address;
import org.appfuse.model.User;


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
	//TODO: rename into extensionMappings. But JSPs need to be refactored too!
	private Map<Long, ViewMapping> mappings = new HashMap<Long, ViewMapping>();
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

	@OneToMany(mappedBy="resource", cascade=CascadeType.ALL)
	@MapKey(columns = @Column(name = "extension_id"))
	public Map<Long, ViewMapping> getMappings() {
		return mappings;
	}
	public void setMappings(Map<Long, ViewMapping> mappings) {
		this.mappings = mappings;
	}
	public void addMapping(ViewMapping mapping) {
		mapping.setResource(this);
		this.mappings.put(mapping.getExtension().getId(), mapping);
	}
	
	@OneToOne(mappedBy="resource", cascade=CascadeType.ALL)
	public CoreViewMapping getCoreMapping() {
		return coreMapping;
	}
	public void setCoreMapping(CoreViewMapping coreMapping) {
		this.coreMapping = coreMapping;
	}


	@Transient
	public Collection<ViewMapping> getExtensionMappings() {
		return getMappings().values();
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
	
	@Transient
	public boolean hasMapping() {
		boolean result = false;
		if (mappings.size() > 0){
			result = true;
		}
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("jdbcUser", this.jdbcUser)
				.append("created", this.getCreated()).append("modified",
						this.getModified()).append("id", this.getId()).append(
						"link", this.getLink()).append("jdbcUrl", this.jdbcUrl)
				.append("validConnection", this.isValidConnection()).append(
						"modifier", this.getModifier()).append(
						"jdbcDriverClass", this.jdbcDriverClass).append(
						"recordCount", this.recordCount).append("coreMapping",
						this.coreMapping).append("creator", this.getCreator())
				.append("description", this.getDescription()).append("title",
						this.getTitle())
				.append("serviceName", this.serviceName).append("mappings",
						this.mappings).append("datasource", this.datasource)
				.append("lastImport", this.lastImport).append(
						"extensionMappings", this.getExtensionMappings())
				.append("jdbcPassword", this.jdbcPassword).append("guid",
						this.getGuid()).toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (o == this){
			return true;
		}
		if (!(o instanceof DatasourceBasedResource)) {
			return false;
		}
        final DatasourceBasedResource resource = (DatasourceBasedResource) o;
        
        return this.hashCode() == resource.hashCode();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	        int result = 17;
	        result = (guid != null ? guid.hashCode() : 0);
	        result = 31 * result + (link != null ? link.hashCode() : 0);
	        result = 31 * result + (title != null ? title.hashCode() : 0);
	        result = 31 * result + (description != null ? description.hashCode() : 0);
	        result = 31 * result + (creator != null ? creator.hashCode() : 0);
	        result = 31 * result + (created != null ? created.hashCode() : 0);
	        result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
	        result = 31 * result + (modified != null ? modified.hashCode() : 0);
	        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
	        result = 31 * result + (jdbcDriverClass != null ? jdbcDriverClass.hashCode() : 0);
	        result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
	        result = 31 * result + (jdbcUser != null ? jdbcUser.hashCode() : 0);
	        result = 31 * result + (jdbcPassword != null ? jdbcPassword.hashCode() : 0);
	        result = 31 * result + (lastImport != null ? lastImport.hashCode() : 0);
	        result = 31 * result + recordCount;
	        result = 31 * result + (coreMapping != null ? coreMapping.hashCode() : 0);
	        result = 31 * result + (mappings != null ? mappings.hashCode() : 0);
	        return result;
	    }
}
