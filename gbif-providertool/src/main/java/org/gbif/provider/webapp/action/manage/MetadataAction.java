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

import com.opensymphony.xwork2.Preparable;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.LabelValue;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Organisation;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.EmlFactory;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.model.voc.PublicationStatusForDisplay;
import org.gbif.provider.model.voc.ResourceDisplay;
import org.gbif.provider.model.voc.ResourceType;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ArchiveUtil;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.util.ArchiveUtil.ArchiveRequest;
import org.gbif.provider.util.ArchiveUtil.ArchiveResponse;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

/**
 * TODO: Documentation.
 * 
 */
public class MetadataAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {

  private static final long serialVersionUID = -4560571281629028337L;

  public List<ResourceDisplay> resourcesForDisplay = new ArrayList<ResourceDisplay>();

  private static final String OTHER = "other";

  private String next;

  private String nextPage;

  protected HttpServletRequest request;
  @Autowired
  private EmlManager emlManager;

  private Eml eml;

  @Autowired
  private ResourceArchiveManager resourceArchiveService;

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
  private Map<String, String> publicationStatusMap;
  private String jdbcDriverClass;

  @Autowired
  private ArchiveUtil<OccurrenceResource> occResourceArchiveUtil;

  @Autowired
  private ArchiveUtil<ChecklistResource> checklistResourceArchiveUtil;

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
      // remove resource with appropriate manager
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

  public String getNext() {
    return next;
  }

  public String getNextPage() {
    return nextPage;
  }

