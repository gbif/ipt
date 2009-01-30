package org.gbif.provider.service;

import java.io.File;
import java.net.URL;

import org.gbif.provider.model.voc.ImageType;
import org.springframework.core.io.Resource;

public interface ImageCacheManager {
	public File getCachedImage(Long resourceId, ImageType imgType, Integer subtype, String area, int width, int height);
	public URL getCachedImageURL(Long resourceId, ImageType imgType, Integer subtype, String area, int width, int height);
	/**
	 * Cache a URL based image in the webapp/cache folder so its accessible as a static resource.
	 * The cached image is identified by a number of parameters that define its cache-filename
	 * @param resourceId
	 * @param imgType
	 * @param subType optional, can be null
	 * @param area of map. optional, can be null
	 * @param width
	 * @param height
	 * @param originalUrl URL to image that should be cached
	 * @return filename of cached file within the webapps-cache-resource folder which can be found using AppConfig.getResourceCacheFile()
	 */
	public String cacheImage(Long resourceId, ImageType imgType, Integer subtype, String area, int width, int height, String originalUrl);
}
