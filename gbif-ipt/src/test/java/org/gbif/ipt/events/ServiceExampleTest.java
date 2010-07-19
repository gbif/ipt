/*
 * Copyright 2010 GBIF.
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
 */
package org.gbif.ipt.events;

import static com.google.inject.Scopes.SINGLETON;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.gbif.ipt.service.InvalidConfigException.TYPE.INVALID_BASE_URL;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test coverage for {@link ServiceExample}.
 */
public class ServiceExampleTest {

  static class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ServiceExample.class).in(SINGLETON);
      bind(EventBus.class).to(EventBusImpl.class).in(SINGLETON);
    }
  }

  private final static Injector injector = Guice.createInjector(new GuiceModule());

  /**
   * Test method for
   * {@link org.gbif.ipt.events.ServiceExample#handleConfigChange(org.gbif.ipt.config.AppConfig)}
   * .
   */
  @Test
  public final void testHandleConfigChange() {
    ServiceExample service = injector.getInstance(ServiceExample.class);
    EventBus bus = injector.getInstance(EventBus.class);
    Config config;

    // Tests valid base URL. Fires a ConfigChangeEvent on the bus. Since
    // ServiceExample is listening to ConfigChangeEvent, we expect its IPT base
    // URL to change:
    String baseUrl = "http://foo.com";
    config = IptConfig.builder().baseUrl(baseUrl).build();
    bus.fireEvent(ConfigChangeEvent.with(config));
    assertEquals(baseUrl, service.getIptBaseUrl());

    baseUrl = "http://bar.com";
    config = IptConfig.builder().baseUrl(baseUrl).build();
    bus.fireEvent(ConfigChangeEvent.with(config));
    assertEquals(baseUrl, service.getIptBaseUrl());

    // Tests invalid base URLs. We expect a ConfigInvalidEvent to be fired on
    // the bus by ServiceExample, so we add a handler on the bus that verifies
    // the ConfigInvalidEvent:
    bus.addHandler(ConfigInvalidEvent.TYPE, new ConfigInvalidEventHandler() {
      public void onInvalid(ConfigInvalidEvent event) {
        assertEquals(event.getException().getType(), INVALID_BASE_URL);
        System.out.println(event.getException());
      }
    });

    // Now we fire ConfigChangeEvents with invalid base URLs, and our handler
    // above will verify that ServiceExample fired ConfigInvalidEvent:
    config = IptConfig.builder().baseUrl(null).build();
    bus.fireEvent(ConfigChangeEvent.with(config));
    config = IptConfig.builder().baseUrl("").build();
    bus.fireEvent(ConfigChangeEvent.with(config));
    config = IptConfig.builder().baseUrl("http://localhost.com").build();
    bus.fireEvent(ConfigChangeEvent.with(config));
  }
}
