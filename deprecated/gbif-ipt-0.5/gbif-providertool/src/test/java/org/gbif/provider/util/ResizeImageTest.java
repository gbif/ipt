package org.gbif.provider.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class ResizeImageTest extends ContextAwareTestBase{

	@Test
	public void testResize() throws Exception {
		Resource res = this.applicationContext.getResource("classpath:test-image.jpg");
		File in = res.getFile();
		File out = new File(in.getParent(), "test-image2.jpg");
		try {
			ResizeImage.resizeImage(in, out, 68, 68);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
