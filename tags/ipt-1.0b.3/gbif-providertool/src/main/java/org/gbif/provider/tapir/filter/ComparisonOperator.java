package org.gbif.provider.tapir.filter;

import java.util.Iterator;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.tapir.filter.Filter.FilterIterator;
import org.gbif.provider.util.RecursiveIterator;

public abstract class ComparisonOperator extends BooleanOperator {
	protected ExtensionProperty property;	
	
	public ExtensionProperty getProperty() {
		return property;
	}
	public void setProperty(ExtensionProperty property) {
		log.debug("Setting property to: " + property.getQualName());
		if (property.getName()==null){
			property.setName(property.getQualName());
		}
		this.property = property;
	}
	public void setProperty(String propertyAsString) {
		setProperty(new ExtensionProperty(propertyAsString));
	}
	
	protected abstract String getOperatorSymbol();

	public Iterator<BooleanOperator> iterator() {
		return new RecursiveIterator<BooleanOperator>(this);
	}
}
