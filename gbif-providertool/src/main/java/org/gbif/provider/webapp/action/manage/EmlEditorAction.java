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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.Organisation;
import org.gbif.provider.model.eml.Agent;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.JGTICuratorialUnit;
import org.gbif.provider.model.eml.Method;
import org.gbif.provider.model.eml.PhysicalData;
import org.gbif.provider.model.eml.Project;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.eml.TemporalCoverage;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

/**
 * TODO: Documentation.
 * 
 */
public class EmlEditorAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {

  /**
   * Enumeration of method types.
   * 
   */
  private enum RequestMethod {
    ASSOCIATED_PARTIES,
    NO_OP,
    ORGANISATION,
    SAMPLING_METHODS,
    TEMPORAL_COVERAGES,
    PROJECTS,
    CITATIONS,
    COLLECTIONS,
    PHYSICAL_DATA,
    RESOURCE_FORM;
  }

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
    return cfg.getOrg().getTitle();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  public List getRoles() {
    return Arrays.asList(Role.values());
  }

  public String getTaxonomicClassification() {
    String coverage = "";
    // for (TaxonKeyword k : eml.getTaxonomicClassification()) {
    // if (k != null) {
    // coverage += k.getScientificName() + ", ";
    // }
    // }
    return coverage.substring(0, coverage.lastIndexOf(","));
  }

  @Override
  public void prepare() {
    super.prepare();
    agentRoleMap = translateI18nMap(new HashMap<String, String>(
        Role.htmlSelectMap), true);
    switch (method(request)) {
      case ASSOCIATED_PARTIES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero agents.
          eml = emlManager.load(resource);
          eml.getAssociatedParties().clear();
        } else {
          // eml was populated via Struts.
          List<Agent> agents = eml.getAssociatedParties();
          eml = emlManager.load(resource);
          eml.setAssociatedParties(agents);
        }
        break;
      case CITATIONS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // citations.
          eml = emlManager.load(resource);
          eml.getBibliographicCitations().clear();
          eml.setCitation("");
        } else {
          // eml was populated via Struts.
          List<String> citations = eml.getBibliographicCitations();
          String citation = eml.getCitation();
          eml = emlManager.load(resource);
          eml.setBibliographicCitations(citations);
          eml.setCitation(citation);
        }
        break;
      case COLLECTIONS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // CuratorialUnits.
          eml = emlManager.load(resource);
          eml.getJgtiCuratorialUnits().clear();
        } else {
          // eml was populated via Struts.
          List<JGTICuratorialUnit> curatorialUnits = eml.getJgtiCuratorialUnits();
          eml = emlManager.load(resource);
          eml.setJgtiCuratorialUnits(curatorialUnits);
        }
        break;
      case TEMPORAL_COVERAGES:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // temporal coverages.
          eml = emlManager.load(resource);
          eml.getTemporalCoverages().clear();
        } else {
          // eml was populated via Struts.
          List<TemporalCoverage> coverages = eml.getTemporalCoverages();
          eml = emlManager.load(resource);
          eml.setTemporalCoverages(coverages);
        }
        break;
      case SAMPLING_METHODS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // sampling methods.
          eml = emlManager.load(resource);
          eml.getSamplingMethods().clear();
        } else {
          // eml was populated via Struts.
          List<Method> methods = eml.getSamplingMethods();
          eml = emlManager.load(resource);
          eml.setSamplingMethods(methods);
        }
        break;
      case PHYSICAL_DATA:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // physicalData.
          eml = emlManager.load(resource);
          eml.getPhysicalData().clear();
        } else {
          // eml was populated via Struts.
          List<PhysicalData> physicalData = eml.getPhysicalData();
          eml = emlManager.load(resource);
          eml.setPhysicalData(physicalData);
        }
        break;
      case PROJECTS:
        if (eml == null && resource != null) {
          // eml equals null means that the form was submitted with zero
          // projects.
          eml = emlManager.load(resource);
        } else {
          // eml was populated via Struts.
          Project project = eml.getProject();
          eml = emlManager.load(resource);
          eml.setProject(project);
        }
        break;
    }
    if (eml == null && resource != null) {
      eml = emlManager.load(resource);
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
    if (resource.getOrgTitle() == null || resource.getOrgTitle().isEmpty()) {
      resource.setOrgPassword("");
      resource.setOrgUuid("");
    } else {
      validateResource();
    }
    resource.setDirty();
    emlManager.save(eml);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.struts2.interceptor.ServletRequestAware#setServletRequest(javax
   * .servlet.http.HttpServletRequest)
   */
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setTaxonomicClassification(String taxonomicCoverage) {
    List<TaxonKeyword> keywords = new ArrayList<TaxonKeyword>();
    for (String k : StringUtils.split(taxonomicCoverage, ",")) {
      k = StringUtils.trimToNull(k);
      if (k != null) {
        // keywords.add(TaxonKeyword.create(k, null, null));
      }
    }
    // eml.setTaxonomicClassification(keywords);
  }

  private void validateResource() {
    Organisation org = Organisation.builder().password(
        resource.getOrgPassword()).organisationKey(resource.getOrgUuid()).build();
    if (!registryManager.isOrganisationRegistered(org)) {
      saveMessage(getText("config.check.orgLogin"));
      resource.setOrgPassword(null);
    }
  }
}
