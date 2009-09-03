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

import org.gbif.provider.util.Constants;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.MapKey;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;

/**
 * An abstract resource which contains data in addition to metadata
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class DataResource extends Resource {
  private static Log log = LogFactory.getLog(DataResource.class);
  private String jdbcDriverClass = "com.mysql.jdbc.Driver";
  private String jdbcUrl = "jdbc:mysql://localhost/YOUR_DATABASE";
  private String jdbcUser;
  private String jdbcPassword;
  private UploadEvent lastUpload;
  // extension mappings, not including the core mapping
  private Map<Long, ExtensionMapping> extensionMappings = new HashMap<Long, ExtensionMapping>();
  //
  // transient properties
  //
  private DataSource datasource;
  // counts by interpreted rank
  private int numSpecies;
  private int numGenera;
  private int numFamilies;
  private int numOrders;
  private int numClasses;
  private int numPhyla;
  private int numKingdoms;
  // other counts
  private int numTaxa;
  private int numTerminalTaxa;
  private int numAccepted;
  private int numSynonyms;

  public void addExtensionMapping(ExtensionMapping mapping) {
    mapping.setResource(this);
    this.extensionMappings.put(mapping.getExtension().getId(), mapping);
  }

  @Transient
  public List<String> getAdditionalIdentifiers() {
    List<String> ids = new ArrayList<String>();
    return ids;
  }

  @Transient
  public Set<ExtensionMapping> getAllMappings() {
    return new HashSet<ExtensionMapping>(extensionMappings.values());
  }

  @Transient
  public ExtensionMapping getCoreMapping() {
    return extensionMappings.get(Constants.DARWIN_CORE_EXTENSION_ID);
  }

  @Transient
  public DataSource getDatasource() {
    if (datasource == null) {
      try {
        this.udpateDatasource();
      } catch (SQLException e) {
        // DONT do nothing. Error is logged already
      }
    }
    return datasource;
  }

  @Transient
  public abstract String getDwcGuidPropertyName();

  @Transient
  public ExtensionMapping getExtensionMapping(Extension extension) {
    if (extension.equals(getCoreMapping().getExtension())) {
      return getCoreMapping();
    }
    return this.extensionMappings.get(extension.getId());
  }

  @Transient
  public List<ExtensionMapping> getExtensionMappings() {
    List<ExtensionMapping> exts = new ArrayList<ExtensionMapping>();
    for (ExtensionMapping m : extensionMappings.values()) {
      if (!m.isCore()) {
        exts.add(m);
      }
    }
    return exts;
  }

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "resource_fk", insertable = false, updatable = false)
  @MapKey(columns = @Column(name = "extension_fk"))
  public Map<Long, ExtensionMapping> getExtensionMappingsMap() {
    return extensionMappings;
  }

  @Column(length = 64)
  public String getJdbcDriverClass() {
    return jdbcDriverClass;
  }

  @Column(length = 64)
  public String getJdbcPassword() {
    return jdbcPassword;
  }

  @Column(length = 128)
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  @Column(length = 64)
  public String getJdbcUser() {
    return jdbcUser;
  }

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "last_upload_event_fk", nullable = true)
  public UploadEvent getLastUpload() {
    return lastUpload;
  }

  @Transient
  public Date getLastUploadDate() {
    if (lastUpload != null) {
      return lastUpload.getExecutionDate();
    }
    return null;
  }

  @Transient
  public String getLayerName() {
    return "gbif:resource" + getId();
  }

  public int getNumAccepted() {
    return numAccepted;
  }

  public int getNumClasses() {
    return numClasses;
  }

  public int getNumFamilies() {
    return numFamilies;
  }

  public int getNumGenera() {
    return numGenera;
  }

  public int getNumKingdoms() {
    return numKingdoms;
  }

  public int getNumOrders() {
    return numOrders;
  }

  public int getNumPhyla() {
    return numPhyla;
  }

  public int getNumSpecies() {
    return numSpecies;
  }

  public int getNumSynonyms() {
    return numSynonyms;
  }

  public int getNumTaxa() {
    return numTaxa;
  }

  public int getNumTerminalTaxa() {
    return numTerminalTaxa;
  }

  @Transient
  public int getRecTotal() {
    return getCoreMapping().getRecTotal();
  }

  @Transient
  public boolean hasData() {
    if (lastUpload != null && lastUpload.getRecordsUploaded() > 0) {
      return true;
    }
    return false;
  }

  @Transient
  public boolean hasDbConnection() {
    boolean hasDbConnection = false;
    try {
      DataSource dsa = getDatasource();
      if (dsa != null) {
        Connection con = dsa.getConnection();
        hasDbConnection = true;
      }
    } catch (SQLException e) {
      hasDbConnection = false;
    }
    return hasDbConnection;
  }

  @Transient
  public boolean hasMetadata() {
    boolean result = false;
    if (getTitle() != null && getTitle().trim().length() > 0) {
      result = true;
    }
    return result;
  }

  /**
   * Checks to see whether a resource has the minimal mappings to proceed with
   * an upload
   * 
   * @return
   */
  @Transient
  public boolean hasMinimalMapping() {
    boolean result = false;
    if (getCoreMapping().getPropertyMappings().size() > 0) {
      result = true;
    }
    return result;
  }

  @Transient
  @Override
  public boolean isDataResource() {
    return true;
  }

  public void removeExtensionMapping(ExtensionMapping mapping) {
    this.extensionMappings.remove(mapping.getExtension().getId());
  }

  /**
   * Reset all cached stats, log events etc, so that it looks as if the resource
   * was just created (doesnt change the created timestamp though)
   */
  public void resetStats() {
    numSpecies = 0;
    numGenera = 0;
    numFamilies = 0;
    numOrders = 0;
    numClasses = 0;
    numPhyla = 0;
    numKingdoms = 0;

    numTaxa = 0;
    numTerminalTaxa = 0;
    numAccepted = 0;
    numSynonyms = 0;

    lastUpload = null;
    setRecTotal(0);
  }

  public void setExtensionMappingsMap(
      Map<Long, ExtensionMapping> extensionMappings) {
    this.extensionMappings = extensionMappings;
  }

  public void setJdbcDriverClass(String jdbcDriverClass) {
    this.jdbcDriverClass = StringUtils.trimToNull(jdbcDriverClass);
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = StringUtils.trimToNull(jdbcUrl);
  }

  public void setJdbcUser(String jdbcUser) {
    this.jdbcUser = jdbcUser;
  }

  public void setLastUpload(UploadEvent lastUpload) {
    this.lastUpload = lastUpload;
  }

  public void setNumAccepted(int numAccepted) {
    this.numAccepted = numAccepted;
  }

  public void setNumClasses(int numClasses) {
    this.numClasses = numClasses;
  }

  public void setNumFamilies(int numFamilies) {
    this.numFamilies = numFamilies;
  }

  public void setNumGenera(int numGenera) {
    this.numGenera = numGenera;
  }

  public void setNumKingdoms(int numKingdoms) {
    this.numKingdoms = numKingdoms;
  }

  public void setNumOrders(int numOrders) {
    this.numOrders = numOrders;
  }

  public void setNumPhyla(int numPhyla) {
    this.numPhyla = numPhyla;
  }

  public void setNumSpecies(int numSpecies) {
    this.numSpecies = numSpecies;
  }

  public void setNumSynonyms(int numSynonyms) {
    this.numSynonyms = numSynonyms;
  }

  public void setNumTaxa(int numTaxa) {
    this.numTaxa = numTaxa;
  }

  public void setNumTerminalTaxa(int numTerminalTaxa) {
    this.numTerminalTaxa = numTerminalTaxa;
  }

  public void setRecTotal(int recTotal) {
    getCoreMapping().setRecTotal(recTotal);
  }

  public void udpateDatasource() throws SQLException {
    if (this.getJdbcUrl() != null && jdbcDriverClass != null) {
      try {
        Class.forName(this.jdbcDriverClass);
        Driver driver = DriverManager.getDriver(this.getJdbcUrl());
        datasource = new SimpleDriverDataSource(driver, this.getJdbcUrl(),
            this.getJdbcUser(), this.getJdbcPassword());
      } catch (java.lang.ClassNotFoundException e) {
        datasource = null;
        String msg = String.format(
            "Couldnt load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s. Error: %s",
            this.jdbcDriverClass, this.getJdbcUrl(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      } catch (Exception e) {
        datasource = null;
        String msg = String.format(
            "Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s. Error: %s",
            this.jdbcDriverClass, this.getJdbcUrl(), this.getJdbcUser(),
            e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      }
    } else {
      datasource = null;
    }
  }
}
