package org.gbif.provider.tapir;

import org.apache.commons.lang.StringUtils;

public class Not extends LogicalOperator {
	private LogicalOperator op;

	public LogicalOperator getOp() {
		return op;
	}

	public void setOp(LogicalOperator op) {
		this.op = op;
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