  public String getOther() {
    return OTHER;
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

  public List<?> getResources() {
    return resources;
  }

  public List<ResourceDisplay> getResourcesForDisplay() {
    return resourcesForDisplay;
  }

  public Map<String, String> getResourceTypeMap() {
    return resourceTypeMap;
  }

  public String list() {
    resource = null;
    if (isAdminUser()) {
      resources = getResourceTypeMatchingManager().getAll();
    } else {
      resources = getResourceTypeMatchingManager().getPublishedResources();
    }
    for (Resource r : resources) {
      resourcesForDisplay.add(new ResourceDisplay(r));
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
    publicationStatusMap = translateI18nMap(new HashMap<String, String>(
        PublicationStatusForDisplay.htmlSelectMap), true);
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
      eml = emlManager.deserialize(resource);
      if (eml == null) {
        eml = new Eml();
        eml.setResource(resource);
      }
      // Some properties in resource are the same as what's required in eml, so
      // we copy them over here:
      eml.setTitle(resource.getTitle());
      eml.setAlternateIdentifier(resource.getGuid());
      eml.setLogoUrl(cfg.getResourceLogoUrl(resource.getId()));
      // emlManager.save(eml);
    }

    // if (resource != null) {
    // resource = resourceManager.save(resource);
    // eml = emlManager.deserialize(resource);
    // if (eml == null) {
    // eml = new Eml();
    // }
    // eml.setResource(resource);
    // // Some properties in resource are the same as what's required in eml, so
    // // we copy them over here:
    // eml.setTitle(resource.getTitle());
    // eml.setAlternateIdentifier(resource.getGuid());
    // String contactName = resource.getContactName();
    // if (contactName != null) {
    // List<String> name = Lists.newArrayList(Splitter.on(
    // CharMatcher.WHITESPACE).trimResults().omitEmptyStrings().split(
    // contactName));
    // eml.getResourceCreator().setFirstName(name.get(0));
    // eml.getResourceCreator().setLastName(name.size() > 1 ? name.get(1) : "");
    // }
    // eml.setAbstract(resource.getDescription());
    //
    // // TODO: Causing problems?
    // emlManager.serialize(eml);
    // // emlManager.save(eml);
    // }
  }

  public String publish() {
    log.info("Publish method called");
    // publish only when POSTed, not with ordinary GET
    if (resource == null) {
      resource = getResourceTypeMatchingManager().get(resourceId);
    }
    if (request.getMethod().equalsIgnoreCase("post")) {
      if (eml == null && resource != null) {
        eml = emlManager.deserialize(resource);
      }
      try {
        emlManager.toXmlFile(eml);
        saveMessage("Created new eml.xml file: "
            + cfg.getEmlFile(resource.getId()));
      } catch (IOException e) {
        saveMessage("Unable to create new eml.xml file");
      }
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
    resource.updateWithMetadata(eml);
    resource = resourceManager.save(resource);

    eml.setResource(resource);
    // eml.setTitle(resource.getTitle());
    // eml.setAlternateIdentifier(resource.getGuid());
    // String contactName = resource.getContactName();
    // if (contactName != null) {
    // List<String> name =
    // Lists.newArrayList(Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings().split(
    // contactName));
    // eml.getResourceCreator().setFirstName(name.get(0));
    // eml.getResourceCreator().setLastName(name.size() > 1 ? name.get(1) : "");
    // }
    // eml.setAbstract(resource.getDescription());

    // validateEml();
    emlManager.serialize(eml);

    String key = (isNew) ? "resource.added" : "resource.updated";
    saveMessage(getText(key));
    if (isNew) {
      updateRecentResources();
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

  public void setNext(String next) {
    this.next = next;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }

  public void setResourcesForDisplay(List<ResourceDisplay> resourcesForDisplay) {
    this.resourcesForDisplay = resourcesForDisplay;
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

  public String upload() throws Exception {
    if (resource.getId() == null) {
      resourceManager.save(resource);
    }

    // Receives the uploaded file and saves it to disk:
    File targetFile = cfg.getSourceFile(resource.getId(), fileFileName);
    log.debug(String.format("Uploading source file for resource %s to file %s",
        resource.getId(), targetFile.getAbsolutePath()));
    InputStream stream = new FileInputStream(file);
    OutputStream bos = new FileOutputStream(targetFile);
    int bytesRead;
    byte[] buffer = new byte[8192];
    while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
      bos.write(buffer, 0, bytesRead);
    }
    bos.close();
    stream.close();

    // OccurrenceResource r = (OccurrenceResource) resource;
    // occResourceManager.save(r);
    // resource.getExtensionMappingsMap().clear();
    resourceManager.save(resource);

    boolean success = false;
    if (resource instanceof OccurrenceResource) {
      ArchiveRequest<OccurrenceResource> req = ArchiveRequest.with(targetFile,
          (OccurrenceResource) resource);
      ArchiveResponse<OccurrenceResource> res = occResourceArchiveUtil.init(req).process();
      resource = res.getResource();
      occResourceManager.save((OccurrenceResource) resource);
      success = res.isSuccess();
      for (String msg : res.getMessages()) {
        saveMessage(msg);
      }
    } else if (resource instanceof ChecklistResource) {
      ArchiveRequest<ChecklistResource> req = ArchiveRequest.with(targetFile,
          (ChecklistResource) resource);
      ArchiveResponse<ChecklistResource> res = checklistResourceArchiveUtil.init(
          req).process();
      resource = res.getResource();
      checklistResourceManager.save((ChecklistResource) resource);
      success = res.isSuccess();
      for (String msg : res.getMessages()) {
        saveMessage(msg);
      }
    }
    eml = getEmlIfExists(targetFile.getParentFile());
    if (eml != null) {
      resource.setTitle(eml.getTitle());
      resource.setDescription(eml.getAbstract());
      eml.setResource(resource);
      emlManager.serialize(eml);
      saveMessage("Loaded existing metadata from the eml.xml file in the archive.");
    } else {
      saveMessage("No existing metadata found in the archive.");
    }

    if (success) {
      return SUCCESS;
    } else if (!success && eml != null) {
      return SUCCESS;
    } else {
      return ERROR;
    }
  }

  public void validateUpload() {
    if (getRequest().getMethod().equalsIgnoreCase("post")) {
      getFieldErrors().clear();
      if ("".equals(fileFileName) || file == null) {
        super.addFieldError("file", getText("errors.requiredField",
            new String[]{getText("uploadForm.file")}));
      } else if (file.length() > 104857600) {
        addActionError(getText("maxLengthExceeded"));
      }
    }
  }

  Eml getEmlIfExists(File location) throws IOException {
    Eml eml = null;
    String path = null;
    File directory = null;
    if (location.isFile()) {
      directory = location.getParentFile();
    } else {
      directory = location;
    }
    String[] list = directory.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.equalsIgnoreCase("eml.xml");
      }
    });
    if (list != null && list.length > 0) {
      try {
        path = String.format("%s/%s", directory.getPath(), "eml.xml");
        eml = EmlFactory.build(new FileInputStream(new File(path)));
      } catch (SAXException e) {
        throw new IOException(e.toString());
      } catch (FileNotFoundException e) {
        throw new IOException(e.toString());
      } catch (IOException e) {
        throw e;
      }
    }
    return eml;
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

  private void validateResource() {
    Organisation org = Organisation.builder().password(
        resource.getOrgPassword()).organisationKey(resource.getOrgUuid()).build();
    if (!registryManager.isOrganisationRegistered(org)) {
      saveMessage(getText("config.check.orgLogin"));
      resource.setOrgPassword(null);
    }
  }
}
