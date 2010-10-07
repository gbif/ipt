/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;
import java.util.UUID;

/**
 * Encapsulates all the information for an Ipt instance
 */
public class Ipt {

  private UUID key;
  private UUID organisationKey;
  private String name;
  private String description;
  private String language;
  private String homepageUrl;
  private String logoUrl;
  private Date created;
  private String primaryContactType;
  private String primaryContactName;
  private String primaryContactAddress;
  private String primaryContactDescription;
  private String primaryContactEmail;
  private String primaryContactPhone;
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
  public String getDescription() {
    return description;
  }

  /**
   * @return the homepageUrl
   */
  public String getHomepageUrl() {
    return homepageUrl;
  }

  /**
   * @return the key
   */
  public UUID getKey() {
    return key;
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
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the organisationKey
   */
  public UUID getOrganisationKey() {
    return organisationKey;
  }

  /**
   * @return the primaryContactAddress
   */
  public String getPrimaryContactAddress() {
    return primaryContactAddress;
  }

  /**
   * @return the primaryContactDescription
   */
  public String getPrimaryContactDescription() {
    return primaryContactDescription;
  }

  /**
   * @return the primaryContactEmail
   */
  public String getPrimaryContactEmail() {
    return primaryContactEmail;
  }

  /**
   * @return the primaryContactName
   */
  public String getPrimaryContactName() {
    return primaryContactName;
  }

  /**
   * @return the primaryContactPhone
   */
  public String getPrimaryContactPhone() {
    return primaryContactPhone;
  }

  /**
   * @return the primaryContactType
   */
  public String getPrimaryContactType() {
    return primaryContactType;
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
    this.description = description;
  }

  /**
   * @param homepageUrl the homepageUrl to set
   */
  public void setHomepageUrl(String homepageUrl) {
    this.homepageUrl = homepageUrl;
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = UUID.fromString(key);
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @param logoUrl the logoUrl to set
   */
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param organisationKey the organisationKey to set
   */
  public void setOrganisationKey(String organisationKey) {
    this.organisationKey = UUID.fromString(organisationKey);
  }

  /**
   * @param primaryContactAddress the primaryContactAddress to set
   */
  public void setPrimaryContactAddress(String primaryContactAddress) {
    this.primaryContactAddress = primaryContactAddress;
  }

  /**
   * @param primaryContactDescription the primaryContactDescription to set
   */
  public void setPrimaryContactDescription(String primaryContactDescription) {
    this.primaryContactDescription = primaryContactDescription;
  }

  /**
   * @param primaryContactEmail the primaryContactEmail to set
   */
  public void setPrimaryContactEmail(String primaryContactEmail) {
    this.primaryContactEmail = primaryContactEmail;
  }

  /**
   * @param primaryContactName the primaryContactName to set
   */
  public void setPrimaryContactName(String primaryContactName) {
    this.primaryContactName = primaryContactName;
  }

  /**
   * @param primaryContactPhone the primaryContactPhone to set
   */
  public void setPrimaryContactPhone(String primaryContactPhone) {
    this.primaryContactPhone = primaryContactPhone;
  }

  /**
   * @param primaryContactType the primaryContactType to set
   */
  public void setPrimaryContactType(String primaryContactType) {
    this.primaryContactType = primaryContactType;
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