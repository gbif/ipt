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

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * This class can be used to encapsulate information about physical data.
 */
public class PhysicalData implements Serializable {
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 1209461796079665955L;
	
	/**
	 * This element contains the name of the character encoding. This is typically ASCII or UTF-8, or one of the other common encodings. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-physical.html#characterEncoding
	 */
	private String charset;
	
	/**
	 * The URL of the resource that is available online. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-physical.html#url
	 */
	private String distributionUrl;
	
	/**
	 * Name of the format of the data object. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-physical.html#formatName
	 */
	private String format;
	
	/**
	 * Version of the format of the data object.
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-physical.html#formatVersion
	 */
	private String formatVersion;
	
	/**
	 * The name of the data object, usually a file in a file system or that is accessible on the network. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-physical.html#objectName
	 */
	private String name;
	
	/**
	 * Required by Struts2
	 */
	public PhysicalData() {
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
		return equal(charset, o.charset) && equal(distributionUrl, o.distributionUrl) && equal(format, o.format) && equal(formatVersion, o.formatVersion) && equal(name, o.name);
	}

	public String getCharset() {
		return charset;
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
	
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setDistributionUrl(String distributionUrl) {
		this.distributionUrl = distributionUrl;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(charset, distributionUrl, format, formatVersion, name);
	}

	@Override
	public String toString() {
		return String.format("Charset=%s, DistributionUrl=%s, Format=%s, FormatVersion=%s, Name=%s", charset, distributionUrl, format, formatVersion, name);
	}
}
