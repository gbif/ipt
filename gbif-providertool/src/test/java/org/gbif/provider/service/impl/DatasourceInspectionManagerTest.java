package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DatasourceInspectionManagerTest extends ResourceTestBase{
	@Autowired
	private AppConfig cfg;
	@Autowired
	private DatasourceInspectionManager datasourceInspectionManager;

	@Test
	public void testGetHeader() throws Exception {
		setup();
		List<String> headers = datasourceInspectionManager.getHeader(resource.getCoreMapping());
		System.out.println(headers);
	}

}
