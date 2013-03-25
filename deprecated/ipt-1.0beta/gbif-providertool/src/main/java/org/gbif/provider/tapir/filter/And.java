package org.gbif.provider.tapir.filter;

public class And extends LogicalMultiOperator {
	public And() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "and";
	}
}
