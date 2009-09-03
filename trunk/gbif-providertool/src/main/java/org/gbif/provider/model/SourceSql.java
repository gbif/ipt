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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class SourceSql extends SourceBase {
  private static Log log = LogFactory.getLog(SourceSql.class);
  private String sql;

  public SourceSql() {
    super();
  }

  public SourceSql(String name, String sql) {
    super();
    this.sql = sql;
  }

  @Lob
  public String getSql() {
    return sql;
  }

  @Override
  @Transient
  public boolean isValid() {
    if (resource != null && StringUtils.isNotBlank(sql)
        && sql.trim().length() > 10) {
      if (resource.hasDbConnection()) {
        return true;
      }
    }
    return false;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

}
