package org.gbif.provider.tapir;

import org.apache.commons.lang.StringUtils;

public class Not extends BooleanOpBase {
	private BooleanOpBase op;

	public BooleanOpBase getOp() {
		return op;
	}

	public void setOp(BooleanOpBase op) {
		this.op = op;
	}

	public String toString(){
		return String.format("not (%s)", op);
	}
}
