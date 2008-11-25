package org.gbif.provider.geo;

import static org.junit.Assert.*;

import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransformationUtilsTest extends ContextAwareTestBase{
	@Autowired
	private TransformationUtils wgs84Util;

	@Test
	public void testTransformIntoWGS84() {
		fail("Not yet implemented");
	}

}
