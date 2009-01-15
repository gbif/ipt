package org.gbif.provider.tapir;

public class Or extends LogicalMultiOperator {
	public Or() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "or";
	}

}
