/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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

import org.hibernate.validator.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * This abstract class can be used as an entity to encapsulate information about
 * data sources, including {@link SourceFile} and {@link SourceSql}.
 * 
 */
@Entity
public abstract class SourceBase implements BaseObject, ResourceRelatedObject {
  private Long id;
  @NotNull
  protected DataResource resource;
  @NotNull
  protected String name;
  protected String csvFileHeader;
  protected String separator;
  protected Integer numLinesToSkip;
  protected Boolean archiveFile = false;
  protected String encoding;

  public boolean getArchiveFile() {
    return archiveFile;
  }

  @Lob
  public String getCsvFileHeader() {
    return csvFileHeader;
  }

  @Lob
  public String getEncoding() {
    return encoding;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 128)
  public String getName() {
    return name;
  }

  public int getNumLinesToSkip() {
    return numLinesToSkip;
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "resource_fk")
  public DataResource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  public String getSeparator() {
    return separator;
  }

  @Transient
  public abstract boolean isValid();

  public void setArchiveFile(boolean isArchiveFile) {
    this.archiveFile = isArchiveFile;
  }

  public void setCsvFileHeader(String firstLineFile) {
    this.csvFileHeader = firstLineFile;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNumLinesToSkip(int numLinesToSkip) {
    this.numLinesToSkip = numLinesToSkip;
  }

  public void setResource(DataResource resource) {
    this.resource = resource;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }
}
