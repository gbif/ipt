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

import org.apache.commons.lang.StringUtils;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * TODO: Documentation.
 * 
 */
public class QuoteTokenizer extends StringTokenizer {
  private boolean quoted = false;

  public QuoteTokenizer(String str) {
    this(str, " ");
  }

  public QuoteTokenizer(String str, String delim) {
    super(StringUtils.trimToEmpty(str), "\"" + delim, true);
  }

  @Override
  public Object nextElement() {
    throw new IllegalAccessError();
  }

  @Override
  public String nextToken() throws NoSuchElementException {
    // NoSuchElementException()
    String token = super.nextToken();
    if (token.equals("\"")) {
      quoted = true;
    }
    while (quoted) {
      try {
        token += super.nextToken();
        if (token.endsWith("\"")) {
          quoted = false;
        }
      } catch (NoSuchElementException e) {
        throw new NoSuchElementException("Quoted string not closed");
      }
    }
    while (token.equals(" ")) {
      token = nextToken();
    }
    return token;
  }

  @Override
  public String nextToken(String delim) {
    throw new IllegalAccessError();
  }

}
