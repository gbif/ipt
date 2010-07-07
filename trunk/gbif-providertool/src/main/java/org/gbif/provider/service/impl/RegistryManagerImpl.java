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
package org.gbif.provider.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsRegistry;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
import org.gbif.registry.api.client.Gbrds.Credentials;
import org.gbif.registry.api.client.Gbrds.IptApi;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.gbif.registry.api.client.Gbrds.ResourceApi;
import org.gbif.registry.api.client.Gbrds.ServiceApi;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesForResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.gbif.registry.api.client.GbrdsResource.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * This class provides a default implementation of {@link RegistryManager}.
 * 
 */
public class RegistryManagerImpl implements RegistryManager {

  @Autowired
  protected AppConfig cfg;

  private final OrganisationApi organsiationApi;
  private final ResourceApi resourceApi;
  private final ServiceApi serviceApi;
  private final IptApi iptApi;

  private RegistryManagerImpl() {
    Gbrds gbif = GbrdsRegistry.init("http://gbrdsdev.gbif.org");
    organsiationApi = gbif.getOrganisationApi();
    resourceApi = gbif.getResourceApi();
    serviceApi = gbif.getServiceApi();
    iptApi = gbif.getIptApi();
  }

  /**
   * @see RegistryManager#buildGbrdsOrganisation(ResourceMetadata)
   */
  public org.gbif.registry.api.client.GbrdsOrganisation.Builder buildGbrdsOrganisation(
      ResourceMetadata resourceMetadata) {
    String description = resourceMetadata.getDescription();
    String homepageURL = resourceMetadata.getLink();
    String key = resourceMetadata.getUddiID();
    String name = resourceMetadata.getTitle();
    String nodeKey = cfg.getOrgNode();
    String nodeName = cfg.getOrgNodeName();
    String password = cfg.getOrgPassword();
    String primaryContactEmail = resourceMetadata.getContactEmail();
    String primaryContactName = resourceMetadata.getContactName();
    String primaryContactType = ContactType.administrative.name();
    return GbrdsOrganisation.builder().description(description).homepageURL(
        homepageURL).key(key).name(name).nodeKey(nodeKey).nodeName(nodeName).password(
        password).primaryContactEmail(primaryContactEmail).primaryContactName(
        primaryContactName).primaryContactType(primaryContactType);
  }

  /**
   * @see RegistryManager#buildGbrdsResource(ResourceMetadata)
   */
  public Builder buildGbrdsResource(ResourceMetadata resourceMetadata) {
    checkNotNull(resourceMetadata);
    String key = resourceMetadata.getUddiID();
    String password = cfg.getOrgPassword();
    String name = resourceMetadata.getTitle();
    String description = resourceMetadata.getDescription();
    String homepageUrl = resourceMetadata.getLink();
    String primaryContactType = ContactType.administrative.name();
    String primaryContactName = resourceMetadata.getContactName();
    String primaryContactEmail = resourceMetadata.getContactEmail();
    return GbrdsResource.builder().key(key).organisationPassword(password).name(
        name).description(description).homepageURL(homepageUrl).primaryContactType(
        primaryContactType).primaryContactName(primaryContactName).primaryContactEmail(
        primaryContactEmail);
  }

