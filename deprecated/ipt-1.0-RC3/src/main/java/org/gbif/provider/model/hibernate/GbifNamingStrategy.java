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
package org.gbif.provider.model.hibernate;

import org.gbif.provider.model.Extension;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Documentation.
 * 
 */
public class GbifNamingStrategy extends ImprovedNamingStrategy implements IptNamingStrategy {
  static final Pattern MULTI_UPPERCASE = Pattern.compile("([A-Z])([A-Z]+)");

  protected final Log log = LogFactory.getLog(getClass());

  public String extensionTableName(Extension ext) {
    if (ext != null) {
      // replace all whitespace
      String extensionName = StringUtils.deleteWhitespace(ext.getName()+"_"+Math.abs(ext.getNamespace().hashCode()));
      // use Hibernate NamingStrategy now for the rest...
      return "dwc_" + tableName(extensionName);
    } else {
      return null;
    }
  }

  @Override
  public String foreignKeyColumnName(String propertyName,
      String propertyEntityName, String propertyTableName,
      String referencedColumnName) {
    // log.debug(StringUtils.join(new String[] {propertyName,
    // propertyEntityName, propertyTableName, referencedColumnName}, " <|> "));

    String colFk;
    String suffix = "_" + referencedColumnName;
    if (referencedColumnName.equalsIgnoreCase("id")) {
      suffix = "_fk";
    }
    if (propertyName != null && propertyName.length() > 1) {
      colFk = propertyToColumnName(propertyName) + suffix;
    } else {
      colFk = propertyToColumnName(propertyTableName) + suffix;
    }
    // log.debug("foreignKeyColumnName: "+colFk);
    return colFk;
  }

  @Override
  public String logicalColumnName(String columnName, String propertyName) {
    return StringHelper.isNotEmpty(columnName) ? columnName : propertyName;
  }

  @Override
  public String propertyToColumnName(String propertyName) {
    propertyName = StringUtils.deleteWhitespace(propertyName);
    // transform multiple upper case characters into CamelCase
    Matcher m = MULTI_UPPERCASE.matcher(propertyName);
    while (m.find()) {
      // group 0 is the whole pattern matched,
      propertyName = propertyName.replaceAll(m.group(0), m.group(1)
          + m.group(2).toLowerCase());
    }
    // call original underscore replacement method
    String x = addUnderscores(propertyName);
    // log.debug(String.format("propertyToColumnName=%s :: <%s>", x,
    // propertyName));
    return x;
  }

  
  
  
  
  
	public static void main(String[] args) {
		GbifNamingStrategy strat = new GbifNamingStrategy();
		Extension ext = new Extension();
		ext.setName("Multimedia");
//		ext.setName("VernacularName");
		ext.setNamespace("http://rs.gbif.org/ipt/terms/1.0/");
		System.out.println(strat.extensionTableName(ext));
	}

}
