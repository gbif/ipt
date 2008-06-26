package org.gbif.provider.datasource.impl;

import java.util.HashMap;
import java.util.Map;

import org.gbif.provider.datasource.SourceRow;
import org.gbif.provider.model.ExtensionProperty;

public class SourceRowImpl implements SourceRow{
	private Map<ExtensionProperty, String> data = new HashMap<ExtensionProperty, String>();
	private String id;
	
	public void addPropertyValue(ExtensionProperty property, String value) {
		data.put(property, value);
		
	}

	public String getLocalId() {
		return id;
	}
	public void  setLocalId(String id) {
		this.id=id;
	}
	
	public Map<ExtensionProperty, String> getPropertyValues() {
		return data;
	}

}
