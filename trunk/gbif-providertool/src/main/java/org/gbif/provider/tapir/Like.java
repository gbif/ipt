package org.gbif.provider.tapir;

public class Like extends ComparisonBinaryOperator {

	public Like() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "like";
	}
}
