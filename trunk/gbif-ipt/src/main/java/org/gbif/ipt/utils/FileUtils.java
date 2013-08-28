package org.gbif.ipt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

public class FileUtils {

  public static final String UTF8 = "UTF8";
  protected static Logger log = Logger.getLogger(FileUtils.class);

  public static void copyStreams(InputStream in, OutputStream out) throws IOException {
    // write the file to the file specified
    int bytesRead;
    byte[] buffer = new byte[8192];

    while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
      out.write(buffer, 0, bytesRead);
    }

    out.close();
    in.close();
  }

  public static void copyStreamToFile(InputStream in, File out) throws IOException {
    copyStreams(in, new FileOutputStream(out));
  }

  public static String formatSize(long longSize, int decimalPos) {
    NumberFormat fmt = NumberFormat.getNumberInstance();
    if (decimalPos >= 0) {
      fmt.setMaximumFractionDigits(decimalPos);
    }
    double val = longSize / (1024 * 1024);
    if (val > 1) {
      return fmt.format(val) + " MB";
    }
    val = longSize / 1024;
    if (val > 1) {
      return fmt.format(val) + " KB";
    }
    return longSize + " bytes";
  }

  public static Reader getUtf8Reader(File file) throws FileNotFoundException {
    Reader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return reader;
  }

  public static Writer startNewUtf8File(File file) throws IOException {
    try {
      org.apache.commons.io.FileUtils.touch(file);
    } catch (IOException e) {
      // io error can happen on windows if last modification cannot be set
      // see http://commons.apache.org/io/api-1.4/org/apache/commons/io/FileUtils.html#touch(java.io.File)
      // we catch this and check if the file was created
      if (file.exists() && file.canWrite()) {
        log.warn("Cant touch file, but it was created: " + e.getMessage());
        log.debug(e);
      } else {
        throw e;
      }
    }
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), UTF8));
  }
}
