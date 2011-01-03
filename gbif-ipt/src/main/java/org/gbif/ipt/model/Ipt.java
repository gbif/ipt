/*
 * Copyright 2009 GBIF.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Encapsulates all the information for an Ipt instance
 */
public class Ipt extends AgentBase implements Serializable {

  private static final long serialVersionUID = 78945123624747L;
  private UUID organisationKey;
  private String language;
  private String logoUrl;
  private Date created;
  private String wsPassword;

  /**
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * @return the description
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @return the logoUrl
   */
  public String getLogoUrl() {
    return logoUrl;
  }

  /**
   * @return the organisationKey
   */
  public UUID getOrganisationKey() {
    return organisationKey;
  }

  /**
   * @return the wsPassword
   */
  public String getWsPassword() {
    return wsPassword;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = StringUtils.trimToNull(description);
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = StringUtils.trimToNull(language);
  }

  /**
   * @param logoUrl the logoUrl to set
   */
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = StringUtils.trimToNull(logoUrl);
  }

  /**
   * @param organisationKey the organisationKey to set
   */
  public void setOrganisationKey(String organisationKey) {
    this.organisationKey = UUID.fromString(organisationKey);
  }

  /**
   * @param wsPassword the wsPassword to set
   */
  public void setWsPassword(String wsPassword) {
    this.wsPassword = wsPassword;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}