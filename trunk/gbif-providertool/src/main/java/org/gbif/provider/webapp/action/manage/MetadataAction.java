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

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.LabelValue;
import org.gbif.provider.model.Organization;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Agent;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.model.voc.ResourceType;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

/**
 * TODO: Documentation.
 * 
 */
public class MetadataAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {
  private static final String OTHER = "other";
  private static final String EML_ASSOCIATED_PARTIES_NAMES = "eml.associatedParties.fname";
  private static final String EML_ASSOCIATED_PARTIES_ROLES = "eml.associatedParties.roleName";

  /**
   * @param req
   * @return List<Agent>
   */
  private static List<Agent> associatedPartiesFromRequest(HttpServletRequest req) {
    List<Agent> agents = Lists.newArrayList();
    if (req == null) {
      return agents;
    }
    List<String> names;
    if (req.getParameterValues(EML_ASSOCIATED_PARTIES_NAMES) == null) {
      if (req.getParameterValues(EML_ASSOCIATED_PARTIES_NAMES + "[]") == null) {
        return agents;
      } else {
        names = Lists.newArrayList(req.getParameterValues(EML_ASSOCIATED_PARTIES_NAMES
            + "[]"));
      }
    } else {
      names = Lists.newArrayList(req.getParameterValues(EML_ASSOCIATED_PARTIES_NAMES));
    }
    List<String> roles;
    if (req.getParameterValues(EML_ASSOCIATED_PARTIES_ROLES) == null) {
      if (req.getParameterValues(EML_ASSOCIATED_PARTIES_ROLES + "[]") == null) {
        return agents;
      } else {
        roles = Lists.newArrayList(req.getParameterValues(EML_ASSOCIATED_PARTIES_ROLES
            + "[]"));
      }
    } else {
      roles = Lists.newArrayList(req.getParameterValues(EML_ASSOCIATED_PARTIES_ROLES));
    }
    if (names.size() != roles.size()) {
      return agents;
    }
    Agent a;
    for (int i = 0; i < names.size(); i++) {
      a = new Agent();
      a.setFirstName(names.get(i));
      a.setRole(roles.get(i));
      agents.add(a);
    }
    return agents;
  }

  protected HttpServletRequest request;

  @Autowired
  private EmlManager emlManager;

  private Eml eml;

  @Autowired
  private RegistryManager registryManager;

  @Autowired
  protected ResourceFactory resourceFactory;

  protected List<? extends Resource> resources;

