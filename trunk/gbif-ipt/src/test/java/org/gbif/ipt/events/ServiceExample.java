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

import static org.gbif.ipt.service.InvalidConfigException.TYPE.INVALID_BASE_URL;

import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;

import com.google.inject.Inject;

import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * An example class that demonstrates using an {@link EventBus} for listening to
 * {@link ConfigChangeEvent}s and also firing {@link ConfigInvalidEvent}s when a
 * changed configuration value is invalid.
 * 
 */
public class ServiceExample {

  private final EventBus bus;
  private String iptBaseUrl;

  @Inject
  ServiceExample(EventBus bus) {
    this.bus = bus;
    bus.addHandler(ConfigChangeEvent.TYPE, new ConfigChangeEventHandler() {
      public void onChange(ConfigChangeEvent event) {
        handleConfigChange(event.getConfig());
      }
    });
  }

  public String getIptBaseUrl() {
    return iptBaseUrl;
  }

  /**
   * Handles a configuration change. If the configuration is invalid, fires a
   * {@link ConfigInvalidEvent} on the {@link EventBus}.
   * 
   * @param config the IPT configuration that changed
   */
  protected void handleConfigChange(Config config) {
    String baseUrl = config.getBaseUrl();
    if (trimToNull(baseUrl) == null) {
      handleInvalidConfig(config, INVALID_BASE_URL,
          "Base URL is null or empty string");
      return;
    }
    if (baseUrl.contains("localhost")) {
      handleInvalidConfig(config, INVALID_BASE_URL,
          "IPT base URL contains localhost");
      return;
    }
    iptBaseUrl = baseUrl;
  }

  /**
   * Fires a {@link ConfigInvalidEvent} on the {@link EventBus}.
   * 
   * @param c the invalid config
   * @param t the type of {@link InvalidConfigException}
   * @param m the message describing why the configuration is invalid
   */
  private void handleInvalidConfig(Config c, TYPE t, String m) {
    bus.fireEvent(ConfigInvalidEvent.with(c, new InvalidConfigException(t, m)));
  }
}
