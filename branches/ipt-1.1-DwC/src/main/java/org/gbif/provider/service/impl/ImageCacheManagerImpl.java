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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * TODO: Documentation.
 * 
 */
public class ImageCacheManagerImpl implements ImageCacheManager {
  protected final Log log = LogFactory.getLog(getClass());
  @Autowired
  private AppConfig cfg;

  public String cacheImage(Long resourceId, ImageType imgType, Integer subType,
      String area, int width, int height, String originalUrl) {
    String cacheFilename = getCacheFilename(imgType, subType, area, width,
        height);
    File cachedFile = cfg.getResourceCacheFile(resourceId, cacheFilename);
    if (!cachedFile.exists()) {
      // download file
      InputStream in = null;
      BufferedOutputStream out = null;
      try {
        // out
        File dir = cachedFile.getParentFile();
        dir.mkdirs();
        cachedFile.createNewFile();
        out = new BufferedOutputStream(new FileOutputStream(cachedFile));
        // in
        URL url = new URL(originalUrl);
        in = url.openStream();
        // download file
        byte[] buffer = new byte[1024];
        int numRead;
        long numWritten = 0;
        while ((numRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, numRead);
          numWritten += numRead;
        }
        log.debug(String.format("Cached image %s at %s", originalUrl,
            cachedFile.getAbsolutePath()));
      } catch (IOException e) {
        log.warn(String.format("Couldn't cache image %s", originalUrl), e);
      } finally {
        try {
          if (in != null) {
            in.close();
          }
          if (out != null) {
            out.close();
          }
        } catch (IOException ioe) {
        }
      }
    }
    return cacheFilename;
  }

  public File getCachedImage(Long resourceId, ImageType imgType,
      Integer subType, String area, int width, int height) {
    String cacheFilename = getCacheFilename(imgType, subType, area, width,
        height);
    return cfg.getResourceCacheFile(resourceId, cacheFilename);
  }

  public URL getCachedImageURL(Long resourceId, ImageType imgType,
      Integer subType, String area, int width, int height) {
    String cacheFilename = getCacheFilename(imgType, subType, area, width,
        height);
    return cfg.getResourceCacheUrl(resourceId, cacheFilename);
  }

  private String getCacheFilename(ImageType imgType, Integer subType,
      String area, int width, int height) {
    assert (imgType != null);
    String subTypeStr = subType == null ? "" : "-" + subType.toString();
    String areaStr = area == null ? "" : "-" + area.toString();
    return String.format("%s%s%s-%sx%s", imgType, subTypeStr, areaStr, width,
        height);
  }

}
