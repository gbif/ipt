package org.gbif.scheduler.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import org.gbif.scheduler.mock.MockAction;
import org.gbif.scheduler.mock.MockInstance;
import org.gbif.scheduler.model.LaunchAction;

public class LaunchActionUtilsTest {
	Log log = LogFactory.getLog(this.getClass());
	
	
	@Test
	public void testExecute() {
		try {
			// existing instance with parameters
			LaunchAction launchAction = new LaunchAction();
			launchAction.setInstanceParam("target");
			launchAction.setMethodName("execute");
			launchAction.setMethodParams("aaa,bbb,null,ddd");
			LaunchActionUtils.execute(new MockInstance(), launchAction);
			
			// existing instance without parameters
			launchAction.setMethodParams(null);
			LaunchActionUtils.execute(new MockInstance(), launchAction);
			
			// new instance with parameters
			launchAction.setInstanceParam(null);
			launchAction.setFullClassName(MockAction.class.getCanonicalName());
			launchAction.setMethodParams("aaa,bbb,null,ddd");
			LaunchActionUtils.execute(new MockInstance(), launchAction);
			
			// new instance without parameters
			launchAction.setMethodParams(null);
			LaunchActionUtils.execute(new MockInstance(), launchAction);			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCommaStringToStringArray() {
		String[] results = LaunchActionUtils.commaStringToStringArray("tim,tom,tam");
		assertTrue(results.length==3);
		assertEquals("tim", results[0]);
		assertEquals("tom", results[1]);
		assertEquals("tam", results[2]);
		
		results = LaunchActionUtils.commaStringToStringArray("tim,tom,null,tam");
		assertTrue(results.length==4);
		assertEquals("tim", results[0]);
		assertEquals("tom", results[1]);
		assertEquals("null", results[2]);
		assertEquals("tam", results[3]);		
	}

	@Test
	public void testPropertyValuesFromInstanceObjectStringArray() {
		LazyDynaBean ldb = new LazyDynaBean();
		ldb.set("aaa", "tim");
		ldb.set("bbb", "tom");
		ldb.set("ccc", "tam");
		String [] params = new String[]{"aaa", "bbb", "ccc"};
		try {
			Object[] results = LaunchActionUtils.propertyValuesFromInstance(ldb,params);
			assertTrue(results.length==3);
			assertEquals("tim", results[0]);
			assertEquals("tom", results[1]);
			assertEquals("tam", results[2]);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	

	@Test
	public void testPropertyValuesFromInstanceObjectString() {
		LazyDynaBean ldb = new LazyDynaBean();
		ldb.set("aaa", "tim");
		ldb.set("bbb", "tom");
		ldb.set("ccc", null);
		ldb.set("ddd", "tam");
		
		try {
			Object[] results = LaunchActionUtils.propertyValuesFromInstance(ldb,"aaa,bbb,ccc,ddd");
			assertTrue(results.length==4);
			assertEquals("tim", results[0]);
			assertEquals("tom", results[1]);
			//assertTrue(results[2] == null);
			assertEquals("tam", results[3]);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
