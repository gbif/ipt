package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.io.IOException;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EmlManagerTest extends ResourceTestBase{
	@Autowired
	private EmlManager emlManager;

	@Test
	public void testSaveResource() {
		resource=this.getResourceMock();
		Eml eml = new Eml();
		eml.setResource(resource);
		eml.addKeyword("Italia");
		eml.addKeyword("Romans");
		eml.addKeyword("River");
		eml.addKeyword("Climate change");
		eml.addKeyword("Mötörhead");
		BBox bbox = new BBox(-3.0,-123.0,  12.0,32.0);
		eml.geographicCoverage().setBoundingCoordinates(bbox);
		eml.geographicCoverage().setDescription("ick weiss auch nicht welche Ecke der Welt däs sein soll...");
		
		emlManager.save(eml);
	}
	
	@Test
	public void testLoadResource() {
		resource=this.getResourceMock();

		Eml eml = emlManager.load(resource);
		eml.addKeyword("Italia");
		eml.addKeyword("Romans");
		eml.addKeyword("River");
		eml.addKeyword("Climate change");
		BBox bbox = new BBox(-3.0,-123.0,  12.0,32.0);
		eml.geographicCoverage().setBoundingCoordinates(bbox);
		eml.geographicCoverage().setDescription("ick weiss auch nicht welche Ecke der Welt das sein soll...");
		
		emlManager.save(eml);
	}

	@Test
	public void testPublishResource() {
		setupOccResource();
		try {
			emlManager.publishNewEmlVersion(resource);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
