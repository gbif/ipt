package org.gbif.logging;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Thread t1 = new Thread(new T1());
		Thread t2 = new Thread(new T2());
		t1.start();
		t2.start();
	}

	static class T1 implements Runnable {
		static Log log = LogFactory.getLog(T1.class);
		public void run() {
			MDC.put("TYPE", "EVEN");
			for (int i=0; i<100; i+=2) {
				log.warn(i);
			}
		}
	}
	static class T2 implements Runnable {
		static Log log = LogFactory.getLog(T2.class);
		public void run() {
			MDC.put("TYPE", "ODD");
			for (int i=1; i<100; i+=2) {
				log.warn(i);
			}
		}
	}
	
	@Test
	public void testMain(){
		// nothing to do, its all about the static setup method
	}
}
