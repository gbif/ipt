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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionRecordManager;

import org.apache.commons.lang.StringUtils;
import org.h2.jdbc.JdbcSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true)
public class ExtensionRecordManagerJDBC extends BaseManagerJDBC implements
    ExtensionRecordManager {
  @Autowired
  private ExtensionManager extensionManager;
  @Autowired
  protected AnnotationManager annotationManager;

  public int count(Extension extension, Long resourceId) {
    String table = namingStrategy.extensionTableName(extension);
    String sql = String.format("select count(*) from %s where resource_fk=%s",
        table, resourceId);
    return executeCount(sql);
  }

  public int countDistinct(ExtensionProperty property, Long resourceId) {
    String table = namingStrategy.extensionTableName(property.getExtension());
    String column = namingStrategy.columnName(property.getName());
    String sql = String.format(
        "select count(distinct %s) from %s where resource_fk=%s", column,
        table, resourceId);
    return executeCount(sql);
  }

  public ExtendedRecord extendCoreRecord(DataResource resource,
      CoreRecord coreRecord) {
    ExtendedRecord extRec = new ExtendedRecord(coreRecord);
    for (ExtensionMapping view : resource.getExtensionMappings()) {
      // Extension extension, List<ExtensionProperty> properties, Set<Long>
      // coreIds, Long resourceId) {
      List<ExtensionRecord> extensionRecords = getExtensionRecords(
          view.getExtension(), view.getMappedProperties(),
          coreRecord.getCoreId(), resource.getId());
      for (ExtensionRecord erec : extensionRecords) {
        extRec.addExtensionRecord(erec);
      }
    }
    return extRec;
  }

  public List<ExtendedRecord> extendCoreRecords(DataResource resource,
      CoreRecord[] coreRecords) {
    LinkedHashMap<Long, ExtendedRecord> extendedRecords = new LinkedHashMap<Long, ExtendedRecord>();
    for (CoreRecord core : coreRecords) {
      extendedRecords.put(core.getCoreId(), new ExtendedRecord(core));
    }
    for (ExtensionMapping view : resource.getExtensionMappings()) {
      List<ExtensionRecord> extensionRecords = getExtensionRecords(
          view.getExtension(), view.getMappedProperties(),
          extendedRecords.keySet(), resource.getId());
      for (ExtensionRecord erec : extensionRecords) {
        extendedRecords.get(erec.getCoreId()).addExtensionRecord(erec);
      }
    }
    return new ArrayList<ExtendedRecord>(extendedRecords.values());
  }

  @Transactional(readOnly = false)
  public void insertExtensionRecord(DataResource resource, ExtensionRecord rec) {
    String table = namingStrategy.extensionTableName(rec.getExtension());
    String cols = "resource_fk, source_id";
    String vals = String.format("%s,'%s'", rec.getResourceId(),
        rec.getSourceId());
    for (ExtensionProperty p : rec) {
      if (rec.getPropertyValue(p) != null) {
        cols += String.format(",%s",
            namingStrategy.propertyToColumnName(p.getName()));
        String val = rec.getPropertyValue(p);
        if (val.length() > p.getColumnLength() && p.getColumnLength() > 0) {
          val = val.substring(0, p.getColumnLength());
          annotationManager.annotate(resource, rec.getSourceId(), null,
              AnnotationType.TrimmedData, null, String.format(
                  "Exceeding data for property %s [%s] cut off", p.getName(),
                  p.getColumnLength()));
        }
        vals += String.format(",'%s'", val);
      }
    }
    String sql = String.format("insert into %s (%s) VALUES (%s)", table, cols,
        vals);
    Connection cn = null;
    try {
      cn = getConnection();
      Statement st = cn.createStatement();
      st.execute(sql);
    } catch (JdbcSQLException e) {
      if (e.getErrorCode() == 90005) {
        System.out.println("YUPPIE YEAH!");
      } else {
        System.out.println("ERROR CODE:" + e.getErrorCode());
      }
    } catch (SQLException e) {
      if (rec.getExtension() == null) {
        log.error(String.format("Couldn't insert record for extension=NULL",
            rec.getExtension()), e);
      } else {
        log.error(String.format("Couldn't insert record for extension %s",
            rec.getExtension().getName()), e);
      }
    } finally {
      if (cn != null) {
        // try {
        // cn.close();
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
      }
    }
  }

  @Transactional(readOnly = false)
  public int removeAll(Extension extension, Long resourceId) {
    String table = namingStrategy.extensionTableName(extension);
    table = "darwin_core";
    String sql = String.format("delete from %s where resource_fk=%s", table,
        resourceId);
    Connection cn = null;
    int count = 0;
    try {
      cn = getConnection();
      Statement st = cn.createStatement();
      count = st.executeUpdate(sql);
      log.debug(String.format("Removed %s records for extension %s", count,
          extension.getName()));
    } catch (SQLException e) {
      log.error(String.format("Couldn't remove all records for extension %s",
          extension.getName()));
      e.printStackTrace();
    } finally {
      if (cn != null) {
        // try {
        // cn.close();
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
      }
    }
    return count;
  }

  @Transactional(readOnly = false)
  public int updateCoreIds(Extension extension, DataResource resource) {
    String table = namingStrategy.extensionTableName(extension);
    String coreTable = "darwin_core";
    if (resource instanceof ChecklistResource) {
      coreTable = "taxon";
    }
    String sql = String.format(
        "update %s e set e.coreid=(select c.id from %s c where c.resource_fk=e.resource_fk and c.source_id=e.source_id) where e.resource_fk=%s",
        table, coreTable, resource.getId());
    Connection cn = null;
    int count = 0;
    try {
      cn = getConnection();
      Statement st = cn.createStatement();
      count = st.executeUpdate(sql);
      log.debug(String.format(
          "Updated %s records with coreids for extension %s", count,
          extension.getName()));
    } catch (SQLException e) {
      log.error(String.format("Couldn't update coreids for extension %s",
          extension.getName()), e);
    } finally {
      if (cn != null) {
        // try {
        // cn.close();
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
      }
    }
    // TODO: annotate extension records with unknown source IDs
    // annotationManager.badExtensionRecord(resource, extension,
    // rec.getSourceId(), "Unkown source ID");
    return count;
  }

  private List<ExtensionRecord> getExtensionRecords(Extension extension,
      List<ExtensionProperty> properties, Long coreId, Long resourceId) {
    String table = namingStrategy.extensionTableName(extension);
    String sql = String.format(
        "select * from %s where coreid=%s and resource_fk=%s", table, coreId,
        resourceId);
    return queryExtensionRecords(resourceId, sql, properties);
  }

  private List<ExtensionRecord> getExtensionRecords(Extension extension,
      List<ExtensionProperty> properties, Set<Long> coreIds, Long resourceId) {
    String table = namingStrategy.extensionTableName(extension);
    String sql = String.format(
        "select * from %s where coreid in (%s) and resource_fk=%s", table,
        StringUtils.join(coreIds, ","), resourceId);
    return queryExtensionRecords(resourceId, sql, properties);
  }

  private List<ExtensionRecord> queryExtensionRecords(Long resourceId,
      String sql, List<ExtensionProperty> properties) {
    List<ExtensionRecord> records = new ArrayList<ExtensionRecord>();
    Connection cn = null;
    try {
      cn = getConnection();
      Statement st = cn.createStatement();
      ResultSet result = st.executeQuery(sql);
      // create extension records from JDBC resultset
      while (result.next()) {
        ExtensionRecord rec = new ExtensionRecord(resourceId,
            result.getLong("coreid"));
        for (ExtensionProperty p : properties) {
          String value = result.getString(namingStrategy.propertyToColumnName(p.getName()));
          if (value != null) {
            rec.setPropertyValue(p, value);
          }
        }
        if (!rec.isEmpty()) {
          records.add(rec);
        }
      }
    } catch (SQLException e) {
      log.error(String.format("Couldn't read extension records for sql: %s",
          sql), e);
    } finally {
      if (cn != null) {
        // try {
        // cn.close();
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
      }
    }
    return records;
  }
}
