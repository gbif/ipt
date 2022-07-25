/*
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

import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDataPackage;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportingTask;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.PBEEncrypt;
import org.gbif.ipt.utils.PBEEncrypt.EncryptionException;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.PreemptiveAuthenticationInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * A guice module containing wiring used for both test and production.
 */
public class IPTModule extends AbstractModule {

  private static final Logger LOG = LogManager.getLogger(IPTModule.class);

  private static final String DATA_DIR_ENV_VAR = "IPT_DATA_DIR";
  // 30 second timeout; too long and users will refresh and not notice errors.
  protected static final int CONNECTION_TIMEOUT_MSEC = 30_000;
  protected static final int MAX_CONNECTIONS = 100;
  protected static final int MAX_PER_ROUTE = 10;

  @Override
  protected void configure() {
    // singletons
    bind(AppConfig.class);
    bind(InputStreamUtils.class).in(Scopes.SINGLETON);
    bind(SimpleTextProvider.class).in(Scopes.SINGLETON);
    bind(ExtensionFactory.class);
    bind(VocabularyFactory.class).in(Scopes.SINGLETON);

    // prototypes
    bind(ThesaurusHandlingRule.class).in(Scopes.NO_SCOPE);

    // assisted inject factories
    install(
      new FactoryModuleBuilder().implement(ReportingTask.class, GenerateDwca.class).build(GenerateDwcaFactory.class));
    install(
        new FactoryModuleBuilder().implement(ReportingTask.class, GenerateDataPackage.class).build(GenerateDataPackageFactory.class));

  }

  @Provides
  @Singleton
  public DataDir provideDataDir(ServletContext ctx) {
    DataDir dd;

    /*
     * Parameter from the servlet context, e.g.
     * <Context>
     *   <Parameter name="IPT_DATA_DIR" value="/var/lib/ipt2"/>
     * </Context>
     * typically in $CATALINA_BASE/conf/Catalina/localhost/ipt2.xml
     */
    String dataDirectoryLocationParam = ctx.getInitParameter(DATA_DIR_ENV_VAR);

    /*
     * System environment variable
     */
    String dataDirectoryLocationEnv = System.getenv(DATA_DIR_ENV_VAR);

    if (dataDirectoryLocationParam != null) {
      LOG.info("Using context parameter " + DATA_DIR_ENV_VAR +
        " for data directory location: " + dataDirectoryLocationParam);
      dd = DataDir.buildFromString(dataDirectoryLocationParam);
    } else if (dataDirectoryLocationEnv != null) {
      LOG.info("Using environment variable " + DATA_DIR_ENV_VAR +
        " for data directory location: " + dataDirectoryLocationEnv);
      dd = DataDir.buildFromString(dataDirectoryLocationEnv);
    } else {
      File dataDirSettingFile = new File(ctx.getRealPath("/") + "/WEB-INF/datadir.location");
      LOG.info("Using file " + dataDirSettingFile.getAbsolutePath() +
        " for data directory location.");
      dd = DataDir.buildFromLocationFile(dataDirSettingFile);
    }
    try {
      if (dd.isConfigured()) {
        dd.clearTmp();
      }
    } catch (IOException e) {
      LOG.warn("Couldn't clear temporary data dir folder", e);
    }
    return dd;
  }

  /**
   * Provides a freemarker template loader as a singleton to be used anywhere needed. It is configured to access the
   * utf8 templates folder on the classpath, i.e. /src/resources/templates
   */
  @Provides
  @Singleton
  public Configuration provideFreemarker(DataDir datadir) {
    Configuration fm = new Configuration(Configuration.VERSION_2_3_31);
    List<TemplateLoader> tLoader = new ArrayList<>();
    tLoader.add(new ClassTemplateLoader(AppConfig.class, "/"));
    try {
      TemplateLoader tlDataDir = new DataDirTemplateLoader(datadir.dataFile(""));
      tLoader.add(tlDataDir);
    } catch (IOException e) {
      LOG.warn("Cannot load custom templates from data dir: " + e.getMessage(), e);
    }
    TemplateLoader tl = new MultiTemplateLoader(tLoader.toArray(new TemplateLoader[0]));
    fm.setDefaultEncoding("UTF-8");
    fm.setTemplateLoader(tl);

    return fm;
  }

  @Provides
  @Singleton
  public HttpClient provideHttpClient() {
    // new threadsafe, multithreaded http client with support for HTTP and HTTPS.
    String version = "unknown";
    try (InputStream configStream = new InputStreamUtils().classpathStream(AppConfig.CLASSPATH_PROPFILE)) {
      Properties props = new Properties();
      if (configStream == null) {
        LOG.error("Could not load default configuration from application.properties in classpath");
      } else {
        props.load(configStream);
        LOG.debug("Loaded default configuration from application.properties in classpath");
        version = props.getProperty(AppConfig.DEV_VERSION);
      }
    } catch (Exception e) {
      LOG.error("Unable to read version from application.properties, continuing start-up.", e);
    }

    String userAgent = String.format("GBIF-IPT/%s (+https://www.gbif.org/ipt) Java/%s (%s)",
        version,
        System.getProperty("java.version", "?"),
        System.getProperty("os.name", "?")
        );

    return HttpUtil.newMultithreadedClient(
        CONNECTION_TIMEOUT_MSEC,
        MAX_CONNECTIONS,
        MAX_PER_ROUTE,
        userAgent,
        new PreemptiveAuthenticationInterceptor());
  }

  @Provides
  @Singleton
  public JdbcSupport provideJdbcSupport() {
    JdbcSupport jdbcs = new JdbcSupport();
    InputStreamUtils streamUtils = new InputStreamUtils();
    InputStream configStream = streamUtils.classpathStream(JdbcSupport.CLASSPATH_PROPFILE);
    if (configStream == null) {
      LOG.error("Could not find supported jdbc driver information file " + JdbcSupport.CLASSPATH_PROPFILE);
    } else {
      try {
        Properties props = new Properties();
        props.load(configStream);
        jdbcs.setProperties(props);
        LOG.debug("Loaded supported jdbc driver information from " + JdbcSupport.CLASSPATH_PROPFILE);
      } catch (IOException e) {
        LOG.error("Could not load supported jdbc driver information from " + JdbcSupport.CLASSPATH_PROPFILE, e);
      }
    }
    return jdbcs;
  }

  @Provides
  @Singleton
  public SAXParserFactory provideNsAwareSaxParserFactory() {
    SAXParserFactory saxf = null;
    try {
      saxf = SAXParserFactory.newInstance();
      saxf.setValidating(false);
      saxf.setNamespaceAware(true);
    } catch (Exception e) {
      LOG.error("Cant create namespace aware SAX Parser Factory: " + e.getMessage(), e);
    }
    return saxf;
  }

  @Provides
  @Singleton
  public PBEEncrypt providePasswordEncryption() {
    final byte[] salt = {0x00, 0x05, 0x02, 0x05, 0x04, 0x25, 0x06, 0x17};
    PBEEncrypt enc = null;
    try {
      enc = new PBEEncrypt("Carla Maria Luise", salt, 9);
    } catch (EncryptionException e) {
      LOG.error("Cannot create password encryption", e);
    }
    return enc;
  }
}
