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
import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * This immutable class encapsulates information about a GBIF Service. Instances
 * are built using the builder pattern.
 * 
 * @see http://code.google.com/p/gbif-registry/wiki/ServiceAPI
 * 
 */
@SuppressWarnings("serial")
public class GbrdsService implements Serializable {

  public static class Builder {

    private String accessPointUrl;
    private String description;
    private String descriptionLanguage;
    private String key;
    private String organisationKey;
    private String resourceKey;
    private String resourcePassword;

    private String type;

    private Builder() {
    }

    public Builder accessPointURL(String val) {
      accessPointUrl = val;
      return this;
    }

    public GbrdsService build() {
      return new GbrdsService(this);
    }

    public Builder description(String val) {
      description = val;
      return this;
    }

    public Builder descriptionLanguage(String val) {
      descriptionLanguage = val;
      return this;
    }

    public Builder key(String val) {
      key = val;
      return this;
    }

    public Builder organisationKey(String val) {
      organisationKey = val;
      return this;
    }

    public Builder resourceKey(String val) {
      resourceKey = val;
      return this;
    }

    public Builder resourcePassword(String val) {
      resourcePassword = val;
      return this;
    }

    public Builder type(String val) {
      type = val;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(GbrdsService service) {
    Preconditions.checkNotNull(service);
    return builder().accessPointURL(service.accessPointURL).description(
        service.description).descriptionLanguage(service.descriptionLanguage).key(
        service.key).organisationKey(service.organisationKey).resourceKey(
        service.resourceKey).resourcePassword(service.resourcePassword).type(
        service.type);
  }

  private String accessPointURL;
  private String description;
  private String descriptionLanguage;
  private String key;
  private String organisationKey;
  private String resourceKey;
  private String resourcePassword;
  private String type;

  public GbrdsService(Builder builder) {
    accessPointURL = builder.accessPointUrl;
    description = builder.description;
    descriptionLanguage = builder.descriptionLanguage;
    key = builder.key;
    organisationKey = builder.organisationKey;
    resourceKey = builder.resourceKey;
    resourcePassword = builder.resourcePassword;
    type = builder.type;
  }

  GbrdsService() {
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GbrdsService)) {
      return false;
    }
    GbrdsService o = (GbrdsService) other;
    return Objects.equal(accessPointURL, o.accessPointURL)
        && Objects.equal(description, o.description)
        && Objects.equal(descriptionLanguage, o.descriptionLanguage)
        && Objects.equal(key, o.key)
        && Objects.equal(organisationKey, o.organisationKey)
        && Objects.equal(resourceKey, o.resourceKey)
        && Objects.equal(resourcePassword, o.resourcePassword)
        && Objects.equal(type, o.type);
  }

  public String getAccessPointURL() {
    return accessPointURL;
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionLanguage() {
    return descriptionLanguage;
  }

  public String getKey() {
    return key;
  }

  public String getOrganisationKey() {
    return organisationKey;
  }

  public String getResourceKey() {
    return resourceKey;
  }

  public String getResourcePassword() {
    return resourcePassword;
  }

  public String getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accessPointURL, description, descriptionLanguage,
        key, organisationKey, resourceKey, resourcePassword, type);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("AccessPointUrl", accessPointURL).add(
        "Description", description).add("DescriptionLanguage",
        descriptionLanguage).add("Key", key).add("OrganisationKey",
        organisationKey).add("ResourceKey", resourceKey).add(
        "ResourcePassword", resourcePassword).add("Type", type).toString();
  }
}
