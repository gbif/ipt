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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Set up RollingFileAppenders, initially in the default directory, then in the data directory.
 */
public class LoggingConfiguration extends XmlConfiguration {
  public static String logDirectory = "";

  public LoggingConfiguration(final LoggerContext loggerContext, final ConfigurationSource configSource) {
    super(loggerContext, configSource);
  }

  @Override
  protected void doConfigure() {
    super.doConfigure();

    final Layout layout = PatternLayout.newBuilder().withPattern("%-5p %d{dd-MMM-yyyy HH:mm:ss} [%c] - %m%n").build();

    final CompositeTriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
      OnStartupTriggeringPolicy.createPolicy(1),
      SizeBasedTriggeringPolicy.createPolicy("10MB")
    );

    final Appender debugAppender = RollingFileAppender.newBuilder()
        .setName("LOGFILE")
        .setLayout(layout)
        .withFileName(logDirectory+"debug.log")
        .withFilePattern(logDirectory+"debug.log.%i")
        .withPolicy(policy)
        .withStrategy(DefaultRolloverStrategy.newBuilder().build())
        .build();
    debugAppender.start();
    addAppender(debugAppender);
    getRootLogger().addAppender(debugAppender, null, null);

    final Appender adminAppender = RollingFileAppender.newBuilder()
        .setName("ADMINFILE")
        .setLayout(layout)
        .withFileName(logDirectory+"admin.log")
        .withFilePattern(logDirectory+"admin.log.%i")
        .withPolicy(policy)
        .withStrategy(DefaultRolloverStrategy.newBuilder().build())
        .build();
    adminAppender.start();
    addAppender(adminAppender);
    getLogger("org.gbif").addAppender(adminAppender, Level.WARN, null);
  }
}
