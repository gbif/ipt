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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtils {

  public static final String UTF8 = "UTF8";

  private static final Logger LOG = LogManager.getLogger(FileUtils.class);

  private static final int BUFFER_SIZE = 8192;
  private static final int TEMP_DIR_ATTEMPTS = 10000;

  private FileUtils() {
    // private constructor.
  }

  public static void copyStreams(InputStream in, OutputStream out) throws IOException {
    // write the file to the file specified
    int bytesRead;
    byte[] buffer = new byte[BUFFER_SIZE];

    while ((bytesRead = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
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
    double val = longSize / (1000f * 1000f);
    if (val > 1) {
      return fmt.format(val) + " MB";
    }
    val = longSize / 1000f;
    if (val > 1) {
      return fmt.format(val) + " kB";
    }
    return longSize + " bytes";
  }

  public static Reader getUtf8Reader(File file) throws FileNotFoundException {
    Reader reader;
    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
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
        LOG.warn("Cant touch file, but it was created: " + e.getMessage());
        LOG.debug(e);
      } else {
        throw e;
      }
    }
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
  }

  /**
   * Construct filename for persisted file (e.g. vocabulary or extension) replacing certain characters with an
   * underscore, and appending a suffix.
   *
   * @param name   original name, excluding suffix
   * @param suffix suffix to add to filename
   *
   * @return constructed filename
   */
  public static String getSuffixedFileName(String name, String suffix) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(suffix);
    return name.replaceAll("[/.:]+", "_") + suffix;
  }

  /**
   * Atomically creates a new directory somewhere beneath the system's temporary directory (as
   * defined by the {@code java.io.tmpdir} system property), and returns its name.
   *
   * <p>Use this method instead of {@link File#createTempFile(String, String)} when you wish to
   * create a directory, not a regular file. A common pitfall is to call {@code createTempFile},
   * delete the file and create a directory in its place, but this leads a race condition which can
   * be exploited to create security vulnerabilities, especially when executable files are to be
   * written into the directory.
   *
   * <p>This method assumes that the temporary volume is writable, has free inodes and free blocks,
   * and that it will not be called thousands of times per second.
   *
   * Copied from guava.
   *
   * @return the newly-created directory
   * @throws IllegalStateException if the directory could not be created
   */
  public static File createTempDir() {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = System.currentTimeMillis() + "-";

    for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
      File tempDir = new File(baseDir, baseName + counter);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    throw new IllegalStateException(
        "Failed to create directory within "
            + TEMP_DIR_ATTEMPTS
            + " attempts (tried "
            + baseName
            + "0 to "
            + baseName
            + (TEMP_DIR_ATTEMPTS - 1)
            + ')');
  }
}
