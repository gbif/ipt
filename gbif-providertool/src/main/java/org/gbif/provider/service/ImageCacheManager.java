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
package org.gbif.provider.service;

import org.gbif.provider.model.voc.ImageType;

import java.io.File;
import java.net.URL;

/**
 * TODO: Documentation.
 * 
 */
public interface ImageCacheManager {

  /**
   * Cache a URL based image in the webapp/cache folder so its accessible as a
   * static resource. The cached image is identified by a number of parameters
   * that define its cache-filename
   * 
   * @param resourceId
   * @param imgType
   * @param subType optional, can be null
   * @param area of map. optional, can be null
   * @param width
   * @param height
   * @param originalUrl URL to image that should be cached
   * @return filename of cached file within the webapps-cache-resource folder
   *         which can be found using AppConfig.getResourceCacheFile()
   */
  String cacheImage(Long resourceId, ImageType imgType, Integer subtype,
      String area, int width, int height, String originalUrl);

  File getCachedImage(Long resourceId, ImageType imgType, Integer subtype,
      String area, int width, int height);

  URL getCachedImageURL(Long resourceId, ImageType imgType, Integer subtype,
      String area, int width, int height);
}
