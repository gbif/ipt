package org.gbif.ipt.config;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.util.LoaderUtil;

@Plugin(name = "LoggingConfigFactory", category = "ConfigurationFactory")
@Order(5)
public class LoggingConfigFactory extends ConfigurationFactory {

  public static boolean useDebug = true;

  /**
   * Valid file extensions for XML files.
   */
  public static final String[] SUFFIXES = new String[] {".xml", "*"};

  /**
   * Return either the debug or production configuration.
   * @param loggerContext The LoggerContext
   * @param configFile The ConfigurationSource
   * @return The Configuration.
   */
  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configFile) {

    String configName = useDebug ? "log4j2.xml" : "log4j2-production.xml";
    final ClassLoader loader = LoaderUtil.getThreadContextClassLoader();

    final ConfigurationSource source = ConfigurationSource.fromResource(configName, loader);

    return new LoggingConfiguration(loggerContext, source);
  }

  /**
   * Returns the file suffixes for XML files.
   * @return An array of File extensions.
   */
  public String[] getSupportedTypes() {
    return SUFFIXES;
  }
}
