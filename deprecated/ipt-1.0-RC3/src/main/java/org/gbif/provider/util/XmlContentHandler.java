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

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Super simple SAX handler that extracts all element and attribute content from
 * any XML document. The resulting string is concatenating all content and
 * inserts a space at every element or attribute start.
 */
public class XmlContentHandler extends DefaultHandler {
  private String content = "";

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
  }

  // strips redundant whitespace
  public String getContent() {
    String[] words = content.split("\\s");
    StringBuffer sb = new StringBuffer();
    for (String w : words) {
      if (w != null && w.trim().length() > 0) {
        sb.append(w);
        sb.append(" ");
      }
    }
    return sb.toString().trim();
  }

  @Override
  public void startDocument() throws SAXException {
    content = "";
  }

  @Override
  public void startElement(String uri, String localName, String name,
      Attributes attributes) throws SAXException {
    int i = attributes.getLength();
    while (i > 0) {
      i--;
      content += " " + attributes.getValue(i);
    }
    content += " ";
  }
}
