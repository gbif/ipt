package org.gbif.provider.tapir;

public class And extends LogicalMultiOperator {
	public And() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
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
