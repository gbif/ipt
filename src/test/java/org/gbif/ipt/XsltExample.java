/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author markus
 * 
 */
public class XsltExample {
  public static void main(String[] args) throws Exception {
    SAXParserFactory saxf = null;
    saxf = SAXParserFactory.newInstance();
    saxf.setValidating(false);
    saxf.setNamespaceAware(true);

    SAXParser sax = saxf.newSAXParser();

    URL xslt = new URL("http://rs.gbif.org/style/human.xsl");
    URL xml = new URL("http://rs.gbif.org/vocabulary/gbif/taxonomic_status.xml");
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(new StreamSource(xslt.openStream()));
    transformer.transform(new StreamSource(xml.openStream()), new StreamResult(System.out));
  }
}
