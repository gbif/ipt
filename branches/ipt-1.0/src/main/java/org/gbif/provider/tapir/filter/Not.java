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

import org.gbif.provider.util.RecursiveIterator;

import java.util.Iterator;

/**
 * TODO: Documentation.
 * 
 */
public class Not extends LogicalOperator {
  private BooleanOperator op;

  public Not() {
    log.debug("Creating " + this.getClass().getSimpleName());
  }

  @Override
  public void addOperand(BooleanOperator operand) {
    setOp(operand);
  }

  public BooleanOperator getOp() {
    return op;
  }

  public Iterator<BooleanOperator> iterator() {
    return new RecursiveIterator<BooleanOperator>(this, op.iterator());
  }

  public void setOp(BooleanOperator op) {
    this.op = op;
  }

  @Override
  public String toHQL() {
    return "(" + op.toHQL() + ")";
  }

  @Override
  public String toString() {
    return String.format("not (%s)", op);
  }

  @Override
  public String toStringRecursive() {
    return toString();
  }

}
