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

import java.util.UUID;

/**
 * Encapsulates all the information for an Organisation
 */
public class Organisation {

  private UUID key;
  private String description;
  private String name;
  private String password;
  private String alias;
  private String homepageURL;
  private String primaryContactType;
  private String primaryContactName;
  private String primaryContactDescription;
  private String primaryContactAddress;
  private String primaryContactEmail;
  private String primaryContactPhone;
  private String nodeKey;
  private String nodeName;
  private String nodeContactEmail;
  private boolean canHost;

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

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
   * @return the nodeContactEmail
   */
  public String getNodeContactEmail() {
    return nodeContactEmail;
  }

  /**
   * @return the nodeKey
   */
  public String getNodeKey() {
    return nodeKey;
  }

  /**
   * @return the nodeName
   */
  public String getNodeName() {
    return nodeName;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
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
   * @return the canHost
   */
  public boolean isCanHost() {
    return canHost;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * @param canHost the canHost to set
   */
  public void setCanHost(boolean canHost) {
    this.canHost = canHost;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param homepageURL the homepageURL to set
   */
  public void setHomepageURL(String homepageURL) {
    this.homepageURL = homepageURL;
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
    this.name = name;
  }

  /**
   * @param nodeContactEmail the nodeContactEmail to set
   */
  public void setNodeContactEmail(String nodeContactEmail) {
    this.nodeContactEmail = nodeContactEmail;
  }

  /**
   * @param nodeKey the nodeKey to set
   */
  public void setNodeKey(String nodeKey) {
    this.nodeKey = nodeKey;
  }

  /**
   * @param nodeName the nodeName to set
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if( ((Organisation)obj).getKey().equals(this.getKey()))
      return true;
    else
      return false;
  }
  
  

}