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

import org.gbif.provider.model.hibernate.Timestampable;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;


/**
 * A specific resource representing the datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends Resource{
	private String serviceName;
	private String sourceJdbcConnection;
	private Date lastImport;
	private Integer recordCount;
	
	@Column(length=16)
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getSourceJdbcConnection() {
		return sourceJdbcConnection;
	}
	public void setSourceJdbcConnection(String sourceJdbcConnection) {
		this.sourceJdbcConnection = sourceJdbcConnection;
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

}
