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
package org.gbif.provider.model.eml;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * This class can be used to encapsulate information about physical data.
 * 
 * Note that this class is immutable. Instances can be created using the builder
 * pattern via the builder method. The charset and name properties are required.
 * 
 */
public class PhysicalData implements Serializable {

  /**
   * This class can be used to build a PhysicalData instance using the builder
   * pattern. Instances of this class are created using PhysicalData's builder
   * method.
   * 
   * Example usage:
   * 
   * <pre>
   * PhysicalData pd = PhysicalData.builder(Charsets.UTF-8, "My Data").build();
   * 
   * or
   * 
   * Builder b = PhysicalData.builder(Charsets.UTF-8, "My Data");
   * PhysicalData pd = b.distributionUrl("http://foo.com").format("disk").formatVersion("2.0").build();
   * </pre>
   * 
   */
  public static class Builder {
    private final Charset charset;
    private String distributionUrl;
    private String format;
    private String formatVersion;
    private final String name;

    private Builder(Charset charset, String name) {
      this.charset = charset;
      this.name = name;
    }

    /**
     * Builds and returns a PhysicalData instance.
     * 
     * @return instance of PhysicalData
     */
    public PhysicalData build() {
      return new PhysicalData(this);
    }

    /**
     * Sets the builder's distributionUrl.
     * 
     * @param distributionUrl the distribution URL
     * @return this builder
     */
    public Builder distributionUrl(String distributionUrl) {
      this.distributionUrl = distributionUrl;
      return this;
    }

    /**
     * Sets the builder's format.
     * 
     * @param format the format
     * @return this builder
     */
    public Builder format(String format) {
      this.format = format;
      return this;
    }

    /**
     * Sets the builder's format version.
     * 
     * @param formatVersion the format version
     * @return this builder
     */
    public Builder formatVersion(String formatVersion) {
      this.formatVersion = formatVersion;
      return this;
    }
  }

  private static final long serialVersionUID = 1209461796079665955L;

  /**
   * Returns an instance of Builder initialized with a charset and name.
   * 
   * @param charset the charset
   * @param name the name
   * @return an instance of Builder
   */
  public static Builder builder(Charset charset, String name) {
    checkNotNull(charset, "Charset was null");
    checkNotNull(name, "Name was null");
    checkArgument(!name.isEmpty(), "Name was empty");
    return new Builder(charset, name);
  }

  private final Charset charset;
  private final String distributionUrl;
  private final String format;
  private final String formatVersion;
  private final String name;

  private PhysicalData(Builder builder) {
    charset = builder.charset;
    distributionUrl = builder.distributionUrl;
    format = builder.format;
    formatVersion = builder.formatVersion;
    name = builder.name;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PhysicalData)) {
      return false;
    }
    PhysicalData o = (PhysicalData) other;
    return equal(charset, o.charset)
        && equal(distributionUrl, o.distributionUrl) && equal(format, o.format)
        && equal(formatVersion, o.formatVersion) && equal(name, o.name);
  }

  public String getCharset() {
    return charset.displayName();
  }

  public String getDistributionUrl() {
    return distributionUrl;
  }

  public String getFormat() {
    return format;
  }

  public String getFormatVersion() {
    return formatVersion;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(charset, distributionUrl, format, formatVersion,
        name);
  }

  @Override
  public String toString() {
    return String.format(
        "Charset=%s, DistributionUrl=%s, Format=%s, FormatVersion=%s, Name=%s",
        charset, distributionUrl, format, formatVersion, name);
  }
}
