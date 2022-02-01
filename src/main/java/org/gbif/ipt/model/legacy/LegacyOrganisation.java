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
package org.gbif.ipt.model.legacy;

import org.gbif.ipt.model.AgentBase;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Encapsulates all the information for a legacy Organisation, with password that is String and not Password object
 * that can be encrypted when persisted to registration configuration.
 */
public class LegacyOrganisation extends AgentBase implements Serializable {

  private static final long serialVersionUID = 2238765436256564L;

  private String password;
  private String alias;
  private String nodeKey;
  private String nodeName;
  private String nodeContactEmail;
  private boolean canHost;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LegacyOrganisation) {
      LegacyOrganisation other = (LegacyOrganisation) obj;
      return Objects.equals(this.getKey(), other.getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getKey());
  }

  /**
   * @return the alias
   */
  @Nullable
  public String getAlias() {
    return alias;
  }

  /**
   * @return the nodeContactEmail
   */
  @Nullable
  public String getNodeContactEmail() {
    return nodeContactEmail;
  }

  /**
   * @return the nodeKey
   */
  @Nullable
  public String getNodeKey() {
    return nodeKey;
  }

  /**
   * @return the nodeName
   */
  @Nullable
  public String getNodeName() {
    return nodeName;
  }

  /**
   * @return the password
   */
  @Nullable
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
  public void setAlias(@Nullable String alias) {
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
  public void setDescription(@Nullable String description) {
    this.description = StringUtils.trimToNull(description);
  }

  /**
   * @param nodeContactEmail the nodeContactEmail to set
   */
  public void setNodeContactEmail(@Nullable String nodeContactEmail) {
    this.nodeContactEmail = StringUtils.trimToNull(nodeContactEmail);
  }

  /**
   * @param nodeKey the nodeKey to set
   */
  public void setNodeKey(@Nullable String nodeKey) {
    this.nodeKey = StringUtils.trimToNull(nodeKey);
  }

  /**
   * @param nodeName the nodeName to set
   */
  public void setNodeName(@Nullable String nodeName) {
    this.nodeName = StringUtils.trimToNull(nodeName);
  }

  /**
   * @param password the password to set
   */
  public void setPassword(@Nullable String password) {
    this.password = StringUtils.trimToNull(password);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
