/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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

package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.StringWriter;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;

public class AboutAction extends BaseAction {

  private static final long serialVersionUID = -476675839075190159L;

  // logging
  private static final Logger LOG = Logger.getLogger(AboutAction.class);

  private final Configuration ftl;
  private String content;

  @Inject
  public AboutAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    Configuration ftl) {
    super(textProvider, cfg, registrationManager);
    this.ftl = ftl;
  }

  public String getContent() {
    return content;
  }

  @Override
  public void prepare() {
    // load hosting organization - call superclass' prepare()
    super.prepare();
    try {
      StringWriter result = new StringWriter();
      Template tmpl = ftl.getTemplate("datadir::config/about.ftl");
      tmpl.process(this, result);
      content = result.toString();
    } catch (Exception e) {
      LOG.warn("Cannot render custom about.ftl template from data dir", e);
      content = "";
    }
  }
}
