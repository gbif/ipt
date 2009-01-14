package org.gbif.provider.tapir;

public class LessThan extends ComparisonOperator {

	public LessThan() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	protected String getOperatorSymbol() {
		return "<";
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
