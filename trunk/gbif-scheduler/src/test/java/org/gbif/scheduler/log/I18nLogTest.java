package org.gbif.scheduler.log;

/**
 * A test class to check how the MDC works with statically defined loggers
 * @author timrobertson
 */
public class I18nLogTest {
	public static void main(String[] args) {
		System.out.println("Starting...");
		I18nLog log = I18nLogFactory.getLog(I18nLogTest.class);
		log.warn("key.test");
		log.warn("key.test", new Exception("Ouch"));
		log.warn("key.test.params", new String[]{"Insertae"});
		log.warn("key.test.params", new String[]{"Insertae"}, new Exception("My toe"));
		log.warn("key.test.params", new String[]{"Insertae", "two"});
	}
}