package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import org.appfuse.service.BaseManagerTestCase;
import org.gbif.provider.service.UploadEventManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UploadEventManagerImplTest extends BaseManagerTestCase{
	@Autowired
	private UploadEventManager uploadEventManager;
	
	@Test
	public void testGetGoogleChartData() {
		System.out.println(uploadEventManager.getGoogleChartData(1L, 450, 200));
		System.out.println(uploadEventManager.getGoogleChartData(321L, 450, 200));
	}

}
