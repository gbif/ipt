package org.gbif.provider.datasource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.NotNull;


/**
 * A raw record with column values mapped to an extension property already, the properties map.
 * @author markus
 *
 */
//implements CoreRecord<ImportRecord>
public class ImportRecord  {
	private static I18nLog logdb = I18nLogFactory.getLog(ImportRecord.class);

	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();
	private Extension extension;
	// for core record
	private Long id;
	private String localId;
	private String guid;
	private String link;
	private boolean isDeleted;
	private boolean isProblematic;
	private Date modified;
	private DatasourceBasedResource resource;

	
	
	public Map<ExtensionProperty, String> getProperties() {
		return properties;
	}
	private void setProperties(Map<ExtensionProperty, String> properties) {
		this.properties = properties;
	}
	public void setPropertyValue(ExtensionProperty property, String value) {
		// check if this is the first property ever set. 
		// If so, remember the extension and check all further added properties
		if (extension == null){
			extension = property.getExtension();
		}else{
			if (!extension.equals(property.getExtension())){
				throw new IllegalArgumentException();
			}
		}
		properties.put(property, value);
		
	}
	
	public String getPropertyValue(ExtensionProperty property){
		return properties.get(property);
	}
	
	/**
	 * Get the extension this record belongs to. 
	 * The property is being set by the set properties methods which guarantee that all properties
	 * of this record belong to the same extension.
	 * @return
	 */
	public Extension getExtension() {
		return extension;
	}
	
	
	// CORE RECORD
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}	

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}	

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isProblematic() {
		return isProblematic;
	}

	public void setProblematic(boolean isProblematic) {
		this.isProblematic = isProblematic;
	}
	
	public DatasourceBasedResource getResource() {
		return resource;
	}

	public void setResource(DatasourceBasedResource resource) {
		this.resource = resource;
	}
}
