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

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public class NamespaceRegistry {
  private final Map<String, String> ns2prefix = new HashMap<String, String>();

  public NamespaceRegistry() {
    super();
    add("http://www.gbif.org/ipt");
  }

  public NamespaceRegistry(DataResource resource) {
    this();
    this.addResource(resource);
  }

  public NamespaceRegistry(String... nss) {
    this();
    for (String ns : nss) {
      this.add(ns);
    }
  }

  public void add(ExtensionProperty property, String preferredPrefix) {
    add(property.getNamespace(), preferredPrefix);
  }

  public void add(String ns) {
    if (StringUtils.trimToNull(ns) != null) {
      // find meaningful prefix
      String prefix = getCommonPrefix(ns);
      if (prefix == null) {
        // try to create meaningful prefix ourselves
        String[] tokens = StringUtils.splitByWholeSeparator(ns, "/");
        if (tokens != null && tokens.length > 1) {
          int ti = tokens.length - 1;
          while (prefix == null) {
            prefix = tokens[ti].toLowerCase().replaceAll("[^a-z0-9]", "");
            if (StringUtils.isNumeric(prefix)) {
              // dont use prefixes that are only numbers
              prefix = null;
            } else {
              prefix = StringUtils.trimToNull(prefix);
            }
            ti--;
          }
        }
        if (prefix == null) {
          // cant find any. Use stupid default
          prefix = "x";
        }
        // make sure prefix is not too long
        prefix = StringUtils.left(prefix, 6);
      }
      add(ns, prefix);
    }
  }

  public void add(String namespace, String preferredPrefix) {
    // only add if not existing yet
    if (!ns2prefix.containsKey(namespace)) {
      String prefix = preferredPrefix;
      if (ns2prefix.containsValue(prefix)) {
        int i = 2;
        while (ns2prefix.containsValue(prefix)) {
          prefix = preferredPrefix + i;
          i++;
        }
      }
      ns2prefix.put(namespace, prefix);
    }
  }

  public void addAll(Collection<ExtensionProperty> properties) {
    for (ExtensionProperty p : properties) {
      add(p.getNamespace());
    }
  }

  public void addResource(DataResource resource) {
    for (ExtensionMapping view : resource.getAllMappings()) {
      this.addAll(view.getMappedProperties());
      this.add(view.getExtension().getNamespace());
    }
    this.add("http://rs.tdwg.org/dwc/dwcrecord/");
  }

  public boolean containsNs(String ns) {
    return ns2prefix.containsKey(ns);
  }

  public boolean containsPrefix(String prefix) {
    return ns2prefix.containsValue(prefix);
  }

  public boolean isEmpty() {
    return ns2prefix.isEmpty();
  }

  public Set<String> knownNamespaces() {
    return ns2prefix.keySet();
  }

  public String prefix(String ns) {
    return ns2prefix.get(ns);
  }

  public int size() {
    return ns2prefix.size();
  }

  public String tagname(ExtensionProperty prop) {
    return tagname(prop.getName());
  }

  public String tagnameQualified(ExtensionProperty prop) {
    return String.format("%s:%s", prefix(prop), tagname(prop));
  }

  public String tagnameQualifiedExtension(Extension ext) {
    String pre = "";
    if (prefix(ext.getNamespace()) != null) {
      pre = prefix(ext.getNamespace()) + ":";
    }
    return pre + tagname(ext.getName());
  }

  @Override
  public String toString() {
    return ns2prefix.toString();
  }

  public String xmlnsDef() {
    String xmlns = "";
    for (String ns : this.knownNamespaces()) {
      xmlns += String.format("xmlns:%s=\"%s\" ", prefix(ns), ns);
    }
    return xmlns;
  }

  private String getCommonPrefix(String ns) {
    if (ns == null) {
      return null;
    }
    // TODO: read external properties file into static map
    String prefix = null;
    if (ns.equalsIgnoreCase("http://ipt.gbif.org")) {
      prefix = "ipt";
    } else if (ns.equalsIgnoreCase("http://rs.gbif.org/ecat/terms/")) {
      prefix = "ecat";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/dwc/terms/")) {
      prefix = "dwc";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/dwc/dwcrecord/")) {
      prefix = "dwr";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/dwc/dwcore/")) {
      prefix = "dwcore";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/dwc/text/")) {
      prefix = "arch";
    } else if (ns.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
      prefix = "rdf";
    } else if (ns.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#")) {
      prefix = "rdfs";
    } else if (ns.equalsIgnoreCase("http://purl.org/dc/elements/1.1/")) {
      prefix = "dcel";
    } else if (ns.equalsIgnoreCase("http://purl.org/dc/terms/")) {
      prefix = "dc";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/tapir/1.0")) {
      prefix = "tapir";
    } else if (ns.equalsIgnoreCase("http://www.w3.org/2001/XMLSchema")) {
      prefix = "xsd";
    } else if (ns.equalsIgnoreCase("http://www.w3.org/2003/01/geo/wgs84_pos#")) {
      prefix = "geo";
    } else if (ns.equalsIgnoreCase("http://www.w3.org/2001/vcard-rdf/3.0#")) {
      prefix = "vcard";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/ontology/voc/TaxonName#")) {
      prefix = "tn";
    } else if (ns.equalsIgnoreCase("http://rs.tdwg.org/ontology/voc/TaxonConcept#")) {
      prefix = "tc";
    }
    return prefix;
  }

  private Object prefix(ExtensionProperty prop) {
    return prefix(prop.getNamespace());
  }

  private String tagname(String name) {
    return StringUtils.deleteWhitespace(StringUtils.capitaliseAllWords(name));
  }

}
