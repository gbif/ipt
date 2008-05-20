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
	private String id;
	private Date modified;
	private ResourceMetadata metadata;
	private String sourceJdbcConnection;
	
	@Id @Column(length=16)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
