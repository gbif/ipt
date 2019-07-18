package org.gbif.ipt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InputStreamUtils {

  protected static final Logger log = LogManager.getLogger(InputStreamUtils.class);

  public InputStream classpathStream(String path) {
    InputStream in = null;
    // relative path. Use classpath instead
    URL url = getClass().getClassLoader().getResource(path);
    if (url != null) {
      try {
        in = url.openStream();
      } catch (IOException e) {
        log.warn(e);
      }
    }
    return in;
  }
}
