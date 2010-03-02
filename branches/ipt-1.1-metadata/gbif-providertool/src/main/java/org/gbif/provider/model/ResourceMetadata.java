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
package org.gbif.provider.model;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * A generic resource describing any digital, online and non digital available
 * biological resources Should be replaced by a proper GBRDS model class.
 * Lacking most properties and internationalized abilities.
 * 
 */
@Embeddable
public class ResourceMetadata {
	protected String uddiID;
	protected String link;
	
	/**
	 * A description of the resource that is being documented that is long enough to 
	 * differentiate it from other similar resource
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#title
	 */
	protected String title;
	
	/**
	 * A brief overview of the resource that is being documented 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#abstract
	 */
	protected String description;
	
	protected String contactName;
	protected String contactEmail;
	protected Point location;

	@Column(length = 64)
	public String getContactEmail() {
		return contactEmail;
	}

	@Column(length = 128)
	public String getContactName() {
		return contactName;
	}

	@Lob
	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public Point getLocation() {
		return location;
	}

	@Column(length = 128)
	@org.hibernate.annotations.Index(name = "title")
	public String getTitle() {
		return title;
	}

	@Column(length = 64, unique = true)
	public String getUddiID() {
		return uddiID;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = StringUtils.trimToNull(contactEmail);
	}

	public void setContactName(String contactName) {
		this.contactName = StringUtils.trimToNull(contactName);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLink(String link) {
		this.link = StringUtils.trimToNull(link);
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public void setTitle(String title) {
		this.title = StringUtils.trimToNull(title);
	}

	public void setUddiID(String uddiID) {
		this.uddiID = StringUtils.trimToNull(uddiID);
	}

}
