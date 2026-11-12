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
import java.util.UUID;

import lombok.Data;

/**
 * Simplified IPT resource view for main and manage pages.
 * A resource can be identified by its shortname, which has to be unique within an IPT instance.
 */
@Data
public class ResourceSummaryView {

  private String logoUrl;
  private String title;
  private UUID organisationKey;
  private String organisationName;
  private String coreType;
  private String subtype;
  private long recordsPublished;
  private Date modified;
  private Date lastPublished;
  private Date nextPublished;
  private PublicationStatus status;
  private PublicationStatus pendingStatus;
  private String creatorName;
  private String shortname;
  private String subject;

  private String organisationAlias;
  private boolean published;
  private boolean dataPackage;

  // public home page only, from the last published version
  // the description as plain text, cut to a few lines
  private String description;
  // the GBIF-supported licence code (CC0-1.0, CC-BY-4.0 or CC-BY-NC-4.0), null if none or another licence
  private String licence;
  // the DOI of the last published version (e.g. 10.1234/abcd) and its URL
  private String doi;
  private String doiUrl;
  // GBIF registry key, when the resource is registered
  private UUID gbifKey;

  // manage page only: what needs doing, worst first in ResourceDataTableViewManagerImpl#toUiAttention
  private boolean publishing;
  private boolean lastPublicationFailed;
  private boolean autoPublishingSuspended;
  private String reservedDoi;
  // update frequency identifier (e.g. monthly) when the resource is auto-published
  private String updateFrequency;

  public String getTitleOrShortname() {
    return title != null ? title : shortname;
  }

  public String getOrganizationAliasOrName() {
    return organisationAlias != null ? organisationAlias : organisationName;
  }
}
