package org.gbif.provider.tapir.filter;

public class LessThanOrEquals extends ComparisonBinaryOperator {

	public LessThanOrEquals() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "<=";
	}
}
