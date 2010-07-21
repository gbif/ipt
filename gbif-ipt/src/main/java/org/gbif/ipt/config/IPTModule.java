package org.gbif.ipt.config;

import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

import javax.servlet.ServletContext;

import freemarker.cache.ClassTemplateLoader;
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
  protected Log log = LogFactory.getLog(this.getClass());

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
  }

  @Provides
  @Singleton
  @Inject
  DataDir provideDataDir(ServletContext ctx) {
    File dataDirSettingFile = new File(ctx.getRealPath("/") + "/WEB-INF/datadir.location");
    log.info("provide servlet context data dir location file at " + dataDirSettingFile.getAbsolutePath());
    return DataDir.buildFromLocationFile(dataDirSettingFile);
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
  Configuration provideFreemarker(AppConfig cfg) {
    // load templates from classpath by prefixing /templates
    TemplateLoader tl = new ClassTemplateLoader(AppConfig.class, "/templates");

    Configuration fm = new Configuration();
    fm.setDefaultEncoding("utf8");
    fm.setTemplateLoader(tl);

    return fm;
  }

  @Provides
  @Singleton
  @Inject
  HttpClient provideHttpClient() {
    HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
    return client;
  }

  @Provides
  @Singleton
  @Inject
  Gbrds provideRegistryClient(AppConfig cfg) {
    String url = cfg.getProperty("dev.registry.url");
    // rely on the fact that AppConfig is already setup
    if (!cfg.isTestInstallation()) {
      url = cfg.getProperty("dev.registrydev.url");
    }
    Gbrds gbif = GbrdsImpl.init(url);
    log.info("Created GBF Registry client with URL: " + url);
    return gbif;
  }
}
