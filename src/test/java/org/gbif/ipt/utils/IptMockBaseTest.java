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
package org.gbif.ipt.utils;

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.TestBeanProvider;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.HttpClient;

import javax.xml.parsers.SAXParserFactory;

public abstract class IptMockBaseTest extends IptBaseTest {

  protected DataDir dataDir;
  protected AppConfig cfg;
  private HttpClient http;

  public IptMockBaseTest() {
    this.dataDir = MockDataDir.buildMock();
    this.cfg = new AppConfig(dataDir);
  }

  protected HttpClient buildHttpClient() {
    // lazy load
    if (http == null) {
      http = TestBeanProvider.provideHttpClient();
    }
    return http;
  }

  protected ResourceManager buildResourceManager() {
    return null;
  }

  protected SAXParserFactory buildSaxFactory() {
    return TestBeanProvider.provideNsAwareSaxParserFactory();
  }
}
