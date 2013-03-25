package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.gbif.provider.service.SourceInspectionManager;
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
	private SourceInspectionManager datasourceInspectionManager;

	@Test
	public void testGetHeader() throws Exception {
		setup();
		List<String> headers = datasourceInspectionManager.getHeader(resource.getCoreMapping().getSource());
		System.out.println(headers);
	}

}
