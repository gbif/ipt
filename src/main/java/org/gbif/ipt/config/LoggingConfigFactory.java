/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  @Override
  public String[] getSupportedTypes() {
    return SUFFIXES;
  }
}
