package org.gbif.provider.tapir;

public class GreaterThan extends ComparisonOpBase {

	@Override
	protected String getOperatorSymbol() {
		return ">";
	}

}
