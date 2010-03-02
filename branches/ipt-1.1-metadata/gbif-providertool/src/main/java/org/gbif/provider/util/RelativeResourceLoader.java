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
package org.gbif.provider.util;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * TODO: Documentation.
 * 
 */
public class RelativeResourceLoader extends FileSystemResourceLoader {
  private final String baseDir;

  private RelativeResourceLoader(String webappDir) {
    super();
    File f = new File(webappDir);
    if (!f.exists()) {
      throw new IllegalArgumentException("Relative basedir must exist");
    }
    this.baseDir = f.getAbsolutePath() + "/";
  }

  @Override
  public Resource getResource(String location) {
    return getResourceByPath(location);
  }

  /*
   * Load resource as file relative to base dir (non-Javadoc)
   * 
   * @see
   * org.springframework.core.io.FileSystemResourceLoader#getResourceByPath(
   * java.lang.String)
   */
  @Override
  protected Resource getResourceByPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    return new FileSystemResource(baseDir + path);
  }

}
