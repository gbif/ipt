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

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Encapsulates all the information for an Organisation.
 */
public class Organisation extends AgentBase implements Serializable {

  private static final long serialVersionUID = 2283765436256564L;

  private Password password = new Password();
  private String alias;
  private String nodeKey;
  private String nodeName;
  private String nodeContactEmail;
  private boolean canHost;
  private boolean agencyAccountPrimary;
  private DOIRegistrationAgency doiRegistrationAgency;
  private String agencyAccountUsername;
  private Password agencyAccountPassword;
  private String doiPrefix;

  public Organisation() {
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Organisation) {
      Organisation other = (Organisation) obj;
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
    if (password != null) {
      return StringUtils.trimToNull(password.password);
    }
    return null;
  }

  /**
   * @return the DOI Registration Agency
   */
  @Nullable
  public DOIRegistrationAgency getDoiRegistrationAgency() {
    return doiRegistrationAgency;
  }

  /**
   * @return the DOI Registration Agency account username
   */
  @Nullable
  public String getAgencyAccountUsername() {
    return agencyAccountUsername;
  }

  /**
   * @return the DOI Registration Agency account password
   */
  @Nullable
  public String getAgencyAccountPassword() {
    if (agencyAccountPassword != null) {
      return StringUtils.trimToNull(agencyAccountPassword.password);
    }
    return null;
  }

  /**
   * @return the DOI prefix that has been assigned to the organization
   */
  @Nullable
  public String getDoiPrefix() {
    return doiPrefix;
  }

  /**
   * @return true if datasets can be registered against this organization during registration, false otherwise.
   */
  public boolean isCanHost() {
    return canHost;
  }

  public void setCanHost(boolean canHost) {
    this.canHost = canHost;
  }

  /**
   * @return true if this account is the only one used by the IPT for registering DOIs, false otherwise.
   */
  public boolean isAgencyAccountPrimary() {
    return agencyAccountPrimary;
  }

  public void setAgencyAccountPrimary(boolean agencyAccountPrimary) {
    this.agencyAccountPrimary = agencyAccountPrimary;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(@Nullable String alias) {
    this.alias = StringUtils.trimToNull(alias);
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
    if (password == null) {
      this.password = new Password();
    }
    this.password.password = password;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * @param doiRegistrationAgency the DOI Registration Agency the organisation has an account with
   */
  public void setDoiRegistrationAgency(@Nullable DOIRegistrationAgency doiRegistrationAgency) {
    this.doiRegistrationAgency = doiRegistrationAgency;
  }

  /**
   * @param agencyAccountUsername the DOI Registration Agency account username
   */
  public void setAgencyAccountUsername(@Nullable String agencyAccountUsername) {
    this.agencyAccountUsername = StringUtils.trimToNull(agencyAccountUsername);
  }

  /**
   * @param agencyAccountPassword the DOI Registration Agency account password
   */
  public void setAgencyAccountPassword(@Nullable String agencyAccountPassword) {
    if (this.agencyAccountPassword == null) {
      this.agencyAccountPassword = new Password();
    }
    this.agencyAccountPassword.password = agencyAccountPassword;
  }

  /**
   * @param doiPrefix the DOI Prefix that has been assigned to the organization
   */
  public void setDoiPrefix(@Nullable String doiPrefix) {
    this.doiPrefix = StringUtils.trimToNull(doiPrefix);
  }

  /**
   * @return test DOI prefix (used in test registrations)
   */
  public String getTestDoiPrefix() {
    return Constants.TEST_DOI_PREFIX;
  }

  /**
   * Create new Organisation by cloning another.
   *
   * @param another Organisation to clone from
   */
  public Organisation(Organisation another) {
    setKey(another.getKey().toString());
    setName(another.getName());

    // fields that can be edited in IPT
    setAlias(another.getAlias());
    setPassword(another.getPassword());
    setCanHost(another.isCanHost());
    setAgencyAccountPrimary(another.isAgencyAccountPrimary());
    setDoiRegistrationAgency(another.getDoiRegistrationAgency());
    setAgencyAccountUsername(another.getAgencyAccountUsername());
    setAgencyAccountPassword(another.getAgencyAccountPassword());
    setDoiPrefix(another.getDoiPrefix());

    // fields that cannot be edited in IPT
    setDescription(another.getDescription());
    setHomepageURL(another.getHomepageURL());
    setPrimaryContactType(another.getPrimaryContactType());
    setPrimaryContactName(another.getPrimaryContactName());
    setPrimaryContactFirstName(another.getPrimaryContactFirstName());
    setPrimaryContactLastName(another.getPrimaryContactLastName());
    setPrimaryContactDescription(another.getPrimaryContactDescription());
    setPrimaryContactAddress(another.getPrimaryContactAddress());
    setPrimaryContactEmail(another.getPrimaryContactEmail());
    setPrimaryContactPhone(another.getPrimaryContactPhone());
    setNodeKey(another.getNodeKey());
    setNodeName(another.getNodeName());
    setNodeContactEmail(another.getNodeContactEmail());
  }
}
