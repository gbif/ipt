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


/**
 * @author markus
 *
 */
@Entity
public class Datasource implements Timestampable{
	private Long id;
	private String serviceName;
	private Date modified;
	private ResourceMetadata metadata;
	private String sourceJdbcConnection;
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=16)
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
			
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	@ManyToOne
	public ResourceMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}
	
	public String getSourceJdbcConnection() {
		return sourceJdbcConnection;
	}
	public void setSourceJdbcConnection(String sourceJdbcConnection) {
		this.sourceJdbcConnection = sourceJdbcConnection;
	}

}
