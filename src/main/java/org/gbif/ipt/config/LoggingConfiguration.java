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
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

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
