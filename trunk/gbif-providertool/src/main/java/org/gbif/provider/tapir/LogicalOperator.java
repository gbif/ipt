package org.gbif.provider.tapir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class LogicalOperator extends BooleanOperator {
	
	// needed due to the Digester based parsing
	public abstract void addOperand(LogicalOperator operand);
	
}
