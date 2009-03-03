package org.gbif.provider.tapir.filter;

public class GreaterThan extends ComparisonBinaryOperator {

	public GreaterThan() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return ">";
	}

}
