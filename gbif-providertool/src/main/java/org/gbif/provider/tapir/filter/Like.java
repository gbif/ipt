package org.gbif.provider.tapir.filter;

public class Like extends ComparisonBinaryOperator {

	public Like() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	@Override
	public void setValue(String value) {
		if (value!=null){
			// replace TAPIR wildcard with H2 wildcard
			value = value.replace("*", "%");
		}
		super.setValue(value);
	}

	@Override
	protected String getOperatorSymbol() {
		return "like";
	}
}
