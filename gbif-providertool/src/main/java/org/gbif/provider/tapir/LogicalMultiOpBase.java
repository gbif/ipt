package org.gbif.provider.tapir;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public abstract class LogicalMultiOpBase extends LogicalOperator {
	private List<LogicalOperator> operands;

	public List<LogicalOperator> getOperands() {
		return operands;
	}

	public void addOperand(LogicalOperator operand) {
		this.operands.add(operand);
	}
	
	protected abstract String getOperatorSymbol();

	public String toString(){
		return StringUtils.join(operands, getOperatorSymbol());
	}
}
