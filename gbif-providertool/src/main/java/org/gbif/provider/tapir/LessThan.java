package org.gbif.provider.tapir;

public class LessThan extends ComparisonBinaryOperator {

	public LessThan() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "<";
	}
}
