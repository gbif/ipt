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
package org.gbif.provider.model.voc;

/**
 * TODO: Documentation.
 * 
 */
public enum ServiceType {
  EML("EML", "http://knb.ecoinformatics.org/software/eml/eml-2.0.1/index.html"), DiGIR(
      "DiGIR",
      "http://digir.sourceforge.net/schema/protocol/2003/1.0/digir.xsd"), BioCASE(
      "BioCASE", "http://www.biocase.org/Doc/Publications/Default.shtml"), TAPIR(
      "TAPIR", "http://rs.tdwg.org/tapir/1.0/schema/tapir.xsd"), RSS("RSS",
      "http://purl.org/rss/1.0/spec"), RSS2("RSS2",
      "http://blogs.law.harvard.edu/tech/rss"), ATOM("ATOM",
      "http://www.atompub.org/"), WFS("WFS",
      "http://www.opengeospatial.org/standards/wfs"), WMS("WMS",
      "http://www.opengeospatial.org/standards/wms"), TCS_RDF("TCS-RDF",
      "http://rs.tdwg.org/ontology/voc/TaxonConcept.rdf"), TCS_XML("TCS-XML",
      "http://www.tdwg.org/uploads/media/v101.xsd"), DWC_ARCHIVE("DWC-ARCHIVE",
      "http://wiki.tdwg.org/twiki/bin/view/DarwinCore/WebHome"), GWC("GWC",
      "http://geowebcache.org");

  public String code;
  public String link;

  private ServiceType(String code, String link) {
    this.code = code;
    this.link = link;
  }

  @Override
  public String toString() {
    return this.code;
  }
}
