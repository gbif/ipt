package org.gbif.provider.tapir.filter;

import java.util.Iterator;

import org.gbif.provider.util.RecursiveIterator;


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
	public String toStringRecursive(){
		return toString();
	}

	public String toHQL() {
		return "("+ op.toHQL() +")";
	}
	
	public Iterator<BooleanOperator> iterator() {
		return new RecursiveIterator<BooleanOperator>(this, op.iterator());
	}

}
