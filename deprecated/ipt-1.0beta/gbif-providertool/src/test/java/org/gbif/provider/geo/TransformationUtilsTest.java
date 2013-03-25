package org.gbif.provider.geo;

import static org.junit.Assert.*;

import org.gbif.provider.geo.TransformationUtils.Wgs84Transformer;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;

public class TransformationUtilsTest {//extends ContextAwareTestBase{
	// @Autowired
	private TransformationUtils wgs84Util = new TransformationUtils();

	@Test
	public void testTransformIntoWGS84() {
		try {
			Wgs84Transformer transformer = wgs84Util.getWgs84Transformer("WGS84");
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
