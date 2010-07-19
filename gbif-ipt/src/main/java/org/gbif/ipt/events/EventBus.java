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
 * This class provides a service interface for an event bus.
 */
public interface EventBus {

  /**
   * Adds a handle.
   * 
   * @param <H> The type of handler
   * @param type the event type associated with this handler
   * @param handler the handler
   * @return the handler registration, can be stored in order to remove the
   *         handler later
   */
  <H extends EventHandler> HandlerRegistration addHandler(
      IptEvent.Type<H> type, final H handler);

  /**
   * Fires the given event to the handlers listening to the event's type.
   * 
   * Note, any subclass should be very careful about overriding this method, as
   * adds/removes of handlers will not be safe except within this
   * implementation.
   * 
   * @param event the event
   */
  void fireEvent(IptEvent<?> event);

  /**
   * Gets the handler at the given index.
   * 
   * @param <H> the event handler type
   * @param index the index
   * @param type the handler's event type
   * @return the given handler
   */
  <H extends EventHandler> H getHandler(IptEvent.Type<H> type, int index);

  /**
   * Gets the number of handlers listening to the event type.
   * 
   * @param type the event type
   * @return the number of registered handlers
   */
  int getHandlerCount(Type<?> type);

  /**
   * Does this handler manager handle the given event type?
   * 
   * @param e the event type
   * @return whether the given event type is handled
   */
  boolean isEventHandled(Type<?> e);
}