  // file/logo upload
  protected File file;
  protected String fileContentType;
  protected String fileFileName;
  private final Map<String, String> jdbcDriverClasses = new HashMap<String, String>() {
    {
      put("com.mysql.jdbc.Driver", "MySQL");
      put("org.postgresql.Driver", "Postgres");
      put("org.h2.Driver", "H2");
      put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "MS SQL Server");
      put("net.sourceforge.jtds.jdbc.Driver", "JTDS");
      put("oracle.jdbc.OracleDriver", "Oracle");
      put("sun.jdbc.odbc.JdbcOdbcDriver", "Generic ODBC");
      put(OTHER, "Other");
    }
  };
  private Map<String, String> resourceTypeMap;

  private Map<String, String> agentRoleMap;

  private String jdbcDriverClass;

  public String connection() {
    if (resource == null) {
      return RESOURCE404;
    }
    DataResource res = (DataResource) resource;
    if (StringUtils.trimToNull(res.getJdbcDriverClass()) != null
        && !jdbcDriverClasses.containsKey(res.getJdbcDriverClass())) {
      jdbcDriverClass = res.getJdbcDriverClass();
      res.setJdbcDriverClass(OTHER);
    }
    return SUCCESS;
  }

  public String delete() {
    if (resource == null) {
      return RESOURCE404;
    } else if (resourceType != null) {
      // remove resource with appropiate manager
      getResourceTypeMatchingManager().remove(resource.getId());
      log.debug("Resource deleted");
      saveMessage(getText("resource.deleted"));
      // update recently viewed resources in session
      Object previousQueue = session.get(Constants.RECENT_RESOURCES);
      if (previousQueue != null && previousQueue instanceof Queue) {
        Queue<LabelValue> queue = (Queue) previousQueue;
        LabelValue res = new LabelValue(resource.getTitle(),
            resourceId.toString());
        // remove entry from queue if it existed before
        queue.remove(res);
        // save back to session
        session.put(Constants.RECENT_RESOURCES, queue);
      }
    } else {
      saveMessage("Can't identify resource to be deleted");
    }
    return "delete";
  }

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

  public File getFile() {
    return file;
  }

  public String getFileContentType() {
    return fileContentType;
  }

  public String getFileFileName() {
    return fileFileName;
  }

  public String getJdbcDriverClass() {
    return jdbcDriverClass;
  }

  public Map<String, String> getJdbcDriverClasses() {
    return jdbcDriverClasses;
  }

  public String getLanguageVocUri() {
    return Vocabulary.Language.uri;
  }

  public String getOther() {
    return OTHER;
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

  public List<?> getResources() {
    return resources;
  }

  public Map<String, String> getResourceTypeMap() {
    return resourceTypeMap;
  }

  public String list() {
    resource = null;
    if (isAdminUser()) {
      resources = getResourceTypeMatchingManager().getAll();
    } else {
      resources = getResourceTypeMatchingManager().getResourcesByUser(
          getCurrentUser().getId());
    }
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    System.out.println(request.getParameterMap());
    resourceTypeMap = translateI18nMap(new HashMap<String, String>(
        ResourceType.htmlSelectMap), true);
    agentRoleMap = translateI18nMap(new HashMap<String, String>(
        Role.htmlSelectMap), true);
    if (resource == null && resourceType != null) {
      // create new empty resource
      if (resourceType.equalsIgnoreCase(OCCURRENCE)) {
        resource = resourceFactory.newOccurrenceResourceInstance();
        resourceTypeMap = translateI18nMap(new HashMap<String, String>(
            ResourceType.htmlSelectMap(ResourceType.SAMPLE_GROUP)), true);

      } else if (resourceType.equalsIgnoreCase(CHECKLIST)) {
        resource = resourceFactory.newChecklistResourceInstance();
        resourceTypeMap = translateI18nMap(new HashMap<String, String>(
            ResourceType.htmlSelectMap(ResourceType.TAXON_GROUP)), true);

      } else {
        resource = resourceFactory.newMetadataResourceInstance();
      }
    }
    if (resource != null) {
      eml = emlManager.load(resource);
      if (eml == null) {
        eml = new Eml();
      }
    }
    List<Agent> associatedParties = eml.getAssociatedParties();
    for (Agent a : associatedPartiesFromRequest(request)) {
      if (associatedParties.contains(a)) {
        continue;
      }
      eml.addAssociatedParty(a);
    }
  }

  public String publish() {
    // publish only when POSTed, not with ordinary GET
    if (request.getMethod().equalsIgnoreCase("post")) {
      Resource res = getResourceTypeMatchingManager().publish(resourceId);
      if (res == null) {
        return RESOURCE404;
      }
    }
    return SUCCESS;
  }

  public String publishAll() {
    list();
    for (Resource res : resources) {
      if (res.isDirty()) {
        getResourceTypeMatchingManager().publish(res.getId());
        saveMessage("Published " + res.getTitle());
      }
    }
    return SUCCESS;
  }

  public String republish() {
    list();
    int i = 0;
    for (Resource res : resources) {
      if (res.getStatus().equals(PublicationStatus.modified)) {
        i++;
        getResourceTypeMatchingManager().publish(res.getId());
      }
    }
    saveMessage("Republished " + i + " modified resources");
    return SUCCESS;
  }

  public String save() {
    if (resource == null) {
      return RESOURCE404;
    }
    if (cancel != null) {
      return "cancel";
    }
    if (delete != null) {
      return delete();
    }

    boolean isNew = (resource.getId() == null);
    validateResource();
    resource.setDirty();
    resource = resourceManager.save(resource);

    List<Agent> associatedParties = eml.getAssociatedParties();
    for (Agent a : associatedPartiesFromRequest(request)) {
      if (associatedParties.contains(a)) {
        continue;
      }
      eml.addAssociatedParty(a);
    }
    validateEml();
    emlManager.save(eml);

    String key = (isNew) ? "resource.added" : "resource.updated";
    saveMessage(getText(key));
    if (isNew) {
      updateRecentResouces();
    }
    // logo
    if (uploadLogo()) {
      saveMessage(getText("resource.logoUploaded"));
    }
    return resourceType;
  }

  public String saveConnection() {
    if (resource == null) {
      return RESOURCE404;
    }
    if (StringUtils.trimToNull(jdbcDriverClass) != null) {
      // advance manual driver entry found. Overrides regular drop down
      DataResource res = (DataResource) resource;
      res.setJdbcDriverClass(jdbcDriverClass);
    }
    save();
    testDbConnection();
    return SUCCESS;
  }

  public void setAgentRoleMap(Map<String, String> agentRoleMap) {
    this.agentRoleMap = agentRoleMap;
  }

  public void setEml(Eml eml) {
    this.eml = eml;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileContentType(String fileContentType) {
    this.fileContentType = fileContentType;
  }

  public void setFileFileName(String fileFileName) {
    this.fileFileName = fileFileName;
  }

  public void setJdbcDriverClass(String jdbcDriverClass) {
    this.jdbcDriverClass = jdbcDriverClass;
  }

  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public String unpublish() {
    // unpublish only when POSTed, not with ordinary GET
    if (request.getMethod().equalsIgnoreCase("post")) {
      getResourceTypeMatchingManager().unPublish(resourceId);
    }
    return SUCCESS;
  }

  private void testDbConnection() {
    if (resource != null) {
      DataResource res = (DataResource) resource;
      try {
        DataSource dsa = res.getDatasource();
        if (dsa != null) {
          Connection con = dsa.getConnection();
        }
      } catch (SQLException e) {
        String msg = "Could not establish db connection: " + e.getMessage();
        log.warn(msg, e);
        saveMessage(msg);
      }
    }
  }

  private void uploadData(File targetFile) throws IOException {
    // retrieve the file data
    InputStream stream = new FileInputStream(file);

    // write the file to the file specified
    OutputStream bos = new FileOutputStream(targetFile);
    int bytesRead;
    byte[] buffer = new byte[8192];

    while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
      bos.write(buffer, 0, bytesRead);
    }

    bos.close();
    stream.close();
  }

  private boolean uploadLogo() {
    if ("".equals(fileFileName) || file == null) {
      return false;
    }
    // final logo destination
    File logoFile = cfg.getResourceLogoFile(resource.getId());
    try {
      uploadData(logoFile);
      // do sth with the file
      ResizeImage.resizeImage(file, logoFile, Constants.LOGO_SIZE,
          Constants.LOGO_SIZE);
    } catch (Exception e) {
      log.error("Couldnt upload or resize logo", e);
      saveMessage(getText("logo.resizeError"));
      return false;
    }

    log.info(String.format("Logo %s uploaded and resized for resource %s",
        logoFile.getAbsolutePath(), resourceId));
    return true;
  }

  /**
   * 
   * void
   */
  private void validateEml() {

    List<Agent> agents = eml.getAssociatedParties();
    for (Agent a : eml.getAssociatedParties()) {
      if (a.getFirstName() == null || a.getFirstName().isEmpty()) {
        agents.remove(a);
      }
    }
    eml.setAssociatedParties(agents);
  }

  private void validateResource() {
    Organization org = Organization.builder().password(
        resource.getOrgPassword()).organizationKey(resource.getOrgUuid()).build();
    if (!registryManager.isOrganizationRegistered(org)) {
      saveMessage(getText("config.check.orgLogin"));
      resource.setOrgPassword(null);
    }
  }
}
