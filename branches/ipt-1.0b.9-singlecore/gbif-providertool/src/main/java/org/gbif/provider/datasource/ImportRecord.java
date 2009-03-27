package org.gbif.provider.datasource;

import java.util.HashMap;
import java.util.Map;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;


/**
 * A raw record with column values mapped to an extension property already, the properties map.
 * @author markus
 *
 */
public class ImportRecord  {
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();
	private Extension extension;
	private Long resourceId;
	// for core record
	private Long coreid;
	private String sourceId;
	private String guid;
	private String link;

	
	
	public ImportRecord(Long resourceId, String sourceId) {
		super();
		this.sourceId = sourceId;
		this.resourceId = resourceId;
	}
	
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
		return coreid;
	}

	public void setId(Long id) {
		this.coreid = id;
	}	

	public String getSourceId() {
		return sourceId;
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
	
	public Long getResourceId() {
		return resourceId;
	}
}
