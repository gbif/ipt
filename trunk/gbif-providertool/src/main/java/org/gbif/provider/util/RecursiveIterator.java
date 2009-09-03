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
package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public class RecursiveIterator<T> implements Iterator<T> {
  private List<Iterator<T>> iters = new ArrayList<Iterator<T>>();
  private Iterator<T> currIter;
  private final T self;
  private boolean returnedThis = false;

  public RecursiveIterator(T self) {
    this.self = self;
  }

  public RecursiveIterator(T self, Iterator<T> iter) {
    this.self = self;
    currIter = iter;
  }

  public RecursiveIterator(T self, List<Iterator<T>> iters) {
    this.self = self;
    this.iters = iters;
    if (!this.iters.isEmpty()) {
      currIter = this.iters.remove(0);
    }
  }

  public boolean hasNext() {
    if (!returnedThis) {
      return true;
    }
    if (!iters.isEmpty()) {
      return true;
    }
    if (currIter != null) {
      return currIter.hasNext();
    }
    return false;
  }

  public T next() {
    if (!returnedThis) {
      returnedThis = true;
      return self;
    }
    if (currIter.hasNext()) {
      return currIter.next();
    }
    currIter = iters.remove(0);
    return currIter.next();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
