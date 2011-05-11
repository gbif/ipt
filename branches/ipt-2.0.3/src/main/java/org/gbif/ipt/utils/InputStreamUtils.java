package org.gbif.ipt.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class InputStreamUtils {
  protected static final Logger log = Logger.getLogger(InputStreamUtils.class);

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

  /**
   * Converts an entire InputStream to a single String with UTF8 as the character encoding.
   * 
   * @param source source input stream to convert
   * @return the string representing the entire input stream
   */
  public String readEntireStream(InputStream source) {
    return readEntireStream(source, FileUtils.UTF8);
  }

  /**
   * Converts an entire InputStream to a single String with explicitly provided character encoding. To convert the
   * InputStream to String we use the BufferedReader.readLine() method. We iterate until the BufferedReader return null
   * which means there's no more data to read. Each line will appended to a StringBuilder and returned as String.
   * 
   * @param source source input stream to convert
   * @param encoding the streams character encoding
   * @return the string representing the entire input stream
   */
  public String readEntireStream(InputStream source, String encoding) {
    BufferedReader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(source, encoding));
    } catch (UnsupportedEncodingException e1) {
      throw new IllegalArgumentException("Unsupported encoding" + encoding, e1);
    }
    StringBuilder sb = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        source.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }
}
