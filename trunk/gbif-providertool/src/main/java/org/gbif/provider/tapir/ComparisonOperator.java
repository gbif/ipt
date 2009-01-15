package org.gbif.provider.tapir;

import org.gbif.provider.model.ExtensionProperty;

public abstract class ComparisonOperator extends BooleanOperator {
	protected ExtensionProperty property;	
	
	public ExtensionProperty getProperty() {
		return property;
	}
	public void setProperty(ExtensionProperty property) {
		log.debug("Setting property to: " + property.getQualName());
		property.setName(property.getQualName());
		this.property = property;
	}
	public void setProperty(String propertyAsString) {
		setProperty(new ExtensionProperty(propertyAsString));
	}
	
	protected abstract String getOperatorSymbol();

}
