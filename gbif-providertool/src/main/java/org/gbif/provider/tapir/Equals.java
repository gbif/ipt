package org.gbif.provider.tapir;

public class Equals extends ComparisonOperator {

	public Equals() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "=";
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
