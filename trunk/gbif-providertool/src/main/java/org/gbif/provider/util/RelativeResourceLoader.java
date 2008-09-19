package org.gbif.provider.util;

import java.io.File;

import org.springframework.core.io.ContextResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

public class RelativeResourceLoader extends FileSystemResourceLoader {
	private String baseDir;

	private RelativeResourceLoader(String webappDir) {
		super();
		File f = new File(webappDir);
		if (!f.exists()){
			throw new IllegalArgumentException("Relative basedir must exist");
		}
		this.baseDir=f.getAbsolutePath() + "/";
	}

	/* Load resource as file relative to base dir
	 * (non-Javadoc)
	 * @see org.springframework.core.io.FileSystemResourceLoader#getResourceByPath(java.lang.String)
	 */
	@Override
	protected Resource getResourceByPath(String path) {
		if (path != null && path.startsWith("/")) {
			path = path.substring(1);
		}
		return new FileSystemResource(baseDir + path);
	}

	@Override
	public Resource getResource(String location) {
		return getResourceByPath(location);
	}
	
}
