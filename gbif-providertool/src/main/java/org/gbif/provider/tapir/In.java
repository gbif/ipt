package org.gbif.provider.tapir;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.ExtensionProperty;

public class In implements BooleanOperator {
	protected Log log = LogFactory.getLog(this.getClass());
	private List<String> literals = new LinkedList<String>();
	private ExtensionProperty property;
	
	public ExtensionProperty getProperty() {
		return property;
	}
	public void setProperty(ExtensionProperty property) {
		log.debug("Setting property to: " + property.getQualName());
		property.setName(property.getQualName());
		this.property = property;
	}
	public void setProperty(String propertyAsString) {
		setProperty(new ExtensionProperty(propertyAsString));
	}

	/**
	 * Another hack for how we use digester
	 */
	public void setValue(String value) {
		literals.add("'" + value + "'");
	}	
	
	public String toString() {
		return String.format("%s %s (%s)", getProperty().getName(), getOperatorSymbol(), StringUtils.join(literals, ","));
	}

	public In() {
		log.debug("Creating " + this.getClass().getSimpleName());
	}
	
	protected String getOperatorSymbol() {
		return "in";
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
