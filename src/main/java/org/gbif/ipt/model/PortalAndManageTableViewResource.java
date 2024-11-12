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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Simplified IPT resource view for main and manage pages.
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 */
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class PortalAndManageTableViewResource {

  private String logoUrl;
  private String title;
  private UUID organisationKey;
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
  private boolean dataPackage;

  public String getTitleOrShortname() {
    return title != null ? title : shortname;
  }

  public String getOrganizationAliasOrName() {
    return organisationAlias != null ? organisationAlias : organisationName;
  }
}
