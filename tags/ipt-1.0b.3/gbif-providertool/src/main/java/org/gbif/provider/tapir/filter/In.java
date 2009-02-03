package org.gbif.provider.tapir.filter;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.ExtensionProperty;
import org.opengis.filter.capability.ComparisonOperators;

public class In extends ComparisonOperator{
	private List<String> literals = new LinkedList<String>();
	public In() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}

	/**
	 * Another hack for how we use digester
	 */
	public void setValue(String value) {
		literals.add("'" + value + "'");
	}	
	public void addValue(String value) {
		literals.add("'" + value + "'");
	}	
	
	public String toHQL() {
		return String.format("%s %s (%s)", property.getHQLName(), getOperatorSymbol(), StringUtils.join(literals, ","));
	}
	public String toString() {
		return String.format("%s %s (%s)", property.getQualName(), getOperatorSymbol(), StringUtils.join(literals, ","));
	}

	protected String getOperatorSymbol() {
		return "in";
	}
}
