package org.gbif.provider.tapir;

public class And extends LogicalMultiOpBase {

	@Override
	protected String getOperatorSymbol() {
		return "and";
	}

}
