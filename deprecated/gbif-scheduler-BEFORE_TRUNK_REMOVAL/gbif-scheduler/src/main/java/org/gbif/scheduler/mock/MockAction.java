package org.gbif.scheduler.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MockAction {
	Log log = LogFactory.getLog(this.getClass());
	public void execute(String p1, String p2, Object p3, String p4) {
		log.debug("************");
		log.debug("************");
		log.debug("Executed with p1[" + p1 + "] p2[" + p2 + "] p3[" + p3 + "] p4[" + p4 + "]");
		log.debug("************");
		log.debug("************");
	}
	public void execute() {
		log.debug("************");
		log.debug("************");
		log.debug("Execute with no params");
		log.debug("************");
		log.debug("************");	
	}
}
