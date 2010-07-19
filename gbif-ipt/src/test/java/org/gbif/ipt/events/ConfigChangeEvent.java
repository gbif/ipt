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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that can be fired when the {@link Config} changes. This event is
 * handled by {@link ConfigChangeEventHandler} implementations.
 * 
 */
public class ConfigChangeEvent extends IptEvent<ConfigChangeEventHandler> {

  public static final Type<ConfigChangeEventHandler> TYPE = new Type<ConfigChangeEventHandler>();

  public static ConfigChangeEvent with(Config config) {
    checkNotNull(config, "Configuration is null");
    return new ConfigChangeEvent(config);
  }

  private final Config config;

  private ConfigChangeEvent(Config config) {
    this.config = config;
  }

  /**
   * @see org.gbif.ipt.events.IptEvent#getAssociatedType()
   */
  @Override
  public org.gbif.ipt.events.IptEvent.Type<ConfigChangeEventHandler> getAssociatedType() {
    return TYPE;
  }

  public Config getConfig() {
    return config;
  }

  /**
   * @see org.gbif.ipt.events.IptEvent#dispatch(org.gbif.ipt.events.EventHandler)
   */
  @Override
  protected void dispatch(ConfigChangeEventHandler handler) {
    handler.onChange(this);
  }
}
