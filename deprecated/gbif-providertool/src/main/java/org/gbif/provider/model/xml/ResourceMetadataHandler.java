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
package org.gbif.provider.model.xml;

import org.gbif.provider.model.ResourceMetadata;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Super simple SAX handler that extracts all element and attribute content from
 * any XML document. The resulting string is concatenating all content and
 * inserts a space at every element or attribute start.
 * 
 */
public class ResourceMetadataHandler extends DefaultHandler {
  private String content;
  public ResourceMetadata meta;

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
  }

  @Override
  public void endElement(String uri, String localName, String name)
      throws SAXException {
    if (name.equalsIgnoreCase("key")) {
      meta.setUddiID(content);
    } else if (name.equalsIgnoreCase("name")) {
      meta.setTitle(content);
    } else if (name.equalsIgnoreCase("description")) {
      meta.setDescription(content);
    } else if (name.equalsIgnoreCase("primaryContactName")) {
      meta.setContactName(content);
    } else if (name.equalsIgnoreCase("primaryContactEmail")) {
      meta.setContactEmail(content);
    } else if (name.equalsIgnoreCase("homepageURL")) {
      meta.setLink(content);
    }
    content = "";
  }

  @Override
  public void startDocument() throws SAXException {
    content = "";
    meta = new ResourceMetadata();
  }

  @Override
  public void startElement(String uri, String localName, String name,
      Attributes attributes) throws SAXException {
    content = "";
  }

}
