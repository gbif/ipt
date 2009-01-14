package org.gbif.provider.tapir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class LogicalOperator implements BooleanOperator {
	protected Log log = LogFactory.getLog(this.getClass());
	
	// needed due to the Digester based parsing
	public void addOperand(LogicalOperator operand) {
	}
}
