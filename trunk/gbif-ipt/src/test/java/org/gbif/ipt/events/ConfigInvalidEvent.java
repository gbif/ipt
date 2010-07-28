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

import org.gbif.ipt.service.InvalidConfigException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that can be fired when the {@link Config} is invalid. This event is
 * handled by {@link ConfigInvalidEventHandler} implementations.
 * 
 */
public class ConfigInvalidEvent extends IptEvent<ConfigInvalidEventHandler> {

  public static final Type<ConfigInvalidEventHandler> TYPE = new Type<ConfigInvalidEventHandler>();

  public static ConfigInvalidEvent with(Config config, InvalidConfigException exception) {
    checkNotNull(config, "Configuration is null");
    checkNotNull(exception, "Exception is null");
    return new ConfigInvalidEvent(config, exception);
  }

  private final InvalidConfigException exception;
  private final Config config;

  private ConfigInvalidEvent(Config config, InvalidConfigException e) {
    this.config = config;
    this.exception = e;
  }

  /**
   * @see org.gbif.ipt.events.IptEvent#getAssociatedType()
   */
  @Override
  public org.gbif.ipt.events.IptEvent.Type<ConfigInvalidEventHandler> getAssociatedType() {
    return TYPE;
  }

  public Config getConfig() {
    return config;
  }

  public InvalidConfigException getException() {
    return exception;
  }

  /**
   * @see org.gbif.ipt.events.IptEvent#dispatch(org.gbif.ipt.events.EventHandler)
   */
  @Override
  protected void dispatch(ConfigInvalidEventHandler handler) {
    handler.onInvalid(this);
  }
}
