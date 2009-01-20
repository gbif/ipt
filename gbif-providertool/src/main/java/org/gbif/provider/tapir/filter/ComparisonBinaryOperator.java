package org.gbif.provider.tapir.filter;

import java.util.Map;

import org.gbif.provider.tapir.ParseException;

public abstract class ComparisonBinaryOperator extends ComparisonOperator{
	protected String value;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		log.debug("Setting value to: " + value);
		this.value = value;
	}
	/**
	 * Sets the value to be the first value found from the params map with the specified key
	 * @param params the parameters to read from
	 * @param key For the map
	 * @throws ParseException 
	 */
	public void setValue(Map params, String key) throws ParseException {
		String value = (String)params.get(key);
		if (value!=null) {
			setValue(value);
		} else {
			log.warn("No values in the parameters[" + params + "] for the key[" + key + "]");
			throw new ParseException("ERROR: No values for key[" + key + "]");
		}
	}
	
	public String toHQL() {
		return String.format("%s %s '%s'", property.getHQLName(), getOperatorSymbol(), value);
	}
	public String toString(){
		return String.format("%s %s '%s'", property.getQualName(), getOperatorSymbol(), value);
	}

}
