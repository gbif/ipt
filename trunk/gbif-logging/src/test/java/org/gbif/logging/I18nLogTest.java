package org.gbif.logging;

import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test class to check how the MDC works with statically defined loggers
 * @author timrobertson
 */
public class I18nLogTest {
	@BeforeClass
	public static void main() {
		System.out.println("Starting...");
		I18nLog log = I18nLogFactory.getLog(I18nLogTest.class);
		log.warn("key.test");
		log.warn("key.test", new Exception("Ouch"));
		log.warn("key.test.params", new String[]{"Insertae"});
		log.warn("key.test.params", new String[]{"Insertae"}, new Exception("My toe"));
		log.warn("key.test.params", new String[]{"Insertae", "two"});
	}
	
	@Test
	public void testMain(){
		// nothing to do, its all about the static setup method
	}	
}