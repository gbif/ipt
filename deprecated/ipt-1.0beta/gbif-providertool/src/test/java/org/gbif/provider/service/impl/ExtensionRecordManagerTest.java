package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.hibernate.PropertyValueException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AssertThrows;


public class ExtensionRecordManagerTest extends ResourceTestBase{
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	protected DarwinCoreManager darwinCoreManager;
	
	
	@Test
	public void testExtendedRecord(){
		this.setupOccResource();
		List<DarwinCore> dwcs = darwinCoreManager.latest(Constants.TEST_OCC_RESOURCE_ID, 1, 5);
		assertTrue(dwcs.size()==5);
		List<ExtendedRecord> records = extensionRecordManager.extendCoreRecords(resource, dwcs.toArray(new CoreRecord[dwcs.size()]));
		assertTrue(records.size()==5);
		List<Extension> extensions = records.get(0).getExtensions();
		assertTrue(records.get(0).getExtensionRecords(extensions.get(0)).size()>0);
	}

}
