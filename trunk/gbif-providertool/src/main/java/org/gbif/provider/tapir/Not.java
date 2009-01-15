package org.gbif.provider.tapir;


public class Not extends LogicalOperator {
	private BooleanOperator op;
	
	public Not() {
		log.debug("Creating " + this.getClass().getSimpleName());	
	}
	
	public BooleanOperator getOp() {
		return op;
	}

	public void setOp(BooleanOperator op) {
		this.op = op;
	}
	
	@Override
	public void addOperand(BooleanOperator operand) {
		setOp(operand);
	}

	public String toString(){
		return String.format("not (%s)", op);
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
