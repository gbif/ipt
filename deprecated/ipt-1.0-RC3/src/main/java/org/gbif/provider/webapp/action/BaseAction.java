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
package org.gbif.provider.webapp.action;

import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.util.AppConfig;

import org.apache.commons.lang.StringUtils;
import org.appfuse.Constants;
import org.appfuse.model.LabelValue;
import org.appfuse.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class BaseAction extends org.appfuse.webapp.action.BaseAction {
  public static final String OCCURRENCE = ExtensionType.Occurrence.alias;
  public static final String CHECKLIST = ExtensionType.Checklist.alias;
  public static final String METADATA = ExtensionType.Metadata.alias;
  public static final String RECORD404 = "record404";
  public static final String RESOURCE404 = "resource404";
  @Autowired
  protected AppConfig cfg;
  private List<String> supportedLocales = new ArrayList<String>();

  public AppConfig getCfg() {
    return cfg;
  }

  public User getCurrentUser() {
    final SecurityContext secureContext = SecurityContextHolder.getContext();
    // secure context will be null when running unit tests so leave userId as
    // null
    if (secureContext != null) {
      final Authentication auth = (SecurityContextHolder.getContext()).getAuthentication();
      if (auth.getPrincipal() instanceof UserDetails) {
        final User user = (User) auth.getPrincipal();
        return user;
      }
    }
    return null;
  }

  public String getLocaleLanguage() {
    return this.getLocale().getLanguage();
  }

  public List<String> getSupportedLocales() {
    return supportedLocales;
  }

  public boolean isAdminUser() {
    User user = getCurrentUser();
    for (LabelValue val : user.getRoleList()) {
      if (val.getValue().equalsIgnoreCase(Constants.ADMIN_ROLE)) {
        return true;
      }
    }
    return false;
  }

  public void setCancel(String cancel) {
    this.cancel = cancel;
  }

  @Override
  public void setDelete(String delete) {
    this.delete = delete;
  }

  public void setSupportedLocales(List<String> supportedLocales) {
    this.supportedLocales = supportedLocales;
  }

  protected List<String> splitMultiValueParameter(String value) {
    if (value == null) {
      return new ArrayList<String>();
    }
    String[] paras = StringUtils.split(value, ", ");
    return Arrays.asList(paras);
  }

  /**
   * @param map To get the i18n values for
   * @return i18n results not sorted in anyway
   */
  protected Map<String, String> translateI18nMap(Map<String, String> map) {
    return translateI18nMap(map, false);
  }

  /**
   * @param map to i18n'alise
   * @param sortByValues if true, then this will sort the results alphabetically
   *          on the i18n name (useful for drop downs...)
   * @return The map which may be sorted on the values
   */
  protected Map<String, String> translateI18nMap(Map<String, String> map,
      boolean sortByValues) {
    for (String key : map.keySet()) {
      String i18Key = map.get(key);
      map.put(key, getText(i18Key));
    }
    if (!sortByValues) {
      return map;
    } else {
      // build a list that we will then sort by the values
      List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>();
      list.addAll(map.entrySet());

      // Sort the list using an annonymous inner class implementing Comparator
      // for the compare method
      Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
        public int compare(Map.Entry<String, String> entry,
            Map.Entry<String, String> entry1) {
          return entry.getValue().compareTo(entry1.getValue());
        }
      });

      // Clear the map
      map.clear();
      map = new LinkedHashMap<String, String>();
      for (Map.Entry<String, String> entry : list) {
        map.put(entry.getKey(), entry.getValue());
      }

      return map;
    }
  }
}
