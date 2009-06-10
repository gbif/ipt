package org.gbif.scheduler.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MockInstance {
	Log log = LogFactory.getLog(this.getClass());
	String aaa = "tim";
	String bbb = "tom";
	String ddd = "tam";
	MockAction target = new MockAction();
	public MockAction getTarget() {
		log.debug("Using instance");
		return target;
	}
	public void setTarget(MockAction target) {
		this.target = target;
	}
	public String getAaa() {
		return aaa;
	}
	public String getBbb() {
		return bbb;
	}
	public String getDdd() {
		return ddd;
	}

}
