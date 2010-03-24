/*
 * Copyright 2009 GBIF.
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
package org.gbif.provider.model;

import static com.google.common.base.Objects.equal;
import static org.apache.commons.lang.StringUtils.trimToNull;

import com.google.common.base.Objects;

/**
 * This class can be used to encapsulate information about an organization
 * defined by the GBIF Registry. Instances are mutable can be created using the
 * builder pattern or by using the POJO pattern.
 * 
 * @see http://goo.gl/H17q
 * 
 */
public class Organization {

  /**
   * This class can be used to construct Organization instances using the
   * builder pattern.
   * 
   */
  public static class Builder {
    private String name;
    private String description;
    private String homepageUrl;
    private String nameLanguage;
    private String descriptionLanguage;
    private PrimaryContact primaryContact = new PrimaryContact();
    private String nodeKey;
    private String user;
    private String organizationKey;
    private String password;

    private Builder() {
    }

    public Organization build() {
      return new Organization(this);
    }

    public Builder description(String val) {
      description = val;
      return this;
    }

    public Builder descriptionLanguage(String val) {
      descriptionLanguage = val;
      return this;
    }

    public Builder homepageUrl(String val) {
      homepageUrl = val;
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

    public Builder nodeKey(String val) {
      nodeKey = val;
      return this;
    }

    public Builder organizationKey(String val) {
      organizationKey = val;
      return this;
    }

    public Builder password(String val) {
      password = val;
      return this;
    }

    public Builder primaryContactAddress(String val) {
      primaryContact.address = val;
      return this;
    }

    public Builder primaryContactDescription(String val) {
      primaryContact.description = val;
      return this;
    }

    public Builder primaryContactEmail(String val) {
      primaryContact.email = val;
      return this;
    }

    public Builder primaryContactName(String val) {
      primaryContact.name = val;
      return this;
    }

    public Builder primaryContactPhone(String val) {
      primaryContact.phone = val;
      return this;
    }

    public Builder primaryContactType(String val) {
      primaryContact.type = val;
      return this;
    }

    public Builder user(String val) {
      user = val;
      return this;
    }
  }
  private static class PrimaryContact {
    private String type;
    private String name;
    private String address;
    private String description;
    private String email;
    private String phone;

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof PrimaryContact)) {
        return false;
      }
      PrimaryContact o = (PrimaryContact) obj;
      return equal(type, o.type) && equal(name, o.name)
          && equal(address, o.address) && equal(description, o.description)
          && equal(email, o.email) && equal(phone, o.phone);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(type, name, address, description, email, phone);
    }

    @Override
    public String toString() {
      return String.format(
          "Type=%s, Name=%s, Address=%s, Description=%s, Email=%s, Phone=%s",
          type, name, address, description, email, phone);
    }
  }

  /**
   * Returns an organization builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  private String name;
  private String description;
  private String homepageUrl;
  private String nameLanguage;
  private String descriptionLanguage;
  private PrimaryContact primaryContact = new PrimaryContact();
  private String nodeKey;
  private String user;
  private String organizationKey;
  private String password;

  public Organization() {
  }

  private Organization(Builder builder) {
    name = builder.name;
    description = builder.description;
    homepageUrl = builder.homepageUrl;
    nameLanguage = builder.nameLanguage;
    descriptionLanguage = builder.descriptionLanguage;
    primaryContact = builder.primaryContact;
    nodeKey = builder.nodeKey;
    user = builder.user;
    organizationKey = builder.organizationKey;
    password = builder.password;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Organization)) {
      return false;
    }
    Organization o = (Organization) obj;
    return equal(name, o.name) && equal(description, o.description)
        && equal(homepageUrl, o.homepageUrl)
        && equal(nameLanguage, o.nameLanguage)
        && equal(descriptionLanguage, o.descriptionLanguage)
        && equal(primaryContact, o.primaryContact) && equal(nodeKey, o.nodeKey)
        && equal(user, o.user) && equal(organizationKey, o.organizationKey)
        && equal(password, o.password);
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionLanguage() {
    return descriptionLanguage;
  }

  public String getHomepageUrl() {
    return homepageUrl;
  }

  public String getName() {
    return name;
  }

  public String getNameLanguage() {
    return nameLanguage;
  }

  public String getNodeKey() {
    return nodeKey;
  }

  public String getOrganizationKey() {
    return organizationKey;
  }

  public String getPassword() {
    return password;
  }

  public String getPrimaryContactAddress() {
    return primaryContact.address;
  }

  public String getPrimaryContactDescription() {
    return primaryContact.description;
  }

  public String getPrimaryContactEmail() {
    return primaryContact.email;
  }

  public String getPrimaryContactName() {
    return primaryContact.name;
  }

  public String getPrimaryContactPhone() {
    return primaryContact.phone;
  }

  public String getPrimaryContactType() {
    return primaryContact.type;
  }

  public String getUser() {
    return user;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, homepageUrl, nameLanguage,
        descriptionLanguage, primaryContact, nodeKey, user, organizationKey,
        password);
  }

  public boolean isRegistered() {
    return organizationKey != null && trimToNull(organizationKey) != null;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDescriptionLanguage(String descriptionLanguage) {
    this.descriptionLanguage = descriptionLanguage;
  }

  public void setHomepageUrl(String homepageUrl) {
    this.homepageUrl = homepageUrl;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNameLanguage(String nameLanguage) {
    this.nameLanguage = nameLanguage;
  }

  public void setNodeKey(String nodeKey) {
    this.nodeKey = nodeKey;
  }

  public void setOrganizationKey(String organizationKey) {
    this.organizationKey = organizationKey;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPrimaryContactAddress(String primaryContactAddress) {
    this.primaryContact.address = primaryContactAddress;
  }

  public void setPrimaryContactDescription(String primaryContactDescription) {
    this.primaryContact.description = primaryContactDescription;
  }

  public void setPrimaryContactEmail(String primaryContactEmail) {
    this.primaryContact.email = primaryContactEmail;
  }

  public void setPrimaryContactName(String primaryContactName) {
    this.primaryContact.name = primaryContactName;
  }

  public void setPrimaryContactPhone(String primaryContactPhone) {
    this.primaryContact.phone = primaryContactPhone;
  }

  public void setPrimaryContactType(String primaryContactType) {
    this.primaryContact.type = primaryContactType;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return String.format(
        "Name=%s, Description=%s, HomepageUrl=%s, NameLanugage=%s, DescriptionLanguage=%s, PrimaryContact=[%s], NodeKey=%s, User=%s, OrganizationKey=%s, Password=%s",
        name, description, homepageUrl, nameLanguage, descriptionLanguage,
        primaryContact, nodeKey, user, organizationKey, password);
  }
}
