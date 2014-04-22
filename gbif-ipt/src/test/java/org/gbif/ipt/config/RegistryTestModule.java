package org.gbif.ipt.config;

import org.gbif.metrics.ws.client.guice.MetricsWsClientModule;
import org.gbif.registry.ws.client.guice.RegistryWsClientModule;
import org.gbif.ws.client.guice.AnonymousAuthModule;
import org.gbif.ws.client.guice.GbifApplicationAuthModule;

import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Utility to provide the different Guice configurations for:
 * <ol>
 * <li>The WS service client layer</li>
 * <li>The read-only WS service client layer</li>
 * </ol>
 * Everything is cached, and reused on subsequent calls.
 */
public class RegistryTestModule {

  private RegistryTestModule() {
  }

  // cache everything, for reuse in repeated calls (e.g. IDE test everything)
  private static Injector webserviceClient;
  private static Injector webserviceClientReadOnly;
  private static Properties properties;

  /**
   * Load the Properties needed to configure the webservice client from the registry.properties file.
   */
  @Singleton
  public static synchronized Properties properties() {
    if (properties == null) {
      Properties p = new Properties();
      try {
        p.load(RegistryTestModule.class.getResourceAsStream("/config/registry.properties"));
      } catch (IOException e) {
        throw Throwables.propagate(e);
      } finally {
        properties = p;
      }
    }
    return properties;
  }

  /**
   * @return An injector that is bound for the webservice client layer.
   */
  public static synchronized Injector webserviceClient() {
    if (webserviceClient == null) {
      // Create authentication module, and set principal name, equal to a GBIF User unique account name
      GbifApplicationAuthModule auth = new GbifApplicationAuthModule(properties());
      auth.setPrincipal("admin");
      webserviceClient = Guice.createInjector(new RegistryWsClientModule(properties()), auth);
    }
    return webserviceClient;
  }

  /**
   * @return An injector that is bound for the webservice client layer but read-only.
   */
  public static synchronized Injector webserviceClientReadOnly() {
    if (webserviceClientReadOnly == null) {
      // Anonymous authentication module used, webservice client will be read-only
      webserviceClientReadOnly =
        Guice.createInjector(new MetricsWsClientModule(properties()),
          new RegistryWsClientModule(properties()), new AnonymousAuthModule());
    }
    return webserviceClientReadOnly;
  }
}
