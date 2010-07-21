package org.gbif.ipt.config;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.servlet.ServletContext;

/**
 * This guice module provides objects only for true IPTs running in a servlet environment. The module is replaced by a
 * test module when using guice for unit tests.
 * 
 * @author markus
 */
public class IPTTestModule extends IPTModule {

  /**
   * provide a test datadir based on classpath
   * 
   * @return
   */
  @Provides
  @Singleton
  ServletContext provideMockServlet() {
    return new MockServletContext();
  }
}
