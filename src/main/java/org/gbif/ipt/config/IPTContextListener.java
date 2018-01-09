/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gbif.ipt.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;
import org.gbif.ipt.struts2.CharacterEncodingFilter;
import org.gbif.ipt.struts2.CorsFilter;
import org.gbif.ipt.struts2.ResponseHeaderFilter;
import org.gbif.ipt.struts2.SanitizeHtmlFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.SessionCookieConfig;
import java.net.URI;

public class IPTContextListener extends GuiceServletContextListener {
  private static final Logger LOG = Logger.getLogger(IPTContextListener.class);
  public static final String ALL_BUT_AUTHENTICATED = "^(?!.*/(manage|admin)/).*";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    // set httpOnly flag programmatically
    ServletContext servletContext = sce.getServletContext();
    SessionCookieConfig scc = servletContext.getSessionCookieConfig();
    scc.setHttpOnly(true);

    try {
      // In order to set the secure flag we need to know if the IPT is configured to run under https.
      // As the IPT can run behind some (reverse) proxy we can only trust the AppConfig, not the servlet context itself.
      // Luckily the guice injector has already been created in the super method above so we have access to it!
      Injector inj = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());
      AppConfig cfg = inj.getInstance(AppConfig.class);
      //scc.setSecure(true);
      URI iptUri = URI.create(cfg.getBaseUrl());
      scc.setSecure(iptUri.getScheme().equalsIgnoreCase("https"));
      if (!scc.isSecure()) {
        LOG.warn("The IPT is running under plain http. Please consider to use secure https instead.");
      }
    } catch (Exception e) {
      // ignore but log
      LOG.warn("Failed to determine if secure flag for cookies are needed. Ignore if the IPT's baseURL has not been configured yet");
    }

    LOG.info("SessionCookieConfig: httpOnly="+scc.isHttpOnly()+"; secure="+scc.isSecure());
  }

  /**
   * Ensure the injector is created when the web application is deployed.
   */
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(
      new Struts2GuicePluginModule(),
      new IPTModule(),
      new ServletModule() {

        @Override
        protected void configureServlets() {

          // Adds CORS headers to all IPT page responses
          bind(CorsFilter.class).in(Singleton.class);
          filter("/*").through(CorsFilter.class);

          // Struts 2 setup
          bind(StrutsPrepareAndExecuteFilter.class).in(Singleton.class);
          filter("/*").through(StrutsPrepareAndExecuteFilter.class);

          bind(CharacterEncodingFilter.class).in(Singleton.class);
          filter("/*").through(CharacterEncodingFilter.class);

          // Adds cache control headers to all IPT page responses
          bind(ResponseHeaderFilter.class).in(Singleton.class);
          filter("/*").through(ResponseHeaderFilter.class);

          // clean html removing xss content for ann unauthenticated pages
          bind(SanitizeHtmlFilter.class).in(Singleton.class);
          filterRegex(ALL_BUT_AUTHENTICATED).through(SanitizeHtmlFilter.class);
          super.configureServlets();
        }
      });
  }

}
