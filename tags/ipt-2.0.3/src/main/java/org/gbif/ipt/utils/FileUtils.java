package org.gbif.ipt.utils;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class FileUtils {
  public static final Pattern TAB_DELIMITED = Pattern.compile("\t");
  public static final String UTF8 = "UTF8";
  protected static Logger log = Logger.getLogger(FileUtils.class);

  public static Set<String> columnsToSet(InputStream source, int... column) throws IOException {
    return columnsToSet(source, new HashSet<String>(), column);
  }

  /**
   * Reads a file and returns a unique set of multiple columns from lines which are no comments (starting with #) and
   * trims whitespace
   * 
   * @param source the UTF-8 encoded text file with tab delimited columns
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @param column variable length argument of column indices to process
   * @return set of column rows
   * @throws IOException
   */
  public static Set<String> columnsToSet(InputStream source, Set<String> resultSet, int... column) throws IOException {
    LineIterator lines = getLineIterator(source);
    int maxCols = 0;
    for (int c : column) {
      if (c > maxCols) {
        maxCols = c;
      }
    }
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        String[] parts = TAB_DELIMITED.split(line);
        if (maxCols <= parts.length) {
          for (int c : column) {
            String cell = parts[c].trim();
            resultSet.add(cell);
          }
        }
      }
    }
    return resultSet;
  }

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

  /**
   * escapes a filename so it is a valid filename on all systems, replacing /. .. \t\r\n
   * 
   * @param filename to be escaped
   * @return
   */
  public static String escapeFilename(String filename) {
    return filename.replaceAll("[\\s./&]", "_");
  }

  public static String formatSize(long longSize, int decimalPos) {
    NumberFormat fmt = NumberFormat.getNumberInstance();
    if (decimalPos >= 0) {
      fmt.setMaximumFractionDigits(decimalPos);
    }
    final double size = longSize;
    double val = size / (1024 * 1024);
    if (val > 1) {
      return fmt.format(val).concat(" MB");
    }
    val = size / 1024;
    if (val > 1) {
      return fmt.format(val).concat(" KB");
    }
    return longSize + " bytes";
  }

  public static InputStream getInputStream(File source) throws FileNotFoundException {
    return new FileInputStream(source);
  }

  /**
   * @param source the source input stream encoded in UTF8
   * @return
   */
  public static LineIterator getLineIterator(InputStream source) {
    return getLineIterator(source, UTF8);
  }

  /**
   * @param source the source input stream
   * @param encoding the encoding used by the input stream
   * @return
   * @throws UnsupportedEncodingException
   */
  public static LineIterator getLineIterator(InputStream source, String encoding) {
    try {
      return new LineIterator(new BufferedReader(new InputStreamReader(source, encoding)));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding" + encoding, e);
    }
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

  private static boolean ignore(String line) {
    if (StringUtils.trimToNull(line) == null || line.startsWith("#")) {
      return true;
    }
    return false;
  }

  public static boolean isCompressedFile(File source) {
    String suffix = source.getName().substring(source.getName().lastIndexOf(".") + 1);
    if (suffix != null && suffix.length() > 0) {
      if (suffix.equalsIgnoreCase("zip")) {
        // try zip
        return true;
      } else if (suffix.equalsIgnoreCase("tgz") || suffix.equalsIgnoreCase("gz")) {
        // try gzip
        return true;
      }
    }
    return false;
  }

  public static Writer startNewUtf8File(File file) throws IOException {
    Writer writer = null;
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
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), UTF8));
    return writer;
  }

  public static Writer startNewUtf8XmlFile(File file) throws IOException {
    Writer writer = startNewUtf8File(file);
    writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
    return writer;
  }

  public static List<String> streamToList(InputStream source) throws IOException {
    return streamToList(source, new ArrayList<String>());
  }

  /**
   * Reads a file and returns a list of all lines which are no comments (starting with #) and trims whitespace
   * 
   * @param source the UTF-8 encoded text file to read
   * @param resultList the list implementation to be used. Will not be cleared before reading!
   * @return list of lines
   * @throws IOException
   */
  public static List<String> streamToList(InputStream source, List<String> resultList) throws IOException {
    LineIterator lines = getLineIterator(source);
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        resultList.add(line);
      }
    }
    return resultList;
  }

  public static Map<String, String> streamToMap(InputStream source) throws IOException {
    return streamToMap(source, new HashMap<String, String>());
  }

  public static Map<String, String> streamToMap(InputStream source, int key, int value, boolean trimToNull)
      throws IOException {
    return streamToMap(source, new HashMap<String, String>(), key, value, trimToNull);
  }

  /**
   * Read a hashmap from a tab delimited file using the row number as an integer value, ignoring commented rows starting
   * with #
   * 
   * @param source tab delimited text file to read
   * @param key column number to use as key
   * @param value column number to use as value
   * @return
   * @throws IOException
   */
  public static Map<String, String> streamToMap(InputStream source, Map<String, String> result) throws IOException {
    LineIterator lines = getLineIterator(source);
    Integer row = 0;
    while (lines.hasNext()) {
      row++;
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        result.put(line, row.toString());
      }
    }
    return result;
  }

  /**
   * Read a hashmap from a tab delimited file, ignoring commented rows starting with #
   * 
   * @param source tab delimited text file to read
   * @param key column number to use as key
   * @param value column number to use as value
   * @param trimToNull if true trims map entries to null
   * @return
   * @throws IOException
   */
  public static Map<String, String> streamToMap(InputStream source, Map<String, String> result, int key, int value,
      boolean trimToNull) throws IOException {
    LineIterator lines = getLineIterator(source);
    int maxCols = key > value ? key : value + 1;
    while (lines.hasNext()) {
      String line = lines.nextLine();
      // ignore comments
      if (!ignore(line)) {
        String[] parts = TAB_DELIMITED.split(line);
        if (maxCols <= parts.length) {
          if (trimToNull) {
            result.put(StringUtils.trimToNull(parts[key]), StringUtils.trimToNull(parts[value]));
          } else {
            result.put(parts[key], parts[value]);
          }
        }
      }
    }
    return result;
  }

  public static Set<String> streamToSet(InputStream source) throws IOException {
    return streamToSet(source, new HashSet<String>());
  }

  /**
   * Reads a file and returns a unique set of all lines which are no comments (starting with #) and trims whitespace
   * 
   * @param source the UTF-8 encoded text file to read
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @return set of unique lines
   * @throws IOException
   */
  public static Set<String> streamToSet(InputStream source, Set<String> resultSet) throws IOException {
    LineIterator lines = getLineIterator(source);
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        resultSet.add(line);
      }
    }
    return resultSet;
  }

  public static String toFilePath(URL url) {
    String protocol = url.getProtocol() == null || url.getProtocol().equalsIgnoreCase("http") ? "" : "/__"
        + url.getProtocol() + "__";
    String domain = url.getAuthority() == null ? "__domainless" : url.getAuthority();
    return domain + protocol + url.getFile();
  }

}
