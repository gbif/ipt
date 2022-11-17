/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.PublicationStatus;

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Simplified IPT resource view for main and manage pages.
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 */
public class SimplifiedResource {

  private String logoUrl;
  private String title;
  private String organisationName;
  private String coreType;
  private String subtype;
  private int recordsPublished;
  private Date modified;
  private Date lastPublished;
  private Date nextPublished;
  private PublicationStatus status;
  private String creatorName;
  private String shortname;
  private String subject;

  private String organisationAlias;
  private boolean published;

  public String getShortname() {
    return shortname;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitleOrShortname() {
    return title != null ? title : shortname;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public void setStatus(PublicationStatus status) {
    this.status = status;
  }

  public int getRecordsPublished() {
    return recordsPublished;
  }

  public void setRecordsPublished(int recordsPublished) {
    this.recordsPublished = recordsPublished;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getOrganisationAlias() {
    return organisationAlias;
  }

  public void setOrganisationAlias(String organisationAlias) {
    this.organisationAlias = organisationAlias;
  }

  public String getOrganisationName() {
    return organisationName;
  }

  public void setOrganisationName(String organisationName) {
    this.organisationName = organisationName;
  }

  public String getOrganizationAliasOrName() {
    return organisationAlias != null ? organisationAlias : organisationName;
  }

  public String getCoreType() {
    return coreType;
  }

  public void setCoreType(String coreType) {
    this.coreType = coreType;
  }

  public String getSubtype() {
    return subtype;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public boolean isPublished() {
    return published;
  }

  public void setPublished(boolean published) {
    this.published = published;
  }

  public Date getLastPublished() {
    return lastPublished;
  }

  public void setLastPublished(Date lastPublished) {
    this.lastPublished = lastPublished;
  }

  public Date getNextPublished() {
    return nextPublished;
  }

  public void setNextPublished(Date nextPublished) {
    this.nextPublished = nextPublished;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimplifiedResource that = (SimplifiedResource) o;
    return published == that.published
        && Objects.equals(shortname, that.shortname)
        && Objects.equals(title, that.title)
        && status == that.status
        && recordsPublished == that.recordsPublished
        && Objects.equals(logoUrl, that.logoUrl)
        && Objects.equals(subject, that.subject)
        && Objects.equals(organisationAlias, that.organisationAlias)
        && Objects.equals(organisationName, that.organisationName)
        && Objects.equals(coreType, that.coreType)
        && Objects.equals(subtype, that.subtype)
        && Objects.equals(modified, that.modified)
        && Objects.equals(lastPublished, that.lastPublished)
        && Objects.equals(nextPublished, that.nextPublished)
        && Objects.equals(creatorName, that.creatorName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shortname, title, status, recordsPublished, logoUrl, subject, organisationAlias, organisationName, coreType, subtype, modified, published, lastPublished, nextPublished, creatorName);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SimplifiedResource.class.getSimpleName() + "[", "]")
        .add("shortname='" + shortname + "'")
        .add("title='" + title + "'")
        .add("status=" + status)
        .add("recordsPublished=" + recordsPublished)
        .add("logoUrl='" + logoUrl + "'")
        .add("subject='" + subject + "'")
        .add("organisationAlias='" + organisationAlias + "'")
        .add("organisationName='" + organisationName + "'")
        .add("coreType='" + coreType + "'")
        .add("subtype='" + subtype + "'")
        .add("modified=" + modified)
        .add("published=" + published)
        .add("lastPublished=" + lastPublished)
        .add("nextPublished=" + nextPublished)
        .add("creatorName='" + creatorName + "'")
        .toString();
  }
}
