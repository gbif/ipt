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
package org.gbif.provider.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Constant values used throughout the application.
 */
public class Constants {
  public static final DateFormat datetime_iso_format = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  public static final Long TEST_OCC_RESOURCE_ID = 13L;
  public static final Long TEST_CHECKLIST_RESOURCE_ID = 10L;
  public static final Long TEST_USER_ID = 1L;
  public static final Long TEST_TAXON_ID = 666L;
  // hacky, but needed for resource managers to calculate stats via
  // setResourceStats
  public static final Long DARWIN_CORE_EXTENSION_ID = 1L;
  public static final String SCIENTIFIC_NAME_QUALNAME = "http://rs.tdwg.org/dwc/terms/ScientificName";
  public static final String RECENT_RESOURCES = "recentResources";
  public static final String DEFAULT_LOGO = "images/resource-logo.gif";
  public static final String ENCODING = "UTF-8";
  public static final int LOGO_SIZE = 68;
  // 1999-07-10
  private static final DateFormat date_iso_format = new SimpleDateFormat(
      "yyyy-MM-dd");

  // 2001-07-04T12:08:56.235-0700

  public static synchronized DateFormat dateIsoFormat() {
    return date_iso_format;
  }

  public static synchronized DateFormat dateTimeIsoFormat() {
    return datetime_iso_format;
  }

  public static String indexName(Class klas, String suffix) {
    return klas.getSimpleName() + suffix;
  }

  /**
   * The name of the ResourceBundle used in this application
   */
  public static final String BUNDLE_KEY = "ApplicationResources";

  /**
   * The request scope attribute for indicating a newly-registered user
   */
  public static final String REGISTERED = "registered";

  /**
   * The name of the manager role, as specified in web.xml
   */
  public static final String MANAGER_ROLE = "ROLE_MANAGER";

  /**
   * The name of the Administrator role, as specified in web.xml
   */
  public static final String ADMIN_ROLE = "ROLE_ADMIN";

  /**
   * The name of the User role, as specified in web.xml
   */
  public static final String USER_ROLE = "ROLE_USER";

}
