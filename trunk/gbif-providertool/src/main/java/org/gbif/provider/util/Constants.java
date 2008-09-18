/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Constant values used throughout the application.
 */
public class Constants {
    /**
     * The name of the Administrator role, as specified in web.xml
     */
    public static final String MANAGER_ROLE = "ROLE_MANAGER";
    public static final Long TEST_RESOURCE_ID = 2L;
    public static final Long TEST_USER_ID = 4L;
    public static final String TEST_BASE_DIR = "/tmp";
    public static final String RECENT_RESOURCES = "recentResources";
    public static final String DEFAULT_LOGO = "/images/resource-logo.gif";
    public static final String ENCODING = "UTF-8";    
    // 1999-07-10
    private static final DateFormat date_iso_format = new SimpleDateFormat("yyyy-MM-dd");
    // 2001-07-04T12:08:56.235-0700
    public static final DateFormat datetime_iso_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    public static synchronized DateFormat DATE_ISO_FORMAT(){
    	return date_iso_format;
    }
    public static synchronized DateFormat DATETIME_ISO_FORMAT(){
    	return datetime_iso_format;
    }    
}
