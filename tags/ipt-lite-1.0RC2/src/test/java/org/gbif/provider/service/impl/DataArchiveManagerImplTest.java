package org.gbif.provider.service.impl;


import java.io.IOException;

import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DataArchiveManagerImplTest extends ResourceTestBase{
	@Autowired
	private DataArchiveManager dataArchiveManager;
	
	@Test
	public void testArchiving(){
		this.setupOccResource();
		try {
			dataArchiveManager.packageArchive(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
