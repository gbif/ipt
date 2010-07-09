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
 * This immutable class encapsulates information about a GBRDS Service.
 * Instances are built using the builder pattern.
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
    private String resourceKey;
    private String type;
    private String typeDescription;

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

    public Builder resourceKey(String val) {
      resourceKey = val;
      return this;
    }

    public Builder type(String val) {
      type = val;
      return this;
    }

    public Builder typeDescription(String val) {
      typeDescription = val;
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
        service.key).resourceKey(service.resourceKey).type(service.type);
  }

  private final String accessPointURL;
  private final String description;
  private final String descriptionLanguage;
  private final String key;
  private final String resourceKey;
  private final String type;
  private final String typeDescription;

  public GbrdsService(Builder builder) {
    accessPointURL = builder.accessPointUrl;
    description = builder.description;
    descriptionLanguage = builder.descriptionLanguage;
    key = builder.key;
    resourceKey = builder.resourceKey;
    type = builder.type;
    typeDescription = builder.typeDescription;
  }

  GbrdsService() {
    this(builder());
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
        && Objects.equal(resourceKey, o.resourceKey)
        && Objects.equal(type, o.type)
        && Objects.equal(typeDescription, o.typeDescription);
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

  public String getResourceKey() {
    return resourceKey;
  }

  public String getType() {
    return type;
  }

  public String getTypeDescription() {
    return typeDescription;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accessPointURL, description, descriptionLanguage,
        key, resourceKey, type, typeDescription);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("AccessPointUrl", accessPointURL).add(
        "Description", description).add("DescriptionLanguage",
        descriptionLanguage).add("Key", key).add("ResourceKey", resourceKey).add(
        "Type", type).toString();
  }
}
