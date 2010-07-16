package org.gbif.ipt.config;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.InputStreamUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public class IPTModule extends AbstractModule{
	protected Log log = LogFactory.getLog(this.getClass());

	@Override 
	protected void configure() {
		// singletons
		bind(AppConfig.class).in(Scopes.SINGLETON);
		bind(InputStreamUtils.class).in(Scopes.SINGLETON);
		bind(SimpleTextProvider.class).in(Scopes.SINGLETON);
		
		// prototypes
		// ... none yet
	}
	
	
	/** Provides a freemarker template loader as a singleton to be used anywhere needed.
	 * It is configured to access the utf8 templates folder on the classpath, i.e. /src/resources/templates
	 * @param cfg
	 * @return
	 */
	@Provides @Singleton @Inject
	Configuration provideFreemarker(AppConfig cfg) {
		// load templates from classpath by prefixing /templates
		TemplateLoader tl = new ClassTemplateLoader(AppConfig.class, "/templates");
		
		Configuration fm = new Configuration();
		fm.setDefaultEncoding("utf8");
		fm.setTemplateLoader(tl);
		
		return fm;
	}
	
	@Provides @Singleton @Inject
	DataDir provideDataDir(ServletContext ctx) {
		File dataDirSettingFile = new File(ctx.getRealPath("/") + "/WEB-INF/datadir.location");
		log.info("provide servlet context data dir location file at "+dataDirSettingFile.getAbsolutePath());
		return new DataDir(dataDirSettingFile);
	}
	
}
