package org.gbif.provider.tapir;

import org.gbif.provider.model.ExtensionProperty;

public class IsNull extends LogicalOperator implements BooleanOperator {
	private ExtensionProperty property;
	
	public IsNull() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
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
	
	protected String getOperatorSymbol() {
		return "is null";
	}

	public String toString(){
		return String.format("%s %s", property.getName(), getOperatorSymbol());
	}
	
	public boolean evaluate() {
		return false;
	}
	public String toSQL() {
		return null;
	}
}
