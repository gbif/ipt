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
package org.gbif.provider.tapir.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * TODO: Documentation.
 * 
 */
public class Filter implements Iterable<BooleanOperator> {
  class FilterIterator implements Iterator<BooleanOperator> {
    private Iterator<BooleanOperator> iter = null;

    public FilterIterator() {
      if (root != null) {
        iter = root.iterator();
      }
    }

    public boolean hasNext() {
      if (iter == null) {
        return false;
      }
      return iter.hasNext();
    }

    public BooleanOperator next() {
      return iter.next();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  protected Log log = LogFactory.getLog(this.getClass());

  private BooleanOperator root;

  public Filter() {
  }

  // needed due to the Digester based parsing
  public void addOperand(BooleanOperator operand) {
  }

  public BooleanOperator getRoot() {
    return root;
  }

  public Iterator<BooleanOperator> iterator() {
    return new FilterIterator();
  }

  public void setRoot(BooleanOperator root) {
    this.root = root;
  }

  /**
   * Will only set the root if it currently null
   */
  public void setRootIfNull(BooleanOperator root) {
    if (this.root == null && root != null) {
      log.debug("Setting root to: " + root.getClass());
      this.root = root;
    }
  }

  public String toHQL() {
    if (root == null) {
      return "";
    }
    return root.toHQL();
  }

  @Override
  public String toString() {
    String rot = "NULL";
    if (root != null) {
      rot = root.toString();
    }
    return "Filter: " + rot;
  }
}
