/*
 * Copyright 2010 GBIF.
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
 */
package org.gbif.registry.api.client;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * This immutable class encapsulates information about a GBIF Organisation.
 * Instances are built using the builder pattern.
 * 
 * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI
 * 
 */
@SuppressWarnings("serial")
public class GbrdsOrganisation implements Serializable {

  /**
   * This class builds {@link GbrdsOrganisation} instances using the builder
   * pattern.
   * 
   */
  public static class Builder {
    private String description;
    private String descriptionLanguage;
    private String homepageURL;
    private String key;
    private String name;
    private String nameLanguage;
    private String nodeContactEmail;
    private String nodeKey;
    private String nodeName;
    private String password;
    private String primaryContactAddress;
    private String primaryContactDescription;
    private String primaryContactEmail;
    private String primaryContactName;
    private String primaryContactPhone;
    private String primaryContactType;

    private Builder() {
    }

    public GbrdsOrganisation build() {
      return new GbrdsOrganisation(this);
    }

    public Builder description(String val) {
      description = val;
      return this;
    }

    public Builder descriptionLanguage(String val) {
      descriptionLanguage = val;
      return this;
    }

    public String getDescription() {
      return description;
    }

    public String getDescriptionLanguage() {
      return descriptionLanguage;
    }

    public String getHomepageURL() {
      return homepageURL;
    }

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }

    public String getNameLanguage() {
      return nameLanguage;
    }

    public String getNodeContactEmail() {
      return nodeContactEmail;
    }

    public String getNodeKey() {
      return nodeKey;
    }

    public String getNodeName() {
      return nodeName;
    }

    public String getPrimaryContactAddress() {
      return primaryContactAddress;
    }

    public String getPrimaryContactDescription() {
      return primaryContactDescription;
    }

    public String getPrimaryContactEmail() {
      return primaryContactEmail;
    }

    public String getPrimaryContactName() {
      return primaryContactName;
    }

    public String getPrimaryContactPhone() {
      return primaryContactPhone;
    }

    public String getPrimaryContactType() {
      return primaryContactType;
    }

    public Builder homepageURL(String val) {
      homepageURL = val;
      return this;
    }

    public Builder key(String val) {
      key = val;
      return this;
    }

    public Builder name(String val) {
      name = val;
      return this;
    }

    public Builder nameLanguage(String val) {
      nameLanguage = val;
      return this;
    }

    public Builder nodeContactEmail(String val) {
      nodeContactEmail = val;
      return this;
    }

    public Builder nodeKey(String val) {
      nodeKey = val;
      return this;
    }

    public Builder nodeName(String val) {
      nodeName = val;
      return this;
    }

    public Builder password(String val) {
      password = val;
      return this;
    }

    public Builder primaryContactAddress(String val) {
      primaryContactAddress = val;
      return this;
    }

    public Builder primaryContactDescription(String val) {
      primaryContactDescription = val;
      return this;
    }

    public Builder primaryContactEmail(String val) {
      primaryContactEmail = val;
      return this;
    }

    public Builder primaryContactName(String val) {
      primaryContactName = val;
      return this;
    }

    public Builder primaryContactPhone(String val) {
      primaryContactPhone = val;
      return this;
    }

    public Builder primaryContactType(String val) {
      primaryContactType = val;
      return this;
    }

  }

  /**
   * Returns a new {@link Builder}.
   * 
   * @return Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private final String description;
  private final String descriptionLanguage;
  private final String homepageURL;
  private final String key;
  private final String name;
  private final String nameLanguage;
  private final String nodeContactEmail;
  private final String nodeKey;
  private final String nodeName;
  private final String password;
  private final String primaryContactAddress;
  private final String primaryContactDescription;
  private final String primaryContactEmail;
  private final String primaryContactName;
  private final String primaryContactPhone;
  private final String primaryContactType;

  GbrdsOrganisation() {
    this(new Builder());
  }

  private GbrdsOrganisation(Builder builder) {
    description = builder.description;
    descriptionLanguage = builder.descriptionLanguage;
    homepageURL = builder.homepageURL;
    key = builder.key;
    name = builder.name;
    nameLanguage = builder.nameLanguage;
    nodeContactEmail = builder.nodeContactEmail;
    nodeKey = builder.nodeKey;
    nodeName = builder.nodeName;
    password = builder.password;
    primaryContactAddress = builder.primaryContactAddress;
    primaryContactDescription = builder.primaryContactDescription;
    primaryContactEmail = builder.primaryContactEmail;
    primaryContactName = builder.primaryContactName;
    primaryContactPhone = builder.primaryContactPhone;
    primaryContactType = builder.primaryContactType;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GbrdsOrganisation)) {
      return false;
    }
    GbrdsOrganisation o = (GbrdsOrganisation) other;
    return Objects.equal(description, o.description)
        && Objects.equal(descriptionLanguage, o.descriptionLanguage)
        && Objects.equal(homepageURL, o.homepageURL)
        && Objects.equal(key, o.key)
        && Objects.equal(name, o.name)
        && Objects.equal(nameLanguage, o.nameLanguage)
        && Objects.equal(nodeContactEmail, o.nodeContactEmail)
        && Objects.equal(nodeKey, o.nodeKey)
        && Objects.equal(nodeName, o.nodeName)
        && Objects.equal(primaryContactAddress, o.primaryContactAddress)
        && Objects.equal(primaryContactDescription, o.primaryContactDescription)
        && Objects.equal(primaryContactEmail, o.primaryContactEmail)
        && Objects.equal(primaryContactName, o.primaryContactName)
        && Objects.equal(primaryContactPhone, o.primaryContactPhone)
        && Objects.equal(primaryContactType, o.primaryContactType);
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionLanguage() {
    return descriptionLanguage;
  }

  public String getHomepageURL() {
    return homepageURL;
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public String getNameLanguage() {
    return nameLanguage;
  }

  public String getNodeContactEmail() {
    return nodeContactEmail;
  }

  public String getNodeKey() {
    return nodeKey;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getPassword() {
    return password;
  }

  public String getPrimaryContactAddress() {
    return primaryContactAddress;
  }

  public String getPrimaryContactDescription() {
    return primaryContactDescription;
  }

  public String getPrimaryContactEmail() {
    return primaryContactEmail;
  }

  public String getPrimaryContactName() {
    return primaryContactName;
  }

  public String getPrimaryContactPhone() {
    return primaryContactPhone;
  }

  public String getPrimaryContactType() {
    return primaryContactType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, descriptionLanguage, homepageURL, key,
        name, nameLanguage, nodeContactEmail, nodeKey, nodeName, password,
        primaryContactAddress, primaryContactDescription, primaryContactEmail,
        primaryContactName, primaryContactPhone, primaryContactType);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("Description", description).add(
        "DescriptionLanguage", descriptionLanguage).add("HomepageURL",
        homepageURL).add("Key", key).add("Name", name).add("NameLanguage",
        nameLanguage).add("NodeContactEmail", nodeContactEmail).add("NodeKey",
        nodeKey).add("NodeName", nodeName).add("Password", password).add(
        "PrimaryContactAddress", primaryContactAddress).add(
        "PrimaryContactDescription", primaryContactDescription).add(
        "PrimaryContactEmail", primaryContactEmail).add("PrimaryContactName",
        primaryContactName).add("PrimaryContactPhone", primaryContactPhone).add(
        "PrimaryContactType", primaryContactType).toString();
  }
}
