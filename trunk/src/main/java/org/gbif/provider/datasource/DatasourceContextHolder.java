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