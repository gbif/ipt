/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * TODO: Documentation.
 * 
 */
public class ImageCacheManagerImplTest extends ContextAwareTestBase {
  @Autowired
  private AppConfig cfg;
  @Autowired
  private ImageCacheManager imageCacheManager;

  @Test
  public void testCacheImage() {
    String source = "http://www.gbif.org/images/gbif_05.jpg";

    File res = imageCacheManager.getCachedImage(4L, ImageType.ChartByRegion,
        43, null, 312, 12220);
    assertFalse(res.exists());

    res = imageCacheManager.getCachedImage(4L, ImageType.ChartByBasisOfRecord,
        null, null, 300, 200);
    if (res.exists()) {
      // remove previous test file
      res.delete();
    }
    String cacheLoc = imageCacheManager.cacheImage(4L,
        ImageType.ChartByBasisOfRecord, null, null, 300, 200, source);
    File cachedImage = cfg.getWebappFile(cacheLoc);
    assertTrue(res.exists());
  }

}
