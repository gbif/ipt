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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.gbif.provider.model.Resource;
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
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
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
    ServletRequestAware {

  static class Helper {
    static boolean nullOrEmpty(String val) {
      return val == null || val.trim().length() == 0;
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

  private final List<Agent> submittedAssociatedParties = Lists.newArrayList();

  private static List<Agent> deletedAgents = Lists.newArrayList();

  private Map<String, String> agentRoleMap;
  private Map<String, String> temporalCoverageTypeMap;
  private Map<String, String> curatorialUnitTypeMap;

  private Map<String, String> methodTypeMap;

  private final List<Rank> taxonHigherRankList = Rank.DARWIN_CORE_HIGHER_RANKS;

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
        if (registry.isLocalhost(cfg.getBaseUrl())) {
          saveMessage(getText("config.ipt.warning.localhost"));
          problems = true;
        }

        // Notifies the user if the Geoserver URL contains localhost:
        if (registry.isLocalhost(cfg.getGeoserverUrl())) {
          saveMessage(getText("config.warning.geoserverLocalhost"));
          problems = true;
        }

        // Notifies user if the resource organisation creds are invalid:
        String orgKey = resource.getOrgUuid();
        if (registry.orgExists(orgKey)) {
          String pass = resource.getOrgPassword();
          if (registry.getCreds(orgKey, pass) == null) {
            saveMessage(getText("config.warning.geoserverCredentials"));
            problems = true;
          }
        }

        // Notifies user if the resource already exists:
        String resourceKey = resource.getMeta().getUddiID();
        if (registry.resourceExists(resourceKey)) {
          saveMessage(getText("config.resourceAlreadyRegistered") + " "
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
            saveMessage(getText("config.ipt.warning.noOrg"));
            break;
          }
        }

        // Sets resource organisation credentials:
        resource.setOrgPassword(creds.getPassword());
        resource.setOrgUuid(creds.getKey());

        // Builds the GBRDS resource to create:
        GbrdsResource gr = registry.getResourceBuilder(resource.getMeta()).organisationKey(
            resource.getOrgUuid()).primaryContactType(
            ContactType.technical.name()).organisationKey(creds.getKey()).build();

        if (resourceKey == null) {

          // Creates new GBRDS resource:
          CreateResourceResponse crr = null;
          try {
            crr = registry.createResource(gr, creds);
          } catch (BadCredentialsException e1) {
            saveMessage(getText("config.ipt.warning.resourceUpdateBadCredentials")
                + " " + creds);
            break;
          }
          int status = crr.getStatus();
          if (status != HttpStatus.SC_CREATED) {
            saveMessage(getText("config.org.warning.orgNotCreated") + " "
                + status);
            break;
          }

          // Verified the new key:
          resourceKey = crr.getResult().getKey();
          if (resourceKey == null) {
            saveMessage(getText("config.org.warning.resourceNoKey"));
            break;
          }
          resource.getMeta().setUddiID(resourceKey);
          saveMessage(getText("config.org.registered") + " " + resourceKey);

          // Creates new GBRDS services:
          gr = registry.getResourceBuilder(resource.getMeta()).key(resourceKey).organisationKey(
              resource.getOrgUuid()).primaryContactType(
              ContactType.technical.name()).organisationKey(creds.getKey()).build();
          Set<String> errors = registry.createResourceServices(gr, resource);
          if (!errors.isEmpty()) {
            for (String e : errors) {
              saveMessage(e);
            }
            break;
          } else {
            saveMessage(getText("config.servicesCreated"));
          }
        } else {

          // Updates the existing GBRDS resource:
          gr = registry.getResourceBuilder(resource.getMeta()).key(resourceKey).organisationKey(
              resource.getOrgUuid()).primaryContactType(
              ContactType.technical.name()).organisationKey(creds.getKey()).build();
          UpdateResourceResponse urr = null;
          try {
            urr = registry.updateResource(gr, creds);
          } catch (BadCredentialsException e) {
            saveMessage(getText("config.ipt.warning.resourceUpdateBadCredentials")
                + " " + creds);
            break;
          }
          int status = urr.getStatus();
          if (status != HttpStatus.SC_OK) {
            saveMessage(getText("config.ipt.warning.resourceNotUpdated") + " "
                + status);
            break;
          } else {
            saveMessage(getText("config.ipt.resourceUpdated"));
          }
        }

        // Save changes to IPT resource:
        resource.setMeta(registry.getMeta(gr));
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