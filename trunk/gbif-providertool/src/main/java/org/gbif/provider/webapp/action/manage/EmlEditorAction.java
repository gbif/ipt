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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.opensymphony.xwork2.Preparable;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.gbif.provider.model.voc.ServiceType.DWC_ARCHIVE;
import static org.gbif.provider.model.voc.ServiceType.EML;
import static org.gbif.provider.model.voc.ServiceType.TAPIR;
import static org.gbif.provider.model.voc.ServiceType.TCS_RDF;
import static org.gbif.provider.model.voc.ServiceType.WFS;
import static org.gbif.provider.model.voc.ServiceType.WMS;

import org.apache.commons.httpclient.HttpStatus;
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
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.service.RegistryManager.RegistryException;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.Credentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides action support for editing EML metadata forms.
 * 
 */
public class EmlEditorAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {

  /**
   * Enumeration of method type.
   * 
   */
  private enum RequestMethod {
    ASSOCIATED_PARTIES, NO_OP, ORGANISATION, SAMPLING_METHODS, KEYWORD_SETS, GEOGRAPHIC_COVERAGES, TEMPORAL_COVERAGES, TAXONOMIC_COVERAGES, PROJECTS, CITATIONS, COLLECTIONS, PHYSICAL_DATA, RESOURCE_FORM, ADDITIONAL_METADATA;
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
  private RegistryManager registryManager;

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
        // resource.getMeta().setUddiID(resource.getOrgUuid());
        String key = createOrUpdateGbrdsResource(resource);
        if (key == null) {
          saveMessage("GBRDS Resource was not saved");
        } else {
          saveMessage("GBRDS Resource saved: " + key);
        }
        resource.getMeta().setUddiID(key);
        resource.setDirty();
        resourceManager.save(resource);
        // Nothing to do here unless the Organisation form has elements whose
        // values are destined for eml.
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
    if (resource.getOrgTitle() == null
        || resource.getOrgTitle().trim().length() == 0) {
      resource.setOrgPassword("");
      resource.setOrgUuid("");
    } else {
      validateResource();
    }
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

  /**
   * @param r void
   * @param resource
   */
  private void createGbrdsServices(GbrdsResource r, Resource resource) {
    String baseUrl = cfg.getBaseUrl();
    if (trimToNull(baseUrl) == null || baseUrl.contains("localhost")) {
      saveMessage("Unable to create GBRDS services because the IPT base URL contains localhost");
      return;
    }
    List<ServiceType> types = Lists.newArrayList(EML, DWC_ARCHIVE, TAPIR, WFS,
        WMS, TCS_RDF);
    GbrdsService.Builder service = GbrdsService.builder().resourceKey(
        r.getKey()).organisationKey(resource.getOrgUuid()).resourcePassword(
        resource.getOrgPassword());
    for (ServiceType type : types) {
      switch (type) {
        case EML:
          service.accessPointURL(cfg.getEmlUrl(resource.getGuid()));
          service.type(EML.getCode());
          break;
        case DWC_ARCHIVE:
          service.accessPointURL(cfg.getArchiveUrl(resource.getGuid()));
          service.type(DWC_ARCHIVE.getCode());
          break;
        case TAPIR:
          service.accessPointURL(cfg.getTapirEndpoint(resource.getId()));
          service.type(TAPIR.getCode());
          break;
        case WFS:
          service.accessPointURL(cfg.getWfsEndpoint(resource.getId()));
          service.type(TAPIR.getCode());
          break;
        case WMS:
          service.accessPointURL(cfg.getWmsEndpoint(resource.getId()));
          service.type(WMS.getCode());
          break;
        case TCS_RDF:
          service.accessPointURL(cfg.getArchiveTcsUrl(resource.getGuid()));
          service.type(TCS_RDF.getCode());
          break;
      }
      try {
        registryManager.createGbrdsService(service.build());
      } catch (RegistryException e) {
        e.printStackTrace();
        String msg = String.format("Unable to register service: %s (%s)",
            service.build().getAccessPointURL(), e.toString());
        saveMessage(msg);
      }
    }
  }

  /**
   * @param resource void
   */
  private String createOrUpdateGbrdsResource(Resource resource) {
    Credentials creds = getOrgCreds(resource);
    if (creds == null
        || !registryManager.validateGbifOrganisationCredentials(
            resource.getOrgUuid(), creds).getResult()) {
      saveMessage("Unable to verify GBRDS organisation credentials");
      return null;
    }
    String baseUrl = cfg.getBaseUrl();
    if (trimToNull(baseUrl) == null || baseUrl.contains("localhost")) {
      saveMessage("Unable to create GBRDS resource because the IPT base URL contains localhost");
      return null;
    }

    String key = resource.getMeta().getUddiID();
    GbrdsResource gs = null;
    try {
      // Updates an existing GBRDS Resource:
      if (trimToNull(key) != null) {
        gs = registryManager.readGbrdsResource(key).getResult();
        if (gs != null) {
          ResourceMetadata rm = resource.getMeta();
          creds = getOrgCreds(resource);
          GbrdsResource gr = registryManager.buildGbrdsResource(rm).organisationKey(
              creds.getId()).organisationPassword(creds.getPasswd()).build();
          UpdateResourceResponse response = registryManager.updateGbrdsResource(gr);
          if (response.getStatus() == HttpStatus.SC_OK) {
            saveMessage(getText("The GBIF Resource associated with this IPT instance has been updated in the GBRDS."));
          } else {
            saveMessage(getText("The GBIF Resource associated with this IPT instance could not be updated in the GBRDS."));
          }
          return key;
        }
      }
      // Creates a new GBRDS Resource:
      ResourceMetadata rm = resource.getMeta();
      GbrdsResource gm = registryManager.buildGbrdsResource(rm).organisationKey(
          creds.getId()).organisationPassword(creds.getPasswd()).build();
      CreateResourceResponse response = registryManager.createGbrdsResource(gm);
      if (response.getStatus() == HttpStatus.SC_CREATED) {
        GbrdsResource r = response.getResult();
        key = r.getKey();
        createGbrdsServices(r, resource);
        resource.getMeta().setUddiID(key);
      } else {
        saveMessage(String.format(
            "Unable to create a GBRDS Resource (%d status)",
            response.getStatus()));
        log.warn("Unable to create GBRDS resource: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return key;
  }

  private Credentials getOrgCreds(Resource resource) {
    Credentials creds = null;
    String orgKey = resource.getOrgUuid();
    String orgPassword = resource.getOrgPassword();
    if (trimToNull(orgKey) != null && trimToNull(orgPassword) != null) {
      creds = Credentials.with(orgKey, orgPassword);
    }
    return creds;
  }

  private boolean validateResource() {
    boolean isResourceValid = false;

    // Validates the GBIF Organisation credentials associated with this
    // resource:
    String orgKey = resource.getOrgUuid();
    String orgPassword = resource.getOrgPassword();
    if (trimToNull(orgKey) != null && trimToNull(orgPassword) != null) {
      Credentials creds = Credentials.with(orgKey, orgPassword);
      ValidateOrgCredentialsResponse response = registryManager.validateGbifOrganisationCredentials(
          orgKey, creds);
      if (!response.getResult()) {
        saveMessage(getText("config.check.orgLogin"));
        resource.setOrgPassword(null);
      } else {
        isResourceValid = true;
      }
    }

    return isResourceValid;
  }
}
