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
	private static final Pattern SELECT = Pattern.compile("(^| )select ",Pattern.CASE_INSENSITIVE); 
	private static final Pattern WHERE = Pattern.compile(" where ",Pattern.CASE_INSENSITIVE); 
	private static final Pattern LIMIT = Pattern.compile(" limit \\d*",Pattern.CASE_INSENSITIVE); 
  public class JdbcInfo {
    protected final String name;
    protected final String title;
    protected final String driver;
    protected final String url;
    protected final String sqlSelect;
    protected final String sqlWhere;
    protected final String sqlLimit;

    protected JdbcInfo(String name, String title, String driver, String url, String sqlSelect, String sqlWhere, String sqlLimit) {
      super();
      this.name = name;
      this.title = title;
      this.driver = driver;
      this.url = url;
      this.sqlSelect=StringUtils.trimToNull(sqlSelect);
      this.sqlWhere=StringUtils.trimToNull(sqlWhere);
      this.sqlLimit=StringUtils.trimToNull(sqlLimit);
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
    private String prepClause(String sql, int limit){
        return " "+sql.replace("?", String.valueOf(limit))+" ";
    }
    private Pattern findClause(String clause, boolean requireWhitespace){
    	String white = " *";
    	if (requireWhitespace){
        	white = " +";
    	}
        return Pattern.compile(clause.replace(" ", white).replace("?", "(\\d*)"),Pattern.CASE_INSENSITIVE);
    }
    public String addLimit(String sql,int limit){
    	// replace select
    	if (sqlSelect!=null){
        	Matcher m = SELECT.matcher(sql);
        	if (m.find()) {
        		// does our new clause, e.g. TOP, already exist?
            	Matcher m2 = findClause(sqlSelect,true).matcher(sql);
            	if (m2.find()) {
            	    sql = m2.replaceFirst(prepClause(sqlSelect,limit));
            	}else{
            	    sql = m.replaceAll(" SELECT"+prepClause(sqlSelect,limit));
            	}
        	} else {
        	    // there MUST be a select...should we throw an error?
        	}
    	}
    	// replace where
    	if (sqlWhere!=null){
    		Matcher m = WHERE.matcher(sql);
        	String clause = " WHERE"+prepClause(sqlWhere,limit);
        	if (m.find()) {
        		// does our new clause, e.g. TOP, already exist?
            	Matcher m2 = findClause(sqlWhere,false).matcher(sql);
            	if (m2.find()) {
            	    sql = m2.replaceFirst(prepClause(sqlWhere,limit));
            	}else{
            	    sql = m.replaceAll(clause+"AND ");
            	}
        	} else {
        	    // lets append it then
        		sql+=clause;
        	}
    	}
    	// append limit
    	if (sqlLimit!=null){
    		Matcher m = LIMIT.matcher(sql);
        	String clause = prepClause(sqlLimit,limit);
        	if (m.find()) {
        	    sql = m.replaceAll(clause);
        	} else {
        	    // lets append it then
        		sql+=clause;
        	}
    	}
    	return sql;
    }
  }

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
      JdbcInfo info = new JdbcInfo(name, props.getProperty(name + ".title"), props.getProperty(name + ".driver"),
          props.getProperty(name + ".url"), props.getProperty(name + ".sql.select"), props.getProperty(name + ".sql.where"), props.getProperty(name + ".sql.limit"));
      driver.put(name, info);
    }
    return driver.size();
  }

}
