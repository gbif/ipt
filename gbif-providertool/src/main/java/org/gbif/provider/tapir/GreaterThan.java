package org.gbif.provider.tapir;

public class GreaterThan extends ComparisonOperator {

	@Override
	protected String getOperatorSymbol() {
		return ">";
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
