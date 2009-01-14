package org.gbif.provider.tapir;

public class Like extends ComparisonOperator {

	public Like() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "like";
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
