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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sets SameSite=Lax on the session cookie, so browsers don't send it with cross-site POST requests, frames, images
 * or scripts, only with top-level GET navigation. This is a second line of defence: the CSRF token checked by
 * {@link org.gbif.ipt.struts2.CsrfInterceptor} is the main one.
 * <p>
 * Done here rather than in web.xml, which can only set cookie attributes with the Servlet 6 schema. The session
 * cookie configuration can only be changed while the context is being initialised.
 */
public class SessionCookieListener implements ServletContextListener {

  private static final Logger LOG = LogManager.getLogger(SessionCookieListener.class);

  public static final String SAME_SITE = "SameSite";
  public static final String SAME_SITE_VALUE = "Lax";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      SessionCookieConfig config = sce.getServletContext().getSessionCookieConfig();
      if (config.getAttribute(SAME_SITE) == null) {
        config.setAttribute(SAME_SITE, SAME_SITE_VALUE);
      }
      LOG.debug("Session cookie SameSite attribute: {}", config.getAttribute(SAME_SITE));
    } catch (RuntimeException | LinkageError e) {
      // e.g. a container older than Servlet 6
      LOG.warn("Could not set the SameSite attribute of the session cookie: {}", e.getMessage());
    }
  }
}
