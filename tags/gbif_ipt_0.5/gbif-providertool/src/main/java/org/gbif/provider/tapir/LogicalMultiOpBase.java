package org.gbif.provider.tapir;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public abstract class LogicalMultiOpBase extends BooleanOpBase {
	private List<BooleanOpBase> operands;

	public List<BooleanOpBase> getOperands() {
		return operands;
	}

	public void addOperand(BooleanOpBase operand) {
		this.operands.add(operand);
	}
	
	protected abstract String getOperatorSymbol();

	public String toString(){
		return StringUtils.join(operands, getOperatorSymbol());
	}
}
