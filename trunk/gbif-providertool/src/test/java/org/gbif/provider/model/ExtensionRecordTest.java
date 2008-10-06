package org.gbif.provider.model;

import static org.junit.Assert.*;

import org.gbif.provider.model.dto.ExtensionRecord;
import org.junit.Before;
import org.junit.Test;

public class ExtensionRecordTest {

	@Test
	public void testIterator() {
		ExtensionRecord extRec = new ExtensionRecord(5324l, 1l);
		extRec.setPropertyValue(new ExtensionProperty("alberto:the:great"), "hallo");
		extRec.setPropertyValue(new ExtensionProperty("alberto:the:medium"), "servus");
		extRec.setPropertyValue(new ExtensionProperty("alberto:the:small"), "bonjour");
		
		for (ExtensionProperty p : extRec){
//			System.out.println(p);
		}
	}

}
