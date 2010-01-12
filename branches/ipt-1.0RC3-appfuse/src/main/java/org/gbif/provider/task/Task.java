/*
 * Copyright 2009 GBIF.
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
package org.gbif.provider.task;

import org.gbif.provider.model.DataResource;

import java.util.concurrent.Callable;

/**
 * Extended interface for resource related providertool tasks. Each tasks needs
 * to be initialised via the init method first before it can be submitted to an
 * executor. The constructor is avoided for this to allow tasks being declared
 * as Spring prototype beans thereby being able to use DI
 * 
 * @param <T>
 */
public interface Task<T> extends Callable<T> {
  Long getResourceId();

  /**
   * Instead of constructor call this method once before using a Task bean
   * 
   * @param resourceId the resource this tasks will work on. Not NULL
   * @param userId the user that has submitted this task. Optional, maybe also
   *          be null
   */
  void init(Long resourceId);

  DataResource loadResource();

  String status();

  int taskTypeId();
}
