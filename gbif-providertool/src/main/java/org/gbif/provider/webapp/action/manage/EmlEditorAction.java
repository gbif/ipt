/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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
package org.gbif.provider.webapp.action.manage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.opensymphony.xwork2.Preparable;

import static org.gbif.provider.model.voc.ServiceType.DWC_ARCHIVE;
import static org.gbif.provider.model.voc.ServiceType.EML;
import static org.gbif.provider.model.voc.ServiceType.TAPIR;
import static org.gbif.provider.model.voc.ServiceType.TCS_RDF;
import static org.gbif.provider.model.voc.ServiceType.WFS;
import static org.gbif.provider.model.voc.ServiceType.WMS;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.eml.Agent;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.GeospatialCoverage;
import org.gbif.provider.model.eml.JGTICuratorialUnit;
import org.gbif.provider.model.eml.JGTICuratorialUnitType;
import org.gbif.provider.model.eml.KeywordSet;
import org.gbif.provider.model.eml.Method;
import org.gbif.provider.model.eml.MethodType;
import org.gbif.provider.model.eml.PhysicalData;
import org.gbif.provider.model.eml.Project;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonomicCoverage;
import org.gbif.provider.model.eml.TemporalCoverage;
import org.gbif.provider.model.eml.TemporalCoverageType;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.manage.EmlEditorAction.Helper.UrlProvider;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides action support for editing EML metadata forms.
 * 
 */
public class EmlEditorAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {

  static class Helper {

    /**
     * Interface for classes that provide a service URL given a
     * {@link ServiceType} and {@link Resource}.
     */
    static interface UrlProvider {
      String getUrl(ServiceType type, Resource resource);
    }

    static boolean checkLocalhostUrl(String url) {
      return !nullOrEmpty(url) && !url.contains("localhost")
          && !url.contains("127.0.0.1");
    }

