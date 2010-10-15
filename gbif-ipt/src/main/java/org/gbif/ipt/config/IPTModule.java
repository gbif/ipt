package org.gbif.ipt.config;

import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.PBEEncrypt;
import org.gbif.ipt.utils.PBEEncrypt.EncryptionException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryProvider;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.xml.parsers.SAXParserFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * A guice module containing wiring used for both test and production.
 * 
 * @See IPTTestModule which is used in tests and additionally injects a mock servlet context
 * 
 * @author markus
 * 
 */
public class IPTModule extends AbstractModule {
  protected Logger log = Logger.getLogger(this.getClass());

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
    bind(GenerateDwcaFactory.class).toProvider(
        FactoryProvider.newFactory(GenerateDwcaFactory.class, GenerateDwca.class));

  }

  @Provides
  @Singleton
  @Inject
  public DataDir provideDataDir(ServletContext ctx) {
    File dataDirSettingFile = new File(ctx.getRealPath("/") + "/WEB-INF/datadir.location");
    log.info("provide servlet context data dir location file at " + dataDirSettingFile.getAbsolutePath());
    DataDir dd = DataDir.buildFromLocationFile(dataDirSettingFile);
    try {
      if (dd != null && dd.isConfigured()) {
        dd.clearTmp();
      }
    } catch (IOException e) {
      log.warn("Couldnt clear temporary data dir folder", e);
    }
    return dd;
  }

  /**
   * Provides a freemarker template loader as a singleton to be used anywhere needed. It is configured to access the
   * utf8 templates folder on the classpath, i.e. /src/resources/templates
   * 
   * @param cfg
   * @return
   */
  @Provides
  @Singleton
  @Inject
  public Configuration provideFreemarker(AppConfig cfg, DataDir datadir) {
    Configuration fm = new Configuration();
    // load templates from classpath by prefixing /templates
    List<TemplateLoader> tLoader = new ArrayList<TemplateLoader>();
    tLoader.add(new ClassTemplateLoader(AppConfig.class, "/templates"));
    try {
      TemplateLoader tlDataDir = new DataDirTemplateLoader(datadir.dataFile(""));
      tLoader.add(tlDataDir);
    } catch (IOException e) {
      log.warn("Cannot load custom templates from data dir: " + e.getMessage(), e);
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
    HttpParams params = new BasicHttpParams();
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

    ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
    DefaultHttpClient client = new DefaultHttpClient(cm, params);

    // registry currently requires Preemptive authentication
    // add preemptive authentication via this interceptor
    HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
      private Logger log = Logger.getLogger(this.getClass());

      public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {

        log.debug(request.getRequestLine());
        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

        // If not auth scheme has been initialized yet
        if (authState.getAuthScheme() == null && credsProvider != null) {
          AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
          // Obtain credentials matching the target host
          Credentials creds = credsProvider.getCredentials(authScope);
          // If found, generate BasicScheme preemptively
          if (creds != null) {
            log.debug("Authentication used for scope " + authScope.getHost());
            authState.setAuthScheme(new BasicScheme());
            authState.setCredentials(creds);
          }
        }
      }
    };

    // Add as the very first interceptor in the protocol chain
    client.addRequestInterceptor(preemptiveAuth, 0);
    return client;
  }

  @Provides
  @Singleton
  public JdbcSupport provideJdbcSupport() {
    JdbcSupport jdbcs = new JdbcSupport();
    InputStreamUtils streamUtils = new InputStreamUtils();
    InputStream configStream = streamUtils.classpathStream(JdbcSupport.CLASSPATH_PROPFILE);
    if (configStream != null) {
      try {
        Properties props = new Properties();
        props.load(configStream);
        jdbcs.setProperties(props);
        log.debug("Loaded supported jdbc driver information from " + JdbcSupport.CLASSPATH_PROPFILE);
      } catch (IOException e) {
        log.error("Could not load supported jdbc driver information from " + JdbcSupport.CLASSPATH_PROPFILE, e);
      }
    } else {
      log.error("Could not find supported jdbc driver information file " + JdbcSupport.CLASSPATH_PROPFILE);
    }
    return jdbcs;
  }

  @Provides
  @Inject
  @Singleton
  public SAXParserFactory provideNsAwareSaxParserFactory() {
    SAXParserFactory saxf = null;
    try {
      ;
      saxf = SAXParserFactory.newInstance();
      saxf.setValidating(false);
      saxf.setNamespaceAware(true);
    } catch (Exception e) {
      log.error("Cant create namespace aware SAX Parser Factory: " + e.getMessage(), e);
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
      log.error("Cannot create password encryption", e);
    }
    return enc;
  }
}
