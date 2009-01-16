package org.gbif.provider.tapir.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BooleanOperator {
	protected Log log = LogFactory.getLog(this.getClass());
	
	public abstract String toHQL();
}
