/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.model;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;


/**
 * @author markus
 * 
 */
public abstract class AgentBase {

  private UUID key;
  protected String description;
  private String name;
  private String homepageURL;
  private String primaryContactType;
  private String primaryContactName;
  private String primaryContactDescription;
  private String primaryContactAddress;
  private String primaryContactEmail;
  private String primaryContactPhone;

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the homepageURL
   */
  public String getHomepageURL() {
    return homepageURL;
  }

  /**
   * @return the key
   */
  public UUID getKey() {
    return key;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
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
   * @param homepageURL the homepageURL to set
   */
  public void setHomepageURL(String homepageURL) {
    this.homepageURL = StringUtils.trimToNull(homepageURL);
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = UUID.fromString(key);
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = StringUtils.trimToNull(name);
  }

  /**
   * @param primaryContactAddress the primaryContactAddress to set
   */
  public void setPrimaryContactAddress(String primaryContactAddress) {
    this.primaryContactAddress = StringUtils.trimToNull(primaryContactAddress);
  }

  /**
   * @param primaryContactDescription the primaryContactDescription to set
   */
  public void setPrimaryContactDescription(String primaryContactDescription) {
    this.primaryContactDescription = StringUtils.trimToNull(primaryContactDescription);
  }

  /**
   * @param primaryContactEmail the primaryContactEmail to set
   */
  public void setPrimaryContactEmail(String primaryContactEmail) {
    this.primaryContactEmail = StringUtils.trimToNull(primaryContactEmail);
  }

  /**
   * @param primaryContactName the primaryContactName to set
   */
  public void setPrimaryContactName(String primaryContactName) {
    this.primaryContactName = StringUtils.trimToNull(primaryContactName);
  }

  /**
   * @param primaryContactPhone the primaryContactPhone to set
   */
  public void setPrimaryContactPhone(String primaryContactPhone) {
    this.primaryContactPhone = StringUtils.trimToNull(primaryContactPhone);
  }

  /**
   * @param primaryContactType the primaryContactType to set
   */
  public void setPrimaryContactType(String primaryContactType) {
    this.primaryContactType = StringUtils.trimToNull(primaryContactType);
  }

}
