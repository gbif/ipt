package org.gbif.provider.tapir;

public abstract class ComparisonBinaryOperator extends ComparisonOperator{
	protected String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public String toHQL() {
		return String.format("%s %s '%s'", property.getHQLName(), getOperatorSymbol(), value);
	}
	public String toString(){
		return String.format("%s %s '%s'", property.getName(), getOperatorSymbol(), value);
	}

}
