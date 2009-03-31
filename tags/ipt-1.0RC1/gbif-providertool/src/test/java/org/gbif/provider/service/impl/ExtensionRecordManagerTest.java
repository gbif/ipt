package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
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
	@Autowired
	protected TaxonManager taxonManager;
	
	
	@Test
	public void testExtendedRecord(){
		this.setupTaxResource();
		List<Taxon> taxa = taxonManager.latest(Constants.TEST_CHECKLIST_RESOURCE_ID, 1, 10);
		assertTrue(taxa.size()==10);
		List<ExtendedRecord> records = extensionRecordManager.extendCoreRecords(resource, taxa.toArray(new CoreRecord[taxa.size()]));
		assertTrue(records.size()==10);
		List<Extension> extensions = records.get(6).getExtensions();
		assertTrue(records.get(6).getExtensionRecords(extensions.get(0)).size()>0);
	}

}
