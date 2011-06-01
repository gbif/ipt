/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.config;

import org.gbif.ipt.model.Source.SqlSource;

import org.apache.commons.lang.xwork.StringUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class JdbcSupport {
  public class JdbcInfo {

    protected final String name;
    protected final String title;
    protected final String driver;
    protected final String url;
    protected final LIMIT_TYPE limitType;

    protected JdbcInfo(String name, String title, String driver, String url, LIMIT_TYPE limitType) {
      super();
      this.name = name;
      this.title = title;
      this.driver = driver;
      this.url = url;
      this.limitType = limitType;
    }

    public String addLimit(String sql, int limit) {
      if (sql == null) {
        return null;
      }
      // replace select
      Matcher m = null;
      if (LIMIT_TYPE.LIMIT == limitType) {
        m = LIMIT.matcher(sql);
        // does LIMIT already exist?
        if (m.find()) {
          sql = m.replaceAll(" LIMIT " + limit);
        } else {
          // lets append it then
        	if(sql.endsWith(";")) {
        		sql = sql.substring(0, sql.length()- 1);
        	}
          sql += " LIMIT " + limit;
        }

      } else if (LIMIT_TYPE.TOP == limitType) {
        m = SELECT.matcher(sql);
        if (m.find()) {
          // does TOP already exist?
          Matcher m2 = TOP.matcher(sql);
          if (m2.find()) {
            sql = m2.replaceFirst(" TOP " + limit);
          } else {
            sql = m.replaceAll(" SELECT TOP " + limit + " ");
          }
        } else {
          // there MUST be a select...should we throw an error?
        }

      } else if (LIMIT_TYPE.ROWNUM == limitType) {
        m = WHERE.matcher(sql);
        if (m.find()) {
          // does rownum already exist?
          Matcher m2 = ROWNUM.matcher(sql);
          if (m2.find()) {
            sql = m2.replaceAll(" rownum <= " + limit + " ");
          } else {
            sql = m.replaceAll(" WHERE rownum <= " + limit + " AND ");
          }
        } else {
          // lets append it then
          // TODO: lookout for order or group bys
          sql += " WHERE rownum <= " + limit;
        }
      }

      return sql;
    }

    public void enableLargeResultSet(Statement stmnt) throws SQLException {
      // force resultsset streaming for MYSQL only
      if (this.driver.startsWith("com.mysql")) {
        // see http://benjchristensen.com/2008/05/27/mysql-jdbc-memory-usage-on-large-resultset/
        stmnt.setFetchSize(Integer.MIN_VALUE);
      } else {
        stmnt.setFetchSize(1000);
      }
    }

    private Pattern findClause(String clause, boolean requireWhitespace) {
      String white = " *";
      if (requireWhitespace) {
        white = " +";
      }
      return Pattern.compile(clause.replace(" ", white).replace("?", "(\\d*)"), Pattern.CASE_INSENSITIVE);
    }

    public String getDriver() {
      return driver;
    }

    public String getJdbcUrl(SqlSource source) {
      return this.url.replace("{host}", source.getHost()).replace("{database}", source.getDatabase());
    }

    public String getJdbcUrl(String host, String database) {
      return this.url.replace("{host}", host).replace("{database}", database);
    }

    public String getName() {
      return name;
    }

    public String getTitle() {
      return title;
    }

    public String getUrl() {
      return url;
    }

  };
  protected enum LIMIT_TYPE {
    LIMIT, TOP, ROWNUM
  }

  private static final Pattern SELECT = Pattern.compile("(^| )select ", Pattern.CASE_INSENSITIVE);
  private static final Pattern TOP = Pattern.compile(" top \\d+", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHERE = Pattern.compile(" where ", Pattern.CASE_INSENSITIVE);
  private static final Pattern ROWNUM = Pattern.compile(" rownum[ <>=]+\\d+", Pattern.CASE_INSENSITIVE);
  private static final Pattern LIMIT = Pattern.compile(" limit \\d+", Pattern.CASE_INSENSITIVE);

  public static final String CLASSPATH_PROPFILE = "jdbc.properties";

  private final Map<String, JdbcInfo> driver = new TreeMap<String, JdbcInfo>();

  public JdbcInfo get(String name) {
    return driver.get(name.toLowerCase());
  }

  public List<String> list() {
    List<String> driverNames = new ArrayList(driver.keySet());
    Collections.sort(driverNames);
    return driverNames;
  }

  public Map<String, String> optionMap() {
    Map<String, String> map = new TreeMap<String, String>();
    for (JdbcInfo j : options()) {
      map.put(j.getName(), j.getTitle());
    }
    return map;
  }

  /**
   * @return map of name to jdbc info suitable for html selects
   */
  public Collection<JdbcInfo> options() {
    return driver.values();
  }

  protected int setProperties(Properties props) {
    driver.clear();
    // get distinct list of driver names
    Set<String> names = new HashSet<String>();
    for (Enumeration propertyNames = props.propertyNames(); propertyNames.hasMoreElements();) {
      String name = StringUtils.substringBefore((String) propertyNames.nextElement(), ".");
      names.add(name);
    }
    // create a jdbc info object for each
    for (String name : names) {
      name = name.toLowerCase();
      LIMIT_TYPE lt = LIMIT_TYPE.valueOf(props.getProperty(name + ".limitType"));
      JdbcInfo info = new JdbcInfo(name, props.getProperty(name + ".title"), props.getProperty(name + ".driver"), props.getProperty(name + ".url"), lt);
      driver.put(name, info);
    }
    return driver.size();
  }

}
