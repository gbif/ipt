package org.gbif.provider.tapir;

public class GreaterThanOrEquals extends ComparisonBinaryOperator {

	public GreaterThanOrEquals() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return ">=";
	}

}
