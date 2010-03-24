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

import org.gbif.provider.tapir.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class BooleanBlock extends BooleanOperator implements
    Iterable<BooleanOperator> {
  class BlockIterator implements Iterator<BooleanOperator> {
    private int idx;
    private final BooleanBlock block;

    public BlockIterator(BooleanBlock booleanBlock) {
      block = booleanBlock;
    }

    public boolean hasNext() {
      return idx < block.atoms.size();
    }

    public BooleanOperator next() {
      BooleanOperator op = block.atoms.get(idx);
      idx++;
      return op;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private final List<BooleanOperator> atoms = new ArrayList<BooleanOperator>();
  private BooleanBlock parent = null;

  public BooleanBlock() {
    super();
  }

  public BooleanBlock(BooleanBlock parent) {
    super();
    this.parent = parent;
  }

  public void addAtom(BooleanOperator atom) {
    atoms.add(atom);
  }

  public BooleanBlock getParent() {
    return parent;
  }

  public boolean isEmpty() {
    return atoms.size() == 0;
  }

  public Iterator<BooleanOperator> iterator() {
    return new BlockIterator(this);
  }

  public BooleanBlock openBlock() {
    BooleanBlock block = new BooleanBlock(this);
    atoms.add(block);
    return block;
  }

  /**
   * Pretty weird routine. Should be easier to solve, but cant think of anything
   * for now. Here are examples of "blocks" that contain no ORs and no brackets
   * (=blocks) and how they are being resolved in this routine
   * 
   * not isnull and like and equals NEW ROOT not NOT isnull NOT(isnull) and
   * AND[not(isnull),?] like AND[not(isnull),like] and
   * AND[and[not(isnull),like],?] equals AND[and[not(isnull),like],equals]
   * 
   * 
   * not not isnull and like and not equals NEW ROOT UNFINISHED_LOP not NOT
   * NOT(?) not NOT(not(?)) not(NOT(?)) isnull NOT(not(isnull)) and
   * AND(not(not(isnull)),?) AND(not(not(isnull)),?) like
   * AND(not(not(isnull)),like) and AND(and(not(not(isnull)),like),?)
   * AND(and(not(not(isnull)),like),?) not
   * AND(and(not(not(isnull)),like),not(?))
   * and(and(not(not(isnull)),like),NOT(?)) equals
   * AND(and(not(not(isnull)),like),not(equals))
   * 
   * 
   * not not isnull or like and not equals NEW ROOT UNFINISHED_LOP not NOT
   * NOT(?) not NOT(not(?)) not(NOT(?)) isnull NOT(not(isnull)) or
   * OR(not(not(isnull)),?) OR(not(not(isnull)),?) like OR(not(not(isnull)),?)
   * OR(not(not(isnull)),?) | like and AND(and(not(not(isnull)),like),?)
   * AND(and(not(not(isnull)),like),?) not
   * AND(and(not(not(isnull)),like),not(?))
   * and(and(not(not(isnull)),like),NOT(?)) equals
   * AND(and(not(not(isnull)),like),not(equals))
   * 
   * 
   * 
   * less and equals and isnull NEW ROOT UNFINISHED_LOP UNASSIGNED_LEFT_ATOM
   * less . . less and AND(less,?) AND(less,?) . equals AND(less,equals)
   * AND(less,equals) and and(AND(less,equals),?) and(AND(less,equals),?) isnull
   * and(AND(less,equals),isnull)
   * 
   * @return a single boolean operator that is linked to the entire resolved
   *         subblocks. There should only be COPs and LOPs be left
   * @throws ParseException
   */
  public BooleanOperator resolve() throws ParseException {
    if (isEmpty()) {
      return null;
    }
    if (size() == 1) {
      return atoms.get(0);
    }
    // find ORs first as they have lowest precedence
    // create new block for both "sides"
    BooleanBlock leftBlock = new BooleanBlock(this);
    BooleanBlock rightBlock = new BooleanBlock(this);
    Or lop = null;
    for (BooleanOperator op : this) {
      if (op instanceof Or && lop == null) {
        // found the first OR operator
        lop = (Or) op;
      } else if (lop == null) {
        leftBlock.addAtom(op);
      } else {
        rightBlock.addAtom(op);
      }
    }
    if (lop != null) {
      // found at least one OR. Recursively resolve both operand blocks and
      // return the OR
      lop.addOperand(leftBlock.resolve());
      lop.addOperand(rightBlock.resolve());
      return lop;
    }

    // no ORs found.
    // Only NOT, AND, COPs and Blocks are left and there are at least 2 atoms...
    BooleanOperator root = null;
    BooleanOperator curr = null;
    LogicalOperator unfinishedLOP = null;
    BooleanOperator unassignedLeftAtom = null;
    Iterator<BooleanOperator> iter = this.iterator();
    while (iter.hasNext()) {
      curr = iter.next();
      // recursively resolve blocks (only AND/OR/COPs left)
      if (curr instanceof BooleanBlock) {
        curr = ((BooleanBlock) curr).resolve();
      }
      // find root. The last AND or NOT it is
      if (curr instanceof And) {
        root = curr;
      } else if (root == null && curr instanceof Not) {
        root = curr;
      }
      // some operator looking for its right operand?
      if (unfinishedLOP != null) {
        // does this need to be a COP?
        // think so, might want to test to rase an error. simple cast?
        unfinishedLOP.addOperand(curr);
        unassignedLeftAtom = unfinishedLOP;
        unfinishedLOP = null;
        continue;
      }
      // a new LOP that is waiting for its right operand?
      if (LogicalOperator.class.isAssignableFrom(curr.getClass())) {
        unfinishedLOP = (LogicalOperator) curr;
        if (LogicalMultiOperator.class.isAssignableFrom(curr.getClass())) {
          if (unassignedLeftAtom == null) {
            throw new ParseException(
                "Binary logical operator requires left argument");
          }
          unfinishedLOP.addOperand(unassignedLeftAtom);
          unassignedLeftAtom = null;
        }
      } else {
        if (unassignedLeftAtom != null) {
          throw new ParseException(
              "Unused left argument piled up. Maybe 2 COPs? "
                  + unassignedLeftAtom.getClass() + ", " + curr.getClass());
        }
        unassignedLeftAtom = curr;
      }
    }
    // bring back a single root operator
    return root;
  }

  public int size() {
    return atoms.size();
  }

  @Override
  public String toHQL() {
    try {
      return resolve().toHQL();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String toString() {
    return atoms.toString();
  }

}
