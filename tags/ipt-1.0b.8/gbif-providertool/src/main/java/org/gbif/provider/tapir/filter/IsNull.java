package org.gbif.provider.tapir.filter;

import org.gbif.provider.model.ExtensionProperty;

public class IsNull extends ComparisonOperator {
	public IsNull() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
		
	protected String getOperatorSymbol() {
		return "is null";
	}

	public String toHQL(){
		return String.format("%s %s", property.getHQLName(), getOperatorSymbol());
	}
	public String toString(){
		return String.format("%s %s", property.getQualName(), getOperatorSymbol());
	}
}
