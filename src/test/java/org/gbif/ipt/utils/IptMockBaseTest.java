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

package org.gbif.ipt.utils;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.HttpUtil;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.xml.parsers.SAXParserFactory;

/**
 * @author markus
 * 
 */
public abstract class IptMockBaseTest {
  protected IPTModule guice = new IPTModule();
  protected DataDir dataDir;
  protected AppConfig cfg;
  private HttpUtil http;
  protected DefaultHttpClient client;

  public IptMockBaseTest() {
    super();
    this.dataDir = DataDir.buildMock();
    this.cfg = new AppConfig(dataDir);
  }

  protected DefaultHttpClient buildHttpClient() {
    // lazy load
    if (client == null) {
      client = guice.provideHttpClient();
    }
    return client;
  }

  protected HttpUtil buildHttpUtil() {
    // lazy load
    if (http == null) {
      http = new HttpUtil(buildHttpClient());
    }
    return http;
  }

  protected ResourceManager buildResourceManager() {
    return null;
  }

  protected SAXParserFactory buildSaxFactory() {
    return guice.provideNsAwareSaxParserFactory();
  }
}
