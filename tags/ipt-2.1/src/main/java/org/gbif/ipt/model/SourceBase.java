/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import java.io.Serializable;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Objects.equal;

/**
 * Shared features for any Source implementation.
 */
public abstract class SourceBase implements Comparable<Source>, Serializable, Source {

  private static final long serialVersionUID = 119920000112L;

  protected Resource resource;
  protected String name;
  protected String encoding = "UTF-8";
  protected String dateFormat = "YYYY-MM-DD";
  protected int columns;
  protected boolean readable = false;

  /**
   * This method normalises a file name by removing certain reserved characters and converting all file name characters
   * to lowercase.
   * The reserved characters are:
   * <ul>
   * <li>All whitespace characters</li>
   * <li>All slash character</li>
   * <li>All backslash character</li>
   * <li>All question mark character</li>
   * <li>All percent character</li>
   * <li>All asterik character</li>
   * <li>All colon character</li>
   * <li>All pipe character</li>
   * <li>All less than character</li>
   * <li>All greater than character</li>
   * <li>All quote character</li>
   * </ul>
   *
   * @param name to normalise, may be null
   *
   * @return normalised name
   */
  public static String normaliseName(@Nullable String name) {
    if (name == null) {
      return null;
    }
    return StringUtils.substringBeforeLast(name, ".").replaceAll("[\\s.:/\\\\*?%|><\"]+", "").toLowerCase();
  }

  public int compareTo(Source o) {
    if (this == o) {
      return 0;
    }
    if (this.name == null) {
      return -1;
    }
    return name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!SourceBase.class.isInstance(other)) {
      return false;
    }
    Source o = (Source) other;
    // return equal(resource, o.resource) && equal(name, o.name);
    return equal(name, o.getName());
  }

  public int getColumns() {
    return columns;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public String getEncoding() {
    return encoding;
  }

  public String getName() {
    return name;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public int hashCode() {
    // return Objects.hashCode(resource, name);
    return Objects.hashCode(name);
  }

  public boolean isFileSource() {
    return TextFileSource.class.isInstance(this);
  }

  public boolean isExcelSource() {
    return ExcelFileSource.class.isInstance(this);
  }

  public boolean isSqlSource() {
    return SqlSource.class.isInstance(this);
  }

  public boolean isReadable() {
    return readable;
  }

  public void setColumns(int columns) {
    this.columns = columns;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setName(String name) {
    this.name = normaliseName(name);
  }

  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + name + ";" + resource + "]";
  }

}
