package org.gbif.ipt.config;

import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportingTask;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.PBEEncrypt;
import org.gbif.ipt.utils.PBEEncrypt.EncryptionException;
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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * A guice module containing wiring used for both test and production.
 */
public class IPTModule extends AbstractModule {

  private static final Logger LOG = Logger.getLogger(IPTModule.class);
  // 2 minute timeout
  protected static final int CONNECTION_TIMEOUT_MSEC = 120000;
  protected static final int MAX_CONNECTIONS = 100;
  protected static final int MAX_PER_ROUTE = 10;

  @Override
  protected void configure() {
    // singletons
    bind(AppConfig.class).in(Scopes.SINGLETON);
    bind(InputStreamUtils.class).in(Scopes.SINGLETON);
    bind(SimpleTextProvider.class).in(Scopes.SINGLETON);
    bind(ExtensionFactory.class).in(Scopes.SINGLETON);
    bind(VocabularyFactory.class).in(Scopes.SINGLETON);

    // prototypes
    bind(ThesaurusHandlingRule.class).in(Scopes.NO_SCOPE);

    // assisted inject factories
    install(
      new FactoryModuleBuilder().implement(ReportingTask.class, GenerateDwca.class).build(GenerateDwcaFactory.class));

  }

  @Provides
  @Singleton
  @Inject
  public DataDir provideDataDir(ServletContext ctx) {
    File dataDirSettingFile = new File(ctx.getRealPath("/") + "/WEB-INF/datadir.location");
    LOG.info("provide servlet context data dir location file at " + dataDirSettingFile.getAbsolutePath());
    DataDir dd = DataDir.buildFromLocationFile(dataDirSettingFile);
    try {
      if (dd != null && dd.isConfigured()) {
        dd.clearTmp();
      }
    } catch (IOException e) {
      LOG.warn("Couldnt clear temporary data dir folder", e);
    }
    return dd;
  }

  /**
   * Provides a freemarker template loader as a singleton to be used anywhere needed. It is configured to access the
   * utf8 templates folder on the classpath, i.e. /src/resources/templates
   */
  @Provides
  @Singleton
  @Inject
  public Configuration provideFreemarker(DataDir datadir) {
    Configuration fm = new Configuration();
    // load templates from classpath by prefixing /templates
    List<TemplateLoader> tLoader = new ArrayList<TemplateLoader>();
    tLoader.add(new ClassTemplateLoader(AppConfig.class, "/templates"));
    try {
      TemplateLoader tlDataDir = new DataDirTemplateLoader(datadir.dataFile(""));
      tLoader.add(tlDataDir);
    } catch (IOException e) {
      LOG.warn("Cannot load custom templates from data dir: " + e.getMessage(), e);
    }
    TemplateLoader tl = new MultiTemplateLoader(tLoader.toArray(new TemplateLoader[tLoader.size()]));
    fm.setDefaultEncoding("utf8");
    fm.setTemplateLoader(tl);

    return fm;
  }

  @Provides
  @Singleton
  @Inject
  public DefaultHttpClient provideHttpClient() {
    // new threadsafe, multithreaded http client with support for http and https.
    DefaultHttpClient client = HttpUtil.newMultithreadedClient(CONNECTION_TIMEOUT_MSEC, MAX_CONNECTIONS, MAX_PER_ROUTE);

    // registry currently requires Preemptive authentication
    // Add as the very first interceptor in the protocol chain
    client.addRequestInterceptor(new PreemptiveAuthenticationInterceptor(), 0);

    return client;
  }

  @Provides
  @Inject
  public HttpUtil provideHttpUtil() {
    // Retrieve the same client instance as configured in this module
    DefaultHttpClient client = provideHttpClient();
    // Return a singleton instance of HttpUtil
    return new HttpUtil(client);
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
  @Inject
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
  @Inject
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
