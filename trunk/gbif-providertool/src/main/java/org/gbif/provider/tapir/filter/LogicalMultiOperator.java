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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public abstract class LogicalMultiOperator extends LogicalOperator {
  private final List<BooleanOperator> operands = new ArrayList<BooleanOperator>();

  @Override
  public void addOperand(BooleanOperator operand) {
    this.operands.add(operand);
  }

  public List<BooleanOperator> getOperands() {
    return operands;
  }

  public Iterator<BooleanOperator> iterator() {
    List<Iterator<BooleanOperator>> iters = new ArrayList<Iterator<BooleanOperator>>();
    for (BooleanOperator op : operands) {
      iters.add(op.iterator());
    }
    return new RecursiveIterator<BooleanOperator>(this, iters);
  }

  @Override
  public String toHQL() {
    if (operands.size() < 2) {
      throw new IllegalStateException(
          "LogicalMultiOperator must have at least two operands");
    }
    List<String> operandsHQL = new ArrayList<String>();
    for (BooleanOperator op : operands) {
      operandsHQL.add(op.toHQL());
    }
    return "(" + StringUtils.join(operandsHQL, " " + getOperatorSymbol() + " ")
        + ")";
  }

  @Override
  public String toString() {
    return getOperatorSymbol().toUpperCase() + ": " + toStringRecursive();
  }

  @Override
  public String toStringRecursive() {
    List<String> inner = new ArrayList<String>();
    if (operands.size() > 1) {
      for (BooleanOperator op : operands) {
        inner.add(op.toStringRecursive());
      }
    } else if (operands.size() > 0) {
      inner.add(operands.get(0).toStringRecursive());
      inner.add("?");
    } else {
      inner.add("?");
      inner.add("?");
    }
    return StringUtils.join(inner, " " + getOperatorSymbol() + " ");
  }

  protected abstract String getOperatorSymbol();

}
