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
package org.gbif.provider.service;

import java.util.Collection;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public interface GenericManager<T> {
  void debugSession();

  boolean exists(Long id);

  void flush();

  T get(Long id);

  List<T> getAll();

  /**
   * Gets all records without duplicates.
   * 
   * @See GenericDao.getAllDistinct()
   * @return
   */
  List<T> getAllDistinct();

  List<Long> getAllIds();

  void remove(Long id);

  void remove(T obj);

  T save(T object);

  void saveAll(Collection<T> objs);
}
