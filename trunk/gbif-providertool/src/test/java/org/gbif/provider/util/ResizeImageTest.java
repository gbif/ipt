package org.gbif.provider.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ResizeImageTest extends ContextAwareTestBase{

	@Test
	public void testResize() throws Exception {
		File in = new File("/Users/markus/Desktop/ubuntu-logo.jpg");
		File out = new File("/Users/markus/Desktop/ubuntu.png");
		try {
			ResizeImage.resizeImage(in, out, 68, 68);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
