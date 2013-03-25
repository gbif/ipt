package org.gbif.provider.tapir.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.util.RecursiveIterator;

public abstract class LogicalMultiOperator extends LogicalOperator {
	private List<BooleanOperator> operands = new ArrayList<BooleanOperator>();

	public List<BooleanOperator> getOperands() {
		return operands;
	}

	public void addOperand(BooleanOperator operand) {
		this.operands.add(operand);
	}
	
	protected abstract String getOperatorSymbol();

	@Override
	public String toHQL() {
		if (operands.size()<2){
			throw new IllegalStateException("LogicalMultiOperator must have at least two operands");
		}
		List<String> operandsHQL = new ArrayList<String>();
		for (BooleanOperator op : operands){
			operandsHQL.add(op.toHQL());
		}
		return "("+ StringUtils.join(operandsHQL, " "+getOperatorSymbol()+" ") +")";
	}
	public String toString() {
		return getOperatorSymbol().toUpperCase()+": "+toStringRecursive();
	}
	public String toStringRecursive(){
		List<String> inner= new ArrayList<String>();
		if (operands.size()>1){
			for (BooleanOperator op : operands){				
				inner.add(op.toStringRecursive());
			}
		}else if (operands.size()>0){
			inner.add(operands.get(0).toStringRecursive());
			inner.add("?");
		}else{
			inner.add("?");
			inner.add("?");
		}
		return StringUtils.join(inner, " " + getOperatorSymbol() + " ");
	}
	
	public Iterator<BooleanOperator> iterator() {
		List<Iterator<BooleanOperator>> iters = new ArrayList<Iterator<BooleanOperator>>();
		for (BooleanOperator op : operands){
			iters.add(op.iterator());
		}
		return new RecursiveIterator<BooleanOperator>(this, iters);
	}
	
}
