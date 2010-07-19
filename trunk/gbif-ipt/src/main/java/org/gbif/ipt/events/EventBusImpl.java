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

import org.gbif.ipt.events.IptEvent.Type;

/**
 * This class provides an default service implementation for {@link EventBus}.
 * 
 */
public class EventBusImpl implements EventBus {

  /**
   * This implementation simply dispatches to {@link HandlerManager}.
   */
  private final HandlerManager handlerManager = new HandlerManager(null);

  public <H extends EventHandler> HandlerRegistration addHandler(Type<H> type,
      H handler) {
    return handlerManager.addHandler(type, handler);
  }

  public void fireEvent(IptEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  public <H extends EventHandler> H getHandler(Type<H> type, int index) {
    return handlerManager.getHandler(type, index);
  }

  public int getHandlerCount(Type<?> type) {
    return handlerManager.getHandlerCount(type);
  }

  public boolean isEventHandled(Type<?> e) {
    return handlerManager.isEventHandled(e);
  }
}
