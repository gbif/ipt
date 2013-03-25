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

package org.gbif.provider.datasource;

import org.springframework.util.Assert;

/**
 * thread-local bound context that stores the current resourceId of a user session to be used for ExternalResourceRoutingDatasource.
 * resourceId could maybe also be stored in the User instance of the Acegi userdetails.
 * @author markus
 *
 */
public class DatasourceContextHolder {
   private static final ThreadLocal<Long> contextHolder = new ThreadLocal<Long>();
        
   public static void setResourceId(Long resourceId) {
      Assert.notNull(resourceId, "resourceId cannot be null");
      contextHolder.set(resourceId);
   }
   public static Long getResourceId() {
      return (Long) contextHolder.get();
   }
   public static void clearResourceId() {
      contextHolder.remove();
   }
}