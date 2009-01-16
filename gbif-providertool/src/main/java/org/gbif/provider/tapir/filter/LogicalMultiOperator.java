package org.gbif.provider.tapir.filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
		String inner;
		if (operands.size()>1){
			inner = StringUtils.join(operands, " " + getOperatorSymbol() + " ");
		}else if (operands.size()>0){
			inner = operands.get(0) + " " + getOperatorSymbol() + " ?";
		}else{
			inner = "? " + getOperatorSymbol() + " ?";
		}
		return "("+inner+")";
	}
}
