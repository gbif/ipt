/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.model.legacy;

import org.gbif.ipt.model.AgentBase;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Encapsulates all the information for a legacy IPT instance, with password that is String and not Password object
 * that can be encrypted when persisted to registration configuration.
 */
public class LegacyIpt extends AgentBase implements Serializable {

  private static final long serialVersionUID = 78945123624747L;
  private UUID organisationKey;
  private String language;
  private String logoUrl;
  private Date created;
  private String wsPassword;

  /**
   * @return the created
   */
  @Nullable
  public Date getCreated() {
    return created;
  }

  /**
   * @return the description
   */
  @Override
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * @return the language
   */
  @Nullable
  public String getLanguage() {
    return language;
  }

  /**
   * @return the logoUrl
   */
  @Nullable
  public String getLogoUrl() {
    return logoUrl;
  }

  /**
   * @return the organisationKey
   */
  @NotNull
  public UUID getOrganisationKey() {
    return organisationKey;
  }

  /**
   * @return the wsPassword
   */
  @Nullable
  public String getWsPassword() {
    return wsPassword;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(@Nullable Date created) {
    this.created = created;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(@Nullable String description) {
    this.description = StringUtils.trimToNull(description);
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(@Nullable String language) {
    this.language = StringUtils.trimToNull(language);
  }

  /**
   * @param logoUrl the logoUrl to set
   */
  public void setLogoUrl(@Nullable String logoUrl) {
    this.logoUrl = StringUtils.trimToNull(logoUrl);
  }

  /**
   * @param organisationKey the organisationKey to set
   */
  public void setOrganisationKey(@Nullable String organisationKey) {
    this.organisationKey = (organisationKey == null) ? null : UUID.fromString(organisationKey);
  }

  /**
   * @param wsPassword the wsPassword to set
   */
  public void setWsPassword(@Nullable String wsPassword) {
    this.wsPassword = wsPassword;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
