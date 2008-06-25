package org.gbif.provider.upload;

import java.util.Map;

import org.gbif.provider.model.ExtensionProperty;

public interface SourceRow {
	public String getLocalId();
	public void setLocalId(String id);
	public Map<ExtensionProperty, String> getPropertyValues();
	public void addPropertyValue(ExtensionProperty property, String value);
}
