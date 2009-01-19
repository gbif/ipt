package org.gbif.provider.tapir.filter;

import java.util.Map;

public abstract class ComparisonBinaryOperator extends ComparisonOperator{
	protected String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * Sets the value to be the first value found from the params map with the specified key
	 * @param params the parameters to read from
	 * @param key For the map
	 */
public void setValue(Map params, String key) {
		String[] values = (String[])params.get(key);
		if (values!=null && values.length==1) {
			this.value = values[0];
		} else if (values!=null && values.length>1) {
			log.info("Using the first value for the filter from the options[" + values + "]");
			this.value = values[0];
		} else {
			log.warn("No values in the parameters[" + params + "] for the key[" + key + "]");
			this.value="ERROR: No values for key[" + key + "]";
		}
	}
	
	public String toHQL() {
		return String.format("%s %s '%s'", property.getHQLName(), getOperatorSymbol(), value);
	}
	public String toString(){
		return String.format("%s %s '%s'", property.getQualName(), getOperatorSymbol(), value);
	}

}
