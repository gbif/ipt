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
import org.gbif.provider.util.QuoteTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * TODO: Documentation.
 * 
 */
public class KVPFilterFactory {
  private static final Map<String, Class> UNARY_LOP;
  private static final Map<String, Class> BINARY_LOPS;
  private static final Map<String, Class> UNARY_COP;
  private static final Map<String, Class> UNBOUND_COP;
  private static final Map<String, Class> BINARY_COPS;
  static {
    Map<String, Class> ops = new HashMap<String, Class>();
    ops.put("not", Not.class);
    UNARY_LOP = Collections.unmodifiableMap(ops);
  }
  static {
    Map<String, Class> ops = new HashMap<String, Class>();
    ops.put("and", And.class);
    ops.put("or", Or.class);
    BINARY_LOPS = Collections.unmodifiableMap(ops);
  }
  static {
    Map<String, Class> ops = new HashMap<String, Class>();
    ops.put("isnull", IsNull.class);
    UNARY_COP = Collections.unmodifiableMap(ops);
  }
  static {
    Map<String, Class> ops = new HashMap<String, Class>();
    ops.put("in", In.class);
    UNBOUND_COP = Collections.unmodifiableMap(ops);
  }
  static {
    Map<String, Class> ops = new HashMap<String, Class>();
    ops.put("equals", Equals.class);
    ops.put("like", Like.class);
    ops.put("greaterthan", GreaterThan.class);
    ops.put("lessthan", LessThan.class);
    ops.put("greaterthanorequals", GreaterThanOrEquals.class);
    ops.put("lessthanorequals", LessThanOrEquals.class);
    BINARY_COPS = Collections.unmodifiableMap(ops);
  }

  protected Log log = LogFactory.getLog(this.getClass());

  // instance vars
  private QuoteTokenizer tokenizer;
  private final LinkedList<String> queuedTokens = new LinkedList<String>();
  private BooleanBlock root;

  public Filter parse(String filter) throws ParseException {
    tokenizer = new QuoteTokenizer(filter, " ()");
    root = new BooleanBlock();
    queuedTokens.clear();
    log.debug("Preprocessing filter string into COP Atoms...");
    resolveAtoms();

    log.debug("Resolve boolean blocks...");
    Filter f = new Filter();
    f.setRoot(root.resolve());
    log.debug("Final filter: " + f.toString());
    return f;
  }

  private String getConcept(Object token) throws ParseException {
    if (isReserved(token)) {
      throw new ParseException("Reserved word found instead of concept: "
          + token);
    }
    if (!(token instanceof String)) {
      throw new ParseException(
          "Concept must be created from a string, but found: "
              + token.toString());
    }
    if (token.equals("(") || token.equals(")")) {
      throw new ParseException("Brackets found instead of concept");
    }
    return (String) token;
  }

  private String getLiteral(String token) throws ParseException {
    if (token.startsWith("\"") && token.endsWith("\"")) {
      return token.substring(1, token.length() - 1);
    }
    throw new ParseException("Literal expected, but found " + token);
  }

  // private LinkedList<BooleanOperator> atoms = new
  // LinkedList<BooleanOperator>();
  // wrapper for the tokenizer
  private boolean hasMoreTokens() {
    if (queuedTokens.size() > 0) {
      return true;
    }
    return tokenizer.hasMoreTokens();
  }

  private boolean isReserved(Object token) {
    if (UNARY_COP.containsKey(token) || BINARY_COPS.containsKey(token)
        || UNARY_LOP.containsKey(token) || BINARY_LOPS.containsKey(token)
        || UNBOUND_COP.containsKey(token)) {
      return true;
    }
    return false;
  }

  private String nextToken() {
    String tok;
    if (queuedTokens.isEmpty()) {
      tok = tokenizer.nextToken();
    } else {
      tok = queuedTokens.poll();
    }
    return tok;
  }

  /**
   * Resolves literals, comparison operators and tries to interpret LOP operator
   * precedence by inserting brackets when not existing
   * 
   * @return List of COPs or string based tokens (,),LOP strings
   * @throws ParseException
   */
  private void resolveAtoms() throws ParseException {
    BooleanBlock curr = root;
    String lastToken = null;
    try {
      while (hasMoreTokens()) {
        String token;
        try {
          token = nextToken();
        } catch (NoSuchElementException e) {
          throw new ParseException("Filter cant be parsed. Invalid", e);
        }
        String t = token.toLowerCase();
        if (token.equals(" ")) {
        } else if (token.equals("(")) {
          curr = curr.openBlock();
        } else if (token.equals(")")) {
          curr = curr.getParent();
        } else if (UNARY_LOP.containsKey(t)) {
          curr.addAtom(new Not());
        } else if (BINARY_LOPS.containsKey(t)) {
          LogicalMultiOperator lop = (LogicalMultiOperator) BINARY_LOPS.get(t).newInstance();
          curr.addAtom(lop);
        } else if (UNARY_COP.containsKey(t)) {
          ComparisonOperator cop = (ComparisonOperator) UNARY_COP.get(t).newInstance();
          cop.setProperty(getConcept(nextToken()));
          curr.addAtom(cop);
        } else if (BINARY_COPS.containsKey(t)) {
          ComparisonBinaryOperator cop = (ComparisonBinaryOperator) BINARY_COPS.get(
              t).newInstance();
          cop.setProperty(getConcept(lastToken));
          lastToken = null;
          cop.setValue(getLiteral(nextToken()));
          curr.addAtom(cop);
        } else if (UNBOUND_COP.containsKey(t)) {
          In cop = new In();
          cop.setProperty(getConcept(lastToken));
          lastToken = null;
          // the next thing is ugly but works fine... sorry, no time
          String tok = null;
          try {
            while (hasMoreTokens()) {
              tok = nextToken();
              cop.setValue(getLiteral(tok));
            }
          } catch (ParseException e) {
            // expected to throw this error at some point when there are no
            // literals anymore
            // put the last token back on the queue, it hasnt been properly
            // processed yet
            queuedTokens.add(tok);
          }
          curr.addAtom(cop);
        } else if (lastToken == null) {
          lastToken = token;
        } else {
          // we shouldn't be here...
          throw new ParseException("Unknown filter token: " + token);
        }
      }
    } catch (ParseException e) {
      throw e;
    } catch (Exception e) {
      log.error("Filter parsing error.", e);
    }
  }
}
