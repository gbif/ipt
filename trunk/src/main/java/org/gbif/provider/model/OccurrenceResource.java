/**
 * 
 */
package org.gbif.provider.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

import org.gbif.provider.model.hibernate.Timestampable;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * A specific resource representing the datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends Resource{
	private String serviceName;
	private String jdbcDriverClass = "jdbc:mysql://localhost/providertoolkit";
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private Date lastImport;
	private Integer recordCount;
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
	
	public Integer getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
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
		datasource = new SingleConnectionDataSource(driverClassName, this.getJdbcUrl(), this.getJdbcUser(), this.getJdbcPassword(), true);			
	}
	@Transient
	public boolean isValidConnection(){
		boolean isValidConnection = false;
		try {
			Connection con = getDatasource().getConnection();
			isValidConnection = true;
		} catch (SQLException e) {
			isValidConnection = false;
		}
		return isValidConnection ;
	}	
}
