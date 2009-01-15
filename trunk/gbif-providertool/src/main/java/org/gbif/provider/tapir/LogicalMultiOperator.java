package org.gbif.provider.tapir;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public abstract class LogicalMultiOperator extends LogicalOperator {
	private List<BooleanOperator> operands = new LinkedList<BooleanOperator>();

	public List<BooleanOperator> getOperands() {
		return operands;
	}

	public void addOperand(BooleanOperator operand) {
		this.operands.add(operand);
	}
	
	protected abstract String getOperatorSymbol();

	@Override
	public String toHQL() {
		List<String> operandsHQL = new ArrayList<String>();
		for (BooleanOperator op : operands){
			operandsHQL.add("("+op.toHQL()+")");
		}
		return StringUtils.join(operandsHQL, " " + getOperatorSymbol() + " ");
	}
	public String toString() {
		List<String> operandsString = new ArrayList<String>();
		for (BooleanOperator op : operands){
			operandsString.add("("+op.toString()+")");
		}
		return StringUtils.join(operandsString, " " + getOperatorSymbol() + " ");
	}
}
