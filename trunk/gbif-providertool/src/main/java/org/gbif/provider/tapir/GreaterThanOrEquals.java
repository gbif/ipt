package org.gbif.provider.tapir;

public class GreaterThanOrEquals extends ComparisonOperator {

	public GreaterThanOrEquals() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return ">=";
	}

	public boolean evaluate() {
		// TODO Auto-generated method stub
		return false;
	}

	public String toSQL() {
		// TODO Auto-generated method stub
		return null;
	}

}
