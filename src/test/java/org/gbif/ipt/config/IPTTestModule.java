package org.gbif.ipt.config;

import javax.servlet.ServletContext;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This guice module provides objects only for true IPTs running in a servlet environment. The module is replaced by a
 * test module when using guice for unit tests.
 */
public class IPTTestModule extends IPTModule {

  /**
   * provide a test datadir based on classpath.
   */
  @Provides
  @Singleton
  ServletContext provideMockServlet() {
    return new MockServletContext();
  }
}
