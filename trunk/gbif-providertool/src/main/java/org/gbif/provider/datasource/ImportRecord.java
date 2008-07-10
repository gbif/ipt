package org.gbif.provider.datasource;

import java.util.HashMap;
import java.util.Map;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;


/**
 * A raw record with column values mapped to an extension property already, the properties map.
 * @author markus
 *
 */
public class ImportRecord extends CoreRecord {
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();
	private Extension extension;

	
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
}
