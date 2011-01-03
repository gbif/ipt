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

/**
 * Encapsulates all the information for an Organisation
 */
public class Organisation extends AgentBase implements Serializable {
  private static final long serialVersionUID = 2238765436256564L;

  private String password;
  private String alias;
  private String nodeKey;
  private String nodeName;
  private String nodeContactEmail;
  private boolean canHost;

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (((Organisation) obj).getKey().equals(this.getKey())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
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
   * @return the canHost
   */
  public boolean isCanHost() {
    return canHost;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(String alias) {
    this.alias = StringUtils.trimToNull(alias);
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
    this.description = StringUtils.trimToNull(description);
  }

  /**
   * @param nodeContactEmail the nodeContactEmail to set
   */
  public void setNodeContactEmail(String nodeContactEmail) {
    this.nodeContactEmail = StringUtils.trimToNull(nodeContactEmail);
  }

  /**
   * @param nodeKey the nodeKey to set
   */
  public void setNodeKey(String nodeKey) {
    this.nodeKey = StringUtils.trimToNull(nodeKey);
  }

  /**
   * @param nodeName the nodeName to set
   */
  public void setNodeName(String nodeName) {
    this.nodeName = StringUtils.trimToNull(nodeName);
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = StringUtils.trimToNull(password);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}