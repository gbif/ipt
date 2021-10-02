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
package org.gbif.ipt.model;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

public abstract class AgentBase {

  private UUID key;
  protected String description;
  private String name;
  private String homepageURL;
  private String primaryContactType;
  private String primaryContactName;
  private String primaryContactFirstName;
  private String primaryContactLastName;
  private String primaryContactDescription;
  private String primaryContactAddress;
  private String primaryContactEmail;
  private String primaryContactPhone;

  /**
   * @return the description
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * @return the homepageURL
   */
  @Nullable
  public String getHomepageURL() {
    return homepageURL;
  }

  /**
   * @return the key
   */
  @NotNull
  public UUID getKey() {
    return key;
  }

  /**
   * @return the name
   */
  @NotNull
  public String getName() {
    return name;
  }

  /**
   * @return the primaryContactAddress
   */
  @Nullable
  public String getPrimaryContactAddress() {
    return primaryContactAddress;
  }

  /**
   * @return the primaryContactDescription
   */
  @Nullable
  public String getPrimaryContactDescription() {
    return primaryContactDescription;
  }

  /**
   * @return the primaryContactEmail
   */
  @Nullable
  public String getPrimaryContactEmail() {
    return primaryContactEmail;
  }

  /**
   * @return the primaryContactFirstName
   */
  @Nullable
  public String getPrimaryContactFirstName() {
    return primaryContactFirstName;
  }

  /**
   * @return the primaryContactLastName
   */
  @Nullable
  public String getPrimaryContactLastName() {
    return primaryContactLastName;
  }

  /**
   * @return the primaryContactName
   */
  @Nullable
  public String getPrimaryContactName() {
    return primaryContactName;
  }

  /**
   * @return the primaryContactPhone
   */
  @Nullable
  public String getPrimaryContactPhone() {
    return primaryContactPhone;
  }

  /**
   * @return the primaryContactType
   */
  @Nullable
  public String getPrimaryContactType() {
    return primaryContactType;
  }

  /**
   * @param homepageURL the homepageURL to set
   */
  public void setHomepageURL(@Nullable String homepageURL) {
    this.homepageURL = StringUtils.trimToNull(homepageURL);
  }

  /**
   * @param key the key to set
   */
  public void setKey(@NotNull String key) {
    this.key = UUID.fromString(key);
  }

  /**
   * @param name the name to set
   */
  public void setName(@NotNull String name) {
    this.name = StringUtils.trimToNull(name);
  }

  /**
   * @param primaryContactAddress the primaryContactAddress to set
   */
  public void setPrimaryContactAddress(@Nullable String primaryContactAddress) {
    this.primaryContactAddress = StringUtils.trimToNull(primaryContactAddress);
  }

  /**
   * @param primaryContactDescription the primaryContactDescription to set
   */
  public void setPrimaryContactDescription(@Nullable String primaryContactDescription) {
    this.primaryContactDescription = StringUtils.trimToNull(primaryContactDescription);
  }

  /**
   * @param primaryContactEmail the primaryContactEmail to set
   */
  public void setPrimaryContactEmail(@Nullable String primaryContactEmail) {
    this.primaryContactEmail = StringUtils.trimToNull(primaryContactEmail);
  }

  public void setPrimaryContactFirstName(@Nullable String primaryContactFirstName) {
    this.primaryContactFirstName = StringUtils.trimToNull(primaryContactFirstName);
  }

  public void setPrimaryContactLastName(@Nullable String primaryContactLastName) {
    this.primaryContactLastName = StringUtils.trimToNull(primaryContactLastName);
  }

  /**
   * @param primaryContactName the primaryContactName to set
   */
  public void setPrimaryContactName(@Nullable String primaryContactName) {
    this.primaryContactName = StringUtils.trimToNull(primaryContactName);
  }

  /**
   * @param primaryContactPhone the primaryContactPhone to set
   */
  public void setPrimaryContactPhone(@Nullable String primaryContactPhone) {
    this.primaryContactPhone = StringUtils.trimToNull(primaryContactPhone);
  }

  /**
   * @param primaryContactType the primaryContactType to set
   */
  public void setPrimaryContactType(@Nullable String primaryContactType) {
    this.primaryContactType = StringUtils.trimToNull(primaryContactType);
  }

}
