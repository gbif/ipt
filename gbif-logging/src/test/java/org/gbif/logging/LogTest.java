package org.gbif.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;

/**
 * A test class to check how the MDC works with statically defined loggers
 * @author timrobertson
 */
public class LogTest {
	public static void main(String[] args) {
		Thread t1 = new Thread(new T1());
		Thread t2 = new Thread(new T2());
		t1.start();
		t2.start();
	}
	
	static class T1 implements Runnable {
		static Log log = LogFactory.getLog(T1.class);
		public void run() {
			MDC.put("TYPE", "EVEN");
			for (int i=0; i<1000; i+=2) {
				log.warn(i);
			}
		}
	}
	static class T2 implements Runnable {
		static Log log = LogFactory.getLog(T2.class);
		public void run() {
			MDC.put("TYPE", "ODD");
			for (int i=1; i<1000; i+=2) {
				log.warn(i);
			}
		}
	}
}
