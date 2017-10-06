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
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.struts2.Struts2GuicePluginModule;
import org.gbif.ipt.struts2.CharacterEncodingFilter;
import org.gbif.ipt.struts2.SanitizeHtmlFilter;

public class IPTContextListener extends GuiceServletContextListener {

  /**
   * Ensure the injector is created when the web application is deployed.
   */
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {

      @Override
      protected void configureServlets() {
        filter("/*").through(CharacterEncodingFilter.class);
        filter("/*").through(SanitizeHtmlFilter.class);
        super.configureServlets();
      }
    }, new Struts2GuicePluginModule(), new IPTModule());
  }

}
