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

import javax.servlet.ServletContext;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This guice module provides objects only for true IPTs running in a servlet environment. The module is replaced by a
 * test module when using guice for unit tests.
 */
public class IPTTestModule extends IPTModule {

  /**
   * provide a test datadir based on classpath.
   */
  @Provides
  @Singleton
  ServletContext provideMockServlet() {
    return new MockServletContext();
  }
}
