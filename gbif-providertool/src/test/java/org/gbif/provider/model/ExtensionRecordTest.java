package org.gbif.provider.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ExtensionRecordTest {

	@Test
	public void testIterator() {
		ExtensionRecord extRec = new ExtensionRecord();
		extRec.setCoreId(5324l);		
		extRec.getProperties().put(new ExtensionProperty("alberto:the:great"), "hallo");
		extRec.getProperties().put(new ExtensionProperty("alberto:the:medium"), "servus");
		extRec.getProperties().put(new ExtensionProperty("alberto:the:small"), "bonjour");
		
		for (ExtensionProperty p : extRec){
//			System.out.println(p);
		}
	}

}
