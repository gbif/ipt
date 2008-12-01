package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import java.io.File;

import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.lowagie.text.ImgTemplate;

public class ImageCacheManagerImplTest extends ContextAwareTestBase{
	@Autowired
	private AppConfig cfg;
	@Autowired
	private ImageCacheManager imageCacheManager;
	
	@Test
	public void testCacheImage() {
		String source = "http://www.gbif.org/images/gbif_05.jpg";
		
		File res = imageCacheManager.getCachedImage(4l, ImageType.ChartByRegion, 43, null, 312, 12220);
		assertFalse(res.exists());

		res = imageCacheManager.getCachedImage(4l, ImageType.ChartByBasisOfRecord, null, null, 300, 200);
		if(res.exists()){
			// remove previous test file
			res.delete();
		}
		String cacheLoc = imageCacheManager.cacheImage(4l, ImageType.ChartByBasisOfRecord, null, null, 300, 200, source);
		File cachedImage = cfg.getWebappFile(cacheLoc);
		assertTrue(res.exists());
	}

}
