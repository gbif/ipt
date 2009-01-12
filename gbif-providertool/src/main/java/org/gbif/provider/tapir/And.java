package org.gbif.provider.tapir;

public class And extends LogicalMultiOpBase {

	@Override
	protected String getOperatorSymbol() {
		return "and";
	}

	public boolean evaluate() {
		// TODO Auto-generated method stub
		return false;
	}

	public String toHQL() {
		// TODO Auto-generated method stub
		return null;
	}

}
