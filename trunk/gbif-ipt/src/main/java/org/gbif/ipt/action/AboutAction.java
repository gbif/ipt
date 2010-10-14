/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action;

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.admin.RegistrationManager;

import com.google.inject.Inject;

import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author markus
 * 
 */
public class AboutAction extends BaseAction {
  @Inject
  private Configuration ftl;
  @Inject
  private RegistrationManager regManager;
  private String content;
  private Organisation host;

  public String getContent() {
    return content;
  }

  public Organisation getHost() {
    return host;
  }

  @Override
  public void prepare() throws Exception {
    host = regManager.getHostingOrganisation();
    if (host == null) {
      host = new Organisation();
    }
    try {
      StringWriter result = new StringWriter();
      Template tmpl = ftl.getTemplate("datadir::config/about.ftl");
      tmpl.process(this, result);
      content = result.toString();
    } catch (Exception e) {
      log.warn("Cannot render custom about.ftl template from data dir", e);
      content = "";
    }

  }
}
