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

import org.gbif.provider.model.hibernate.IptNamingStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class BaseManagerJDBC extends JdbcDaoSupport {
  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  protected IptNamingStrategy namingStrategy;

  protected int executeCount(String sql) {
    Connection cn = getConnection();
    int count = 0;
    try {
      Statement st = cn.createStatement();
      ResultSet result = st.executeQuery(sql);
      // create extension records from JDBC resultset
      while (result.next()) {
        count = result.getInt(1);
      }
    } catch (SQLException e) {
      log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql),
          e);
    }
    return count;
  }

  protected List<Object> executeList(String sql) {
    Connection cn = getConnection();
    List<Object> result = new ArrayList<Object>();
    try {
      Statement st = cn.createStatement();
      ResultSet resultset = st.executeQuery(sql);
      // create extension records from JDBC resultset
      while (resultset.next()) {
        result.add(resultset.getObject(1));
      }
    } catch (SQLException e) {
      log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql),
          e);
    }
    return result;
  }

  protected List<String> executeListAsString(String sql) {
    Connection cn = getConnection();
    List<String> result = new ArrayList<String>();
    try {
      Statement st = cn.createStatement();
      ResultSet resultset = st.executeQuery(sql);
      // create extension records from JDBC resultset
      while (resultset.next()) {
        result.add(resultset.getString(1));
      }
    } catch (SQLException e) {
      log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql),
          e);
    }
    return result;
  }

  protected Map<String, Integer> executeMap(String sql) {
    Connection cn = getConnection();
    Map<String, Integer> map = new HashMap<String, Integer>();
    try {
      Statement st = cn.createStatement();
      ResultSet result = st.executeQuery(sql);
      // create extension records from JDBC resultset
      while (result.next()) {
        String key = result.getString(1);
        if (key != null) {
          map.put(key, result.getInt(2));
        }
      }
    } catch (SQLException e) {
      log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql),
          e);
    }
    return map;
  }
}