  /**
   * @see RegistryManager#createGbrdsOrganisation(GbrdsOrganisation)
   */
  public CreateOrgResponse createGbrdsOrganisation(
      GbrdsOrganisation gbifOrganisation) throws RegistryException {
    checkNotNull(gbifOrganisation);
    CreateOrgResponse response = organsiationApi.create(gbifOrganisation).execute();
    if (response.getError() != null) {
      String msg = String.format("GBIF Organisation not created %s",
          gbifOrganisation);
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @see RegistryManager#createGbrdsResource(GbrdsResource)
   */
  public CreateResourceResponse createGbrdsResource(GbrdsResource gbifResource)
      throws RegistryException {
    checkNotNull(gbifResource);
    CreateResourceResponse response = resourceApi.create(gbifResource).execute();
    if (response.getError() != null) {
      String msg = String.format("GBIF Resource not created %s", gbifResource);
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @see RegistryManager#createGbrdsService(GbrdsService)
   */
  public CreateServiceResponse createGbrdsService(GbrdsService service)
      throws RegistryException {
    checkNotNull(service);
    checkArgument(!service.getAccessPointURL().contains("localhost"));
    CreateServiceResponse response = serviceApi.create(service).execute();
    Throwable error = response.getError();
    if (error != null) {
      throw new RegistryException("Error creating service", error);
    }
    int status = response.getStatus();
    if (status != HttpStatus.SC_CREATED) {
      throw new RegistryException("Service not created");
    }
    return response;
  }

  /**
   * @see RegistryManager#deleteGbrdsResource(GbrdsResource)
   */
  public DeleteResourceResponse deleteGbrdsResource(GbrdsResource resource)
      throws RegistryException {
    checkNotNull(resource);
    DeleteResourceResponse response = resourceApi.delete(resource).execute();
    Throwable error = response.getError();
    if (error != null) {
      throw new RegistryException("Error deleting resource", error);
    }
    int status = response.getStatus();
    if (status != HttpStatus.SC_OK) {
      throw new RegistryException("Resource not deleted");
    }
    return response;
  }

  /**
   * @see RegistryManager#deleteGbrdsService(GbrdsService)
   */
  public DeleteServiceResponse deleteGbrdsService(GbrdsService service)
      throws RegistryException {
    checkNotNull(service);
    DeleteServiceResponse response = serviceApi.delete(service).execute();
    Throwable error = response.getError();
    if (error != null) {
      throw new RegistryException("Error deleting service", error);
    }
    int status = response.getStatus();
    if (status != HttpStatus.SC_OK) {
      throw new RegistryException("Service not deleted");
    }
    return response;
  }

  /**
   * @see RegistryManager#listAllExtensions()
   */
  public Collection<String> listAllExtensions() {
    List<GbrdsExtension> extensions = iptApi.listExtensions().execute().getResult();
    return Lists.transform(extensions, new Function<GbrdsExtension, String>() {
      public String apply(GbrdsExtension ge) {
        return ge.getUrl();
      }
    });
  }

  /**
   * @see RegistryManager#listAllThesauri()
   */
  public Collection<String> listAllThesauri() {
    List<GbrdsThesaurus> thesauri = iptApi.listThesauri().execute().getResult();
    return Lists.transform(thesauri, new Function<GbrdsThesaurus, String>() {
      public String apply(GbrdsThesaurus gt) {
        return gt.getUrl();
      }
    });
  }

  /**
   * @see RegistryManager#listGbifServices(String )
   */
  public ListServicesForResourceResponse listGbrdsServicesForGbrdsResource(
      String gbifResourceKey) throws RegistryException {
    checkNotNull(gbifResourceKey);
    checkArgument(gbifResourceKey.length() > 0);
    ListServicesForResourceResponse response = serviceApi.list(gbifResourceKey).execute();
    Throwable error = response.getError();
    if (error != null) {
      throw new RegistryException("Unable to list services", error);
    }
    int status = response.getStatus();
    if (status != HttpStatus.SC_OK) {
      throw new RegistryException("Unable to list services");
    }
    return response;
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#readGbrdsOrganisation(String)
   */
  public ReadOrgResponse readGbrdsOrganisation(String organisationKey)
      throws RegistryException {
    checkNotNull(organisationKey);
    checkArgument(organisationKey.trim().length() != 0);
    ReadOrgResponse response = organsiationApi.read(organisationKey).execute();
    if (response.getError() != null) {
      String msg = String.format(
          "Unable to read GBIF Organisation with key %s", organisationKey);
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#readGbrdsResource(String)
   */
  public ReadResourceResponse readGbrdsResource(String resourceKey)
      throws RegistryException {
    checkNotNull(resourceKey);
    checkArgument(resourceKey.trim().length() != 0);
    ReadResourceResponse response = resourceApi.read(resourceKey).execute();
    Throwable error = response.getError();
    if (error != null) {
      String msg = String.format("Error reading GBIF Resource with key %s",
          resourceKey);
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @see RegistryManager#updateGbrdsOrganisation(GbrdsOrganisation)
   */
  public UpdateOrgResponse updateGbrdsOrganisation(
      GbrdsOrganisation gbifOrganisation) throws RegistryException {
    checkNotNull(gbifOrganisation);
    UpdateOrgResponse response = organsiationApi.update(gbifOrganisation).execute();
    if (response.getError() != null) {
      String msg = String.format(
          "Unable to read GBIF Organisation with key %s",
          gbifOrganisation.getKey());
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @see RegistryManager#updateGbrdsResource(GbrdsResource)
   */
  public UpdateResourceResponse updateGbrdsResource(GbrdsResource gbifResource)
      throws RegistryException {
    checkNotNull(gbifResource);
    UpdateResourceResponse response = resourceApi.update(gbifResource).execute();
    if (response.getError() != null) {
      String msg = String.format("Unable to read GBIF Resource with key %s",
          gbifResource.getKey());
      throw new RegistryException(msg, response.getError());
    }
    return response;
  }

  /**
   * @see RegistryManager#validateGbifOrganisationCredentials(String,
   *      Credentials)
   */
  public ValidateOrgCredentialsResponse validateGbifOrganisationCredentials(
      String gbigOrganisationKey, Credentials credentials) {
    return organsiationApi.validateCredentials(gbigOrganisationKey, credentials).execute();
  }
}