    static String createResource(GbrdsResource resource, OrgCredentials creds,
        RegistryManager rm) {
      checkNotNull(resource, "Resource is null");
      checkNotNull(creds, "Organisation credentials are null");
      checkNotNull(rm, "Resource manager is null");
      checkArgument(validateResource(resource).isEmpty(), "Invalid resource");
      checkArgument(validateCreds(creds, rm), "Bad credentials");
      GbrdsResource r = null;
      try {
        r = rm.createResource(resource, creds).getResult();
      } catch (BadCredentialsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (r == null) {
        return null;
      }
      return r.getKey();
    }

    static Set<String> createServices(GbrdsResource r, Resource resource,
        UrlProvider up, RegistryManager registryManager) {
      checkNotNull(r, "GBRDS resource is null");
      checkNotNull(resource, "IPT resource is null");
      checkNotNull(up, "Url provider is null");
      checkNotNull(registryManager, "Registry manager is null");
      checkArgument(validateResource(r).isEmpty(), "Invalid resource");
      OrgCredentials creds = registryManager.getCreds(resource.getOrgUuid(),
          resource.getOrgPassword());
      checkArgument(validateCreds(creds, registryManager),
          "Invalid credentials");
      Set<String> serviceKeys = Sets.newHashSet();
      List<ServiceType> types = Lists.newArrayList(EML, DWC_ARCHIVE, TAPIR,
          WFS, WMS, TCS_RDF);
      GbrdsService.Builder service = GbrdsService.builder().resourceKey(
          r.getKey());
      for (ServiceType type : types) {
        switch (type) {
          case EML:
            service.accessPointURL(up.getUrl(EML, resource));
            service.type(EML.getCode());
            break;
          case DWC_ARCHIVE:
            service.accessPointURL(up.getUrl(DWC_ARCHIVE, resource));
            service.type(DWC_ARCHIVE.getCode());
            break;
          case TAPIR:
            service.accessPointURL(up.getUrl(TAPIR, resource));
            service.type(TAPIR.getCode());
            break;
          case WFS:
            service.accessPointURL(up.getUrl(WFS, resource));
            service.type(TAPIR.getCode());
            break;
          case WMS:
            service.accessPointURL(up.getUrl(WMS, resource));
            service.type(WMS.getCode());
            break;
          case TCS_RDF:
            service.accessPointURL(up.getUrl(TCS_RDF, resource));
            service.type(TCS_RDF.getCode());
            break;
        }
        GbrdsService createdService = null;
        try {
          createdService = registryManager.createService(service.build(), creds).getResult();
        } catch (BadCredentialsException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if (createdService != null) {
          serviceKeys.add(createdService.getKey());
        }
      }
      return serviceKeys;
    }

    static GbrdsResource.Builder getResourceBuilder(ResourceMetadata meta) {
      checkNotNull(meta, "Resource metadata is null");
      return GbrdsResource.builder().description(meta.getDescription()).primaryContactEmail(
          meta.getContactEmail()).primaryContactName(meta.getContactName()).homepageURL(
          meta.getLink()).name(meta.getTitle()).key(meta.getUddiID());
    }

    static ResourceMetadata getResourceMetadata(GbrdsResource resource) {
      checkNotNull(resource, "Organisation is nul");
      ResourceMetadata meta = new ResourceMetadata();
      meta.setDescription(resource.getDescription());
      meta.setContactEmail(resource.getPrimaryContactEmail());
      meta.setContactName(resource.getPrimaryContactName());
      meta.setLink(resource.getHomepageURL());
      meta.setTitle(resource.getName());
      meta.setUddiID(resource.getKey());
      return meta;
    }

    static boolean nullOrEmpty(String val) {
      return val == null || val.trim().length() == 0;
    }

    static boolean updateResource(GbrdsResource resource, OrgCredentials creds,
        RegistryManager rm) {
      checkNotNull(resource, "Resource is null");
      checkNotNull(creds, "Credentials are null");
      checkNotNull(rm, "Resource manager is null");
      checkArgument(validateResource(resource).isEmpty(), "Invalid resource");
      checkArgument(validateCreds(creds, rm), "Invalid credentials");
      try {
        return rm.updateResource(resource, creds).getResult();
      } catch (BadCredentialsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return false;
    }

    static boolean validateCreds(OrgCredentials creds, RegistryManager rm) {
      checkNotNull(rm, "Registry manager is null");
      if (creds == null) {
        return false;
      }
      return rm.validateCreds(creds).getResult();
    }

    static ImmutableSet<String> validateResource(GbrdsResource resource) {
      checkNotNull(resource, "Resource is null");
      ImmutableSet.Builder<String> errors = ImmutableSet.builder();
      if (nullOrEmpty(resource.getName())) {
        errors.add("Error: Resource name required");
      }
      if (nullOrEmpty(resource.getPrimaryContactType())) {
        errors.add("Error: Resource contact type required");
      }
      if (nullOrEmpty(resource.getPrimaryContactEmail())) {
        errors.add("Error: Resource contact email required");
      }
      if (nullOrEmpty(resource.getOrganisationKey())) {
        errors.add("Error: Resource key required");
      }
      return errors.build();
    }
  }

  /**
   * Enumeration of method type.
   * 
   */
  private enum RequestMethod {
    ASSOCIATED_PARTIES, NO_OP, ORGANISATION, SAMPLING_METHODS, KEYWORD_SETS, GEOGRAPHIC_COVERAGES, TEMPORAL_COVERAGES, TAXONOMIC_COVERAGES, PROJECTS, CITATIONS, COLLECTIONS, PHYSICAL_DATA, RESOURCE_FORM, ADDITIONAL_METADATA;
  }
  private static class UrlProviderImpl implements UrlProvider {
    final AppConfig cfg;

    UrlProviderImpl(AppConfig cfg) {
      this.cfg = cfg;
    }

    public String getUrl(ServiceType type, Resource resource) {
      switch (type) {
        case EML:
          return cfg.getEmlUrl(resource.getGuid());
        case DWC_ARCHIVE:
          return cfg.getArchiveUrl(resource.getGuid());
        case TAPIR:
          return cfg.getTapirEndpoint(resource.getId());
        case WFS:
          return cfg.getWfsEndpoint(resource.getId());
        case WMS:
          return cfg.getWmsEndpoint(resource.getId());
        case TCS_RDF:
          return cfg.getArchiveTcsUrl(resource.getGuid());
        default:
          return null;
      }
    }
  }

  private static final long serialVersionUID = -843256914689939746L;

  /**
   * Returns the {@link RequestMethod} corresponding to the method parameter
   * value in the request. If the request doesn't have the method parameter or
   * if it is unrecognized, the NO_OP method is returned.
   */
  private static RequestMethod method(HttpServletRequest r) {
    Preconditions.checkNotNull(r);
    if (r.getParameterValues("method") == null) {
      return RequestMethod.NO_OP;
    }
    String method = r.getParameterValues("method")[0];
    if (method.trim().equalsIgnoreCase("associatedParties")) {
      return RequestMethod.ASSOCIATED_PARTIES;
    } else if (method.trim().equalsIgnoreCase("organisation")) {
      return RequestMethod.ORGANISATION;
    } else if (method.trim().equalsIgnoreCase("samplingMethods")) {
      return RequestMethod.SAMPLING_METHODS;
    } else if (method.trim().equalsIgnoreCase("keywordSets")) {
      return RequestMethod.KEYWORD_SETS;
    } else if (method.trim().equalsIgnoreCase("taxonomicCoverages")) {
      return RequestMethod.GEOGRAPHIC_COVERAGES;
    } else if (method.trim().equalsIgnoreCase("geographicCoverages")) {
      return RequestMethod.TAXONOMIC_COVERAGES;
    } else if (method.trim().equalsIgnoreCase("temporalCoverages")) {
      return RequestMethod.TEMPORAL_COVERAGES;
    } else if (method.trim().equalsIgnoreCase("projects")) {
      return RequestMethod.PROJECTS;
    } else if (method.trim().equalsIgnoreCase("resourceForm")) {
      return RequestMethod.RESOURCE_FORM;
    } else if (method.trim().equalsIgnoreCase("citations")) {
      return RequestMethod.CITATIONS;
    } else if (method.trim().equalsIgnoreCase("collections")) {
      return RequestMethod.COLLECTIONS;
    } else if (method.trim().equalsIgnoreCase("physicalData")) {
      return RequestMethod.PHYSICAL_DATA;
    } else if (method.trim().equalsIgnoreCase("additionalMetaData")) {
      return RequestMethod.ADDITIONAL_METADATA;
    }
    return RequestMethod.NO_OP;
  }

  @Autowired
  private RegistryManager registry;

  protected String next;

  protected String nextPage;

  @Autowired
  private EmlManager emlManager;
  @Autowired
  private ThesaurusManager thesaurusManager;
  private Eml eml;
  private HttpServletRequest request;
  private boolean isSubmittedAssoParties;

  private List<Agent> submittedAssociatedParties = Lists.newArrayList();

  private static List<Agent> deletedAgents = Lists.newArrayList();

  private Map<String, String> agentRoleMap;
  private Map<String, String> temporalCoverageTypeMap;
  private Map<String, String> curatorialUnitTypeMap;

  private Map<String, String> methodTypeMap;

  private List<Rank> taxonHigherRankList = Rank.DARWIN_CORE_HIGHER_RANKS;

  private String method;

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public Map<String, String> getAgentRoleMap() {
    return agentRoleMap;
  }

  public AppConfig getConfig() {
    return this.cfg;
  }

  public String getCountryVocUri() {
    return Vocabulary.Country.uri;
  }

  public Map<String, String> getCuratorialUnitTypeMap() {
    return curatorialUnitTypeMap;
  }

  public Eml getEml() {
    return eml;
  }

  public String getKeywords() {
    // String keywords = "";
    // for (String k : eml.getKeywords()) {
    // if (k != null) {
    // keywords += k + ", ";
    // }
    // }
    // return keywords.substring(0, keywords.lastIndexOf(","));
    // TODO
    return null;
  }

  public String getLanguageVocUri() {
    return Vocabulary.Language.uri;
  }

  public String getMethod() {
    return method;
  }

  public Map<String, String> getMethodTypeMap() {
    return methodTypeMap;
  }

  public String getNext() {
    return next;
  }

  public String getNextPage() {
    return nextPage;
  }

  public String getRankVocUri() {
    return Rank.URI;
  }

  public String getRegistryNodeUrl() {
    return AppConfig.getRegistryNodeUrl();
  }

  public String getRegistryOrgTitle() {
    return cfg.getIptOrgMetadata().getTitle();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  public List getRoles() {
    return Arrays.asList(Role.values());
  }

  public List<Rank> getTaxonHigherRankList() {
    return taxonHigherRankList;
  }

  public Map<String, String> getTemporalCoverageTypeMap() {
    return temporalCoverageTypeMap;
  }

  @Override
  public void prepare() {
    super.prepare();
    agentRoleMap = translateI18nMap(new HashMap<String, String>(
        Role.htmlSelectMap), true);
    temporalCoverageTypeMap = translateI18nMap(new HashMap<String, String>(
        TemporalCoverageType.htmlSelectMap), true);
    curatorialUnitTypeMap = translateI18nMap(new HashMap<String, String>(
        JGTICuratorialUnitType.htmlSelectMap), true);
    methodTypeMap = translateI18nMap(new HashMap<String, String>(
        MethodType.htmlSelectMap), true);
    switch (method(request)) {
      case ORGANISATION:
        boolean problems = false;
        // Notifies the user if the IPT base URL contains localhost:
        if (!Helper.checkLocalhostUrl(cfg.getBaseUrl())) {
          saveMessage("Warning: Cannot create GBRDS resource because IPT base URL contains localhost");
          problems = true;
        }
        // Notifies the user if the Geoserver URL contains localhost:
        if (!Helper.checkLocalhostUrl(cfg.getGeoserverUrl())) {
          saveMessage("Warning: Cannot create GBRDS resource because Geoserver URL contains localhost");
          problems = true;
        }
        // Notifies user if the resource organisation creds are invalid:
        String orgKey = resource.getOrgUuid();
        if (registry.orgExists(orgKey)) {
          String pass = resource.getOrgPassword();
          if (registry.getCreds(orgKey, pass) == null) {
            saveMessage("Warning: Cannot create GBRDS resource because the GBRDS organisation credentials are invalid");
            problems = true;
          }
        }
        // Notifies user if the resource already exists:
        String resourceKey = resource.getMeta().getUddiID();
        if (registry.resourceExists(resourceKey)) {
          saveMessage("GBRDS resource is registered for this resource: "
              + resourceKey);
        } else {
          resourceKey = null;
        }

        // Break if there are problems:
        if (problems) {
          break;
        }

        // Figures out credentials to use: Those from the resource or those from
        // the IPT organisation. If neither are valid, a warning is surfaced to
        // the UI:
        String key = resource.getOrgUuid();
        String pass = resource.getOrgPassword();
        OrgCredentials creds = registry.getCreds(key, pass);
        if (creds == null) {
          // Try creds from IPT organisation:
          creds = registry.getCreds(cfg.getOrg().getUddiID(),
              cfg.getOrgPassword());
          if (creds == null) {
            saveMessage("Warning: Cannot create GBRDS resource because a GBRDS organisation is not associated with this IPT instance");
            break;
          }
        }

        resource.setOrgPassword(creds.getPassword());
        resource.setOrgUuid(creds.getKey());

        GbrdsResource gr = Helper.getResourceBuilder(resource.getMeta()).organisationKey(
            resource.getOrgUuid()).primaryContactType(
            ContactType.technical.name()).organisationKey(creds.getKey()).build();

        // Creates new GBRDS resource:
        if (resourceKey == null) {
          resourceKey = Helper.createResource(gr, creds, registry);
          if (resourceKey == null) {
            saveMessage("Warning: Unable to create GBRDS resource");
            break;
          }
          resource.getMeta().setUddiID(resourceKey);
          // Creates new GBRDS services:
          gr = Helper.getResourceBuilder(resource.getMeta()).key(resourceKey).organisationKey(
              resource.getOrgUuid()).primaryContactType(
              ContactType.technical.name()).organisationKey(creds.getKey()).build();
          UrlProvider up = new UrlProviderImpl(cfg);
          Set<String> serviceKeys = Helper.createServices(gr, resource, up,
              registry);
          if (!serviceKeys.isEmpty()) {
            saveMessage("Successfully created GBRDS services: " + serviceKeys);
          }
        } else { // Updates the existing GBRDS resource:
          gr = Helper.getResourceBuilder(resource.getMeta()).key(resourceKey).organisationKey(
              resource.getOrgUuid()).primaryContactType(
              ContactType.technical.name()).organisationKey(creds.getKey()).build();
          if (Helper.updateResource(gr, creds, registry)) {
            saveMessage("GBRDS resource updated successfully");
          } else {
            saveMessage("Warning: Resource not updated in GBRDS");
          }
        }

        // Save changes to IPT resource:
        resource.setMeta(Helper.getResourceMetadata(gr));
        resource.setDirty();
        resourceManager.save(resource);

        // Nothing else to do here unless the Organisation form has elements
        // whose values are destined for eml.

        break;
      case ASSOCIATED_PARTIES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero agents.
          eml = emlManager.deserialize(resource);
          eml.getAssociatedParties().clear();
        } else {
          // eml was populated via Struts.
          List<Agent> agents = eml.getAssociatedParties();
          eml = emlManager.deserialize(resource);
          eml.setAssociatedParties(agents);
        }
        break;
      case GEOGRAPHIC_COVERAGES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // geographic coverages.
          eml = emlManager.deserialize(resource);
          eml.getGeospatialCoverages().clear();
          // This should go away when refactored for one geocoverage
          eml.setGeographicCoverage(null);
        } else {
          // eml was populated via Struts.
          List<GeospatialCoverage> coverages = eml.getGeospatialCoverages();
          GeospatialCoverage singlecoverage = eml.getGeographicCoverage();
          eml = emlManager.deserialize(resource);
          eml.setGeospatialCoverages(coverages);
          // This should go away when refactored for one geocoverage
          eml.setGeographicCoverage(singlecoverage);
        }
        break;
      case TAXONOMIC_COVERAGES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // taxonomic coverages.
          eml = emlManager.deserialize(resource);
          eml.getTaxonomicCoverages().clear();
        } else {
          // eml was populated via Struts.
          List<TaxonomicCoverage> coverages = eml.getTaxonomicCoverages();
          eml = emlManager.deserialize(resource);
          eml.setTaxonomicCoverages(coverages);
        }
        break;
      case TEMPORAL_COVERAGES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // temporal coverages.
          eml = emlManager.deserialize(resource);
          eml.getTemporalCoverages().clear();
        } else {
          // eml was populated via Struts.
          List<TemporalCoverage> coverages = eml.getTemporalCoverages();
          eml = emlManager.deserialize(resource);
          eml.setTemporalCoverages(coverages);
        }
        break;
      case PROJECTS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // projects.
          eml = emlManager.deserialize(resource);
          // Clear out the project and make way for a new one.
          Project project = new Project();
          eml.setProject(project);
        } else {
          // eml was populated via Struts.
          Project project = eml.getProject();
          eml = emlManager.deserialize(resource);
          eml.setProject(project);
        }
        break;
      case SAMPLING_METHODS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // sampling methods.
          eml = emlManager.deserialize(resource);
          eml.getSamplingMethods().clear();
        } else {
          // eml was populated via Struts.
          List<Method> methods = eml.getSamplingMethods();
          eml = emlManager.deserialize(resource);
          eml.setSamplingMethods(methods);
        }
        break;
      case CITATIONS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // citations.
          eml = emlManager.deserialize(resource);
          eml.getBibliographicCitations().clear();
          eml.setCitation("");
        } else {
          // eml was populated via Struts.
          List<String> citations = eml.getBibliographicCitations();
          String citation = eml.getCitation();

          eml = emlManager.deserialize(resource);

          eml.setBibliographicCitations(citations);
          eml.setCitation(citation);
        }
        break;
      case COLLECTIONS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // CuratorialUnits.
          eml = emlManager.deserialize(resource);

          eml.setCollectionName("");
          eml.setCollectionId("");
          eml.setParentCollectionId("");
          eml.getJgtiCuratorialUnits().clear();
        } else {
          // eml was populated via Struts.
          String collectionName = eml.getCollectionName();
          String collectionId = eml.getCollectionId();
          String parentCollectionId = eml.getParentCollectionId();
          List<JGTICuratorialUnit> curatorialUnits = eml.getJgtiCuratorialUnits();

          eml = emlManager.deserialize(resource);

          eml.setCollectionName(collectionName);
          eml.setCollectionId(collectionId);
          eml.setParentCollectionId(parentCollectionId);
          eml.setJgtiCuratorialUnits(curatorialUnits);
        }
        break;
      case PHYSICAL_DATA:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // physicalData.
          eml = emlManager.deserialize(resource);
          eml.getPhysicalData().clear();
        } else {
          // eml was populated via Struts.
          List<PhysicalData> physicalData = eml.getPhysicalData();
          eml = emlManager.deserialize(resource);
          eml.setPhysicalData(physicalData);
        }
        break;
      case KEYWORD_SETS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // keyword sets.
          eml = emlManager.deserialize(resource);
          eml.getKeywords().clear();
        } else {
          // eml was populated via Struts.
          List<KeywordSet> keywords = eml.getKeywords();
          eml = emlManager.deserialize(resource);
          eml.setKeywords(keywords);
        }
        break;
      case ADDITIONAL_METADATA:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // additionalMetadata.
          eml = emlManager.deserialize(resource);
          eml.setHierarchyLevel("");
          eml.setDistributionUrl("");
          eml.setPurpose("");
          eml.setIntellectualRights("");
          eml.setAdditionalInfo("");
          try {
            eml.setPubDate("");
          } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        } else {
          // eml was populated via Struts.
          Date pubDate = eml.getPubDate();
          String distributionUrl = eml.getDistributionUrl();
          String purpose = eml.getPurpose();
          String intellectualRights = eml.getIntellectualRights();
          String additionalInfo = eml.getAdditionalInfo();

          eml = emlManager.deserialize(resource);

          eml.setPubDate(pubDate);
          eml.setDistributionUrl(distributionUrl);
          eml.setPurpose(purpose);
          eml.setIntellectualRights(intellectualRights);
          eml.setAdditionalInfo(additionalInfo);
        }
        break;
    }
    if (eml == null && resource != null) {
      eml = emlManager.deserialize(resource);
    }
    if (eml.getResource() == null) {
      eml.setResource(resource);
    }
  }

  public String save() {
    if (resource == null) {
      return RESOURCE404;
    }
    if (cancel != null) {
      return CANCEL;
    }
    if (next == null) {
      return INPUT;
    }
    // if (resource.getOrgTitle() == null
    // || resource.getOrgTitle().trim().length() == 0) {
    // resource.setOrgPassword("");
    // resource.setOrgUuid("");
    // } else {
    // // validateResource();
    // }
    resource.setDirty();
    for (TemporalCoverage t : eml.getTemporalCoverages()) {
      t.correctDateOrder();
    }
    emlManager.serialize(eml);
    resourceManager.save(resource);
    return SUCCESS;
  }

  public void setEml(Eml eml) {
    this.eml = eml;
  }

  public void setKeywords(String keywordString) {
    List<String> keywords = new ArrayList<String>();
    for (String k : StringUtils.split(keywordString, ",")) {
      k = StringUtils.trimToNull(k);
      if (k != null) {
        keywords.add(k);
      }
    }
    // eml.setKeywords(keywords);
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }

  /**
   * @see ServletRequestAware#setServletRequest(HttpServletRequest)
   */
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

}