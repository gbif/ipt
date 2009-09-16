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
package org.gbif.provider.webapp.action.tapir;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ValueListCount;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.tapir.Diagnostic;
import org.gbif.provider.tapir.ParseException;
import org.gbif.provider.tapir.Severity;
import org.gbif.provider.tapir.TapirOperation;
import org.gbif.provider.tapir.Template;
import org.gbif.provider.tapir.TemplateFactory;
import org.gbif.provider.tapir.Utils;
import org.gbif.provider.tapir.filter.Filter;
import org.gbif.provider.tapir.filter.KVPFilterFactory;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO: Documentation.
 * 
 */
public class TapirAction extends BaseOccurrenceResourceAction implements
    ServletRequestAware {
  private static final String ERROR = "error";
  private static final String PING = "ping";
  private static final String CAPABILITIES = "capabilities";
  private static final String METADATA = "metadata";
  private static final String SEARCH = "search";
  private static final String INVENTORY = "inventory";
  private static final String DWC_MODEL_LOCATION = "http://darwincore.googlecode.com/svn/trunk/tapir/dwc_extensible.tom";
  private static final String DWC_MODEL_ALIAS = "dwc";
  private static final String ABCD_MODEL_LOCATION = "http://darwincore.googlecode.com/svn/trunk/tapir/abcd.tom";
  private static final String ABCD_MODEL_ALIAS = "abcd";
  private static final Set<String> RESERVED_PARAMETERS = new HashSet<String>(
      Arrays.asList(new String[] {
          "operation", "op", "cnt", "count", "s", "start", "l", "limit", "t",
          "template", "c", "concept", "n", "tagname", "f", "filter", "e",
          "envelope", "m", "model", "p", "partial", "o", "orderby", "d",
          "descend"}));
  //
  private static final Pattern conceptAliasPattern = Pattern.compile("^p([0-9]+)$");

  @Autowired
  private DarwinCoreManager darwinCoreManager;
  @Autowired
  private ExtensionRecordManager extensionRecordManager;
  @Autowired
  private EmlManager emlManager;
  @Autowired
  private ExtensionPropertyManager extensionPropertyManager;

  // just in case of fatal errors
  private String error = "unknown fatal error";
  private HttpServletRequest request;
  // request parameters. For aliases different setters are used. See
  // RESERVED_PARAMETERS for all
  private String op = "m";
  private boolean count = false;
  private int start = 0;
  private int limit = 100;
  private int next = -1;
  private String template;
  private String concept;
  private String tagname;
  private String filter;
  private boolean envelope = true;
  private String model;
  private String orderby;
  private String descend;
  // parsed stuff
  private Filter pFilter;
  // TAPIR envelope data
  private final Date now = new Date();
  private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
  // for all request types with resource
  private final NamespaceRegistry nsr = new NamespaceRegistry();
  // CAPABILITIES only
  private Map<String, Set<ExtensionProperty>> conceptSchemas;
  // METADATA only
  private Eml eml;
  // INVENTORY only
  private final LinkedHashMap<ExtensionProperty, String> inventoryProperties = new LinkedHashMap<ExtensionProperty, String>(); // value=tagname
  private List<ValueListCount> values;
  // SEARCH only
  private final LinkedHashMap<ExtensionProperty, Boolean> orderByProperties = new LinkedHashMap<ExtensionProperty, Boolean>(); // value=descend
  private List<ExtendedRecord> records;
  // SUMMARY
  private Integer totalMatched;

  @Override
  public String execute() {
    if (op.startsWith("p")) {
      return ping();
    }
    if (!loadResource()) {
      return ERROR;
    }
    // template overwrites request and sets defaults.
    if (StringUtils.trimToNull(template) != null) {
      readTemplate();
    }
    // process operation
    if (op.startsWith("c")) {
      return capabilities();
    } else if (op.startsWith("m")) {
      return metadata();
    } else if (op.startsWith("i") || op.startsWith("s")) {
      try {
        if (op.startsWith("i")) {
          return inventory();
        } else {
          return search();
        }
      } catch (ParseException e) {
        addError(e.getTapirMessage());
      } catch (IllegalArgumentException e) {
        addError("Invalid request with illegal arguments submitted", e);
      } catch (Exception e) {
        addError("Unknown error", e);
      }
      return ERROR;
    } else {
      addInfo("Unknown TAPIR operation requested. Default to metadata");
      return metadata();
    }
  }

  public String getConcept() {
    return concept;
  }

  public Map<String, Set<ExtensionProperty>> getConceptSchemas() {
    return conceptSchemas;
  }

  public boolean getCount() {
    return count;
  }

  public boolean getDeclareNamespace() {
    return false;
  }

  public String getDescend() {
    return descend;
  }

  public List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }

  public Eml getEml() {
    return eml;
  }

  public boolean getEnvelope() {
    return envelope;
  }

  public String getError() {
    return error;
  }

  public String getFilter() {
    return filter;
  }

  public LinkedHashMap<ExtensionProperty, String> getInventoryProperties() {
    return inventoryProperties;
  }

  public int getLimit() {
    return limit;
  }

  public String getModel() {
    return model;
  }

  public String getModelAlias() {
    return DWC_MODEL_ALIAS;
  }

  public String getModelLocation() {
    return DWC_MODEL_LOCATION;
  }

  public int getNext() {
    return next;
  }

  public Date getNow() {
    return now;
  }

  public NamespaceRegistry getNsr() {
    return nsr;
  }

  public String getOp() {
    return op;
  }

  public String getOrderby() {
    return orderby;
  }

  public List<ExtendedRecord> getRecords() {
    return records;
  }

  @Override
  public OccurrenceResource getResource() {
    return resource;
  }

  public int getStart() {
    return start;
  }

  public String getTagname() {
    return tagname;
  }

  public String getTemplate() {
    return template;
  }

  public Integer getTotalMatched() {
    return totalMatched;
  }

  public List<ValueListCount> getValues() {
    return values;
  }

  public void setC(String concept) {
    this.concept = concept;
  }

  public void setCnt(String count) {
    this.count = Utils.isTrue(count);
  }

  public void setConcept(String concept) {
    this.concept = concept;
  }

  public void setCount(String count) {
    this.count = Utils.isTrue(count);
  }

  public void setD(String descend) {
    this.descend = descend;
  }

  public void setDescend(String descend) {
    this.descend = descend;
  }

  public void setE(String envelope) {
    this.envelope = Utils.isTrue(envelope);
  }

  public void setEnvelope(String envelope) {
    this.envelope = Utils.isTrue(envelope);
  }

  public void setF(String filter) {
    this.filter = filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public void setL(int limit) {
    this.limit = limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void setM(String model) {
    this.model = model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setN(String tagname) {
    this.tagname = tagname;
  }

  public void setO(String orderby) {
    this.orderby = orderby;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public void setOperation(String op) {
    this.op = op;
  }

  public void setOrderby(String orderby) {
    this.orderby = orderby;
  }

  public void setS(int start) {
    this.start = start;
  }

  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setT(String template) {
    this.template = template;
  }

  public void setTagname(String tagname) {
    this.tagname = tagname;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  private void addError(String message) {
    error = message;
    diagnostics.add(new Diagnostic(Severity.ERROR, new Date(), message));
  }

  private void addError(String message, Exception e) {
    error = message;
    diagnostics.add(new Diagnostic(Severity.ERROR, new Date(), message));
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    diagnostics.add(new Diagnostic(Severity.DEBUG, new Date(), sw.toString()));
  }

  private void addFatal(String message) {
    error = message;
    diagnostics.add(new Diagnostic(Severity.FATAL, new Date(), message));
  }

  private void addInfo(String message) {
    diagnostics.add(new Diagnostic(Severity.INFO, new Date(), message));
  }

  private void addMetaNamespaces() {
    nsr.add("http://purl.org/dc/elements/1.1/");
    nsr.add("http://rs.tdwg.org/dwc/terms/");
    nsr.add("http://purl.org/dc/terms/");
    nsr.add("http://www.w3.org/2003/01/geo/wgs84_pos#");
    nsr.add("http://www.w3.org/2001/vcard-rdf/3.0#");
  }

  private void addModelNamespaces() {
    nsr.add("http://rs.tdwg.org/dwc/dwcrecord/");
    nsr.add("http://rs.tdwg.org/dwc/terms/");
    nsr.add("http://purl.org/dc/terms/");
  }

  private void addWarning(String message) {
    diagnostics.add(new Diagnostic(Severity.WARN, new Date(), message));
  }

  private String capabilities() {
    conceptSchemas = new HashMap<String, Set<ExtensionProperty>>();
    for (ExtensionProperty prop : resource.getCoreMapping().getMappedProperties()) {
      String ns = prop.getNamespace();
      if (!conceptSchemas.containsKey(ns)) {
        conceptSchemas.put(ns, new HashSet<ExtensionProperty>());
      }
      conceptSchemas.get(ns).add(prop);
    }
    return CAPABILITIES;
  }

  private void doInventory() {
    concept = StringUtils.trimToNull(concept);
    if (inventoryProperties.isEmpty() && concept == null) {
      addError("At least one concept is required for an inventory");
      throw new IllegalArgumentException();
    }
    // multiple concepts provided?
    List<String> concepts = splitMultiValueParameter(concept);
    List<String> tagnames = splitMultiValueParameter(tagname);
    int i = 0;
    for (String c : concepts) {
      ExtensionProperty p = getProperty(c);
      log.debug("Found inventory concept " + c);
      String tag = "value";
      if (!tagnames.isEmpty() && tagnames.size() > i) {
        tag = tagnames.get(i);
      }
      inventoryProperties.put(p, tag);
      i++;
    }
    // get data
    if (inventoryProperties.isEmpty()) {
      addError("No known concepts requested to do inventory");
      throw new IllegalArgumentException();
    } else {
      List<ExtensionProperty> props = new ArrayList<ExtensionProperty>(
          inventoryProperties.keySet());
      values = darwinCoreManager.inventory(resourceId, props, pFilter, start,
          limit);
      if (count) {
        totalMatched = darwinCoreManager.inventoryCount(resourceId, props,
            pFilter);
        log.debug(totalMatched + " matched records found in total");
      }
    }
  }

  private void doSearch() {
    // parse orderby
    List<String> concepts = splitMultiValueParameter(orderby);
    List<String> descending = splitMultiValueParameter(descend);
    int i = 0;
    for (String c : concepts) {
      ExtensionProperty p = getProperty(c);
      Boolean desc = false;
      if (!descending.isEmpty() && descending.size() > i) {
        desc = Utils.isTrue(descending.get(i));
      }
      orderByProperties.put(p, desc);
    }

    // search
    List<DarwinCore> coreRecords = darwinCoreManager.search(resourceId,
        pFilter, start, limit);
    records = extensionRecordManager.extendCoreRecords(resource,
        coreRecords.toArray(new CoreRecord[coreRecords.size()]));
    if (count) {
      totalMatched = darwinCoreManager.searchCount(resourceId, pFilter);
      log.debug(totalMatched + " matched records found in total");
    }
  }

  /**
   * Looks up an occurrence property for a given Concept string, looking up
   * qualified names before aliases.
   * 
   * @param c
   * @return guaranteed to return an instance and never null
   */
  private ExtensionProperty getProperty(String c) {
    ExtensionProperty p = null;
    Matcher m = conceptAliasPattern.matcher(c);
    if (m.find()) {
      p = extensionPropertyManager.get(Long.decode(m.group(1)));
    } else {
      p = extensionPropertyManager.getCorePropertyByQualName(c);
      if (p == null) {
        // still not found. Try to find by name only
        p = extensionPropertyManager.getCorePropertyByName(c);
      }
    }
    if (p == null) {
      throw new IllegalArgumentException("Requested concept unknown: " + c);
    }
    return p;
  }

  private Map<String, String> getSimpleParameterMap() {
    Map<String, String[]> mapIn = request.getParameterMap();
    Map<String, String> map = new HashMap<String, String>();
    Set<String> reserved = new HashSet<String>();
    for (String k : mapIn.keySet()) {
      if (RESERVED_PARAMETERS.contains(k)) {
        reserved.add(k);
        continue;
      }
      String[] vals = mapIn.get(k);
      if (vals.length == 0) {
        addInfo(String.format("Request parameter %s is empty", k));
      } else {
        map.put(k, vals[0]);
        if (vals.length > 1) {
          addWarning(String.format(
              "Request parameter %s contained multiple values. Only first one has been used",
              k));
        }
      }
    }
    if (reserved.size() > 0) {
      addInfo("Ignore reserved TAPIR parameters: "
          + StringUtils.join(reserved, ", "));
    }
    return map;
  }

  //
  // INVENTORY
  //
  private String inventory() throws ParseException {
    values = new ArrayList<ValueListCount>();
    parseFilter();
    doInventory();
    setNextValue();
    return INVENTORY;
  }

  //
  // HELPER
  //
  private boolean loadResource() {
    if (resourceId != null) {
      resource = occResourceManager.get(resourceId);
      if (resource != null) {
        nsr.addResource(resource);
        return true;
      }
    }
    // resource cant be loaded but is required
    addFatal("Resource unknown");
    return false;
  }

  private String metadata() {
    addMetaNamespaces();
    eml = emlManager.load(resource);
    return METADATA;
  }

  private void parseFilter() throws ParseException, IllegalArgumentException {
    if (pFilter == null) {
      pFilter = new KVPFilterFactory().parse(filter);
      log.debug("Filter created: " + pFilter.toString());
    } else {
      log.debug("Filter existing already. Dont read filter request parameter");
    }
    extensionPropertyManager.lookupFilterCoreProperties(pFilter);
  }

  private String ping() {
    return PING;
  }

  private void readTemplate() {
    try {
      Template tmpl = TemplateFactory.buildTemplate(new URL(template),
          getSimpleParameterMap());
      addInfo("Read TAPIR template " + template);
      if (tmpl.getOperation().equals(TapirOperation.search)) {
        op = "s";
        model = tmpl.getModel();
        pFilter = tmpl.getFilter();
        Map<String, Boolean> orby = tmpl.getOrderBy();
        if (!orby.isEmpty()) {
          orderByProperties.clear();
          for (String c : orby.keySet()) {
            ExtensionProperty p = getProperty(c);
            orderByProperties.put(p, orby.get(c));
          }
        }
      } else if (tmpl.getOperation().equals(TapirOperation.inventory)) {
        op = "i";
        pFilter = tmpl.getFilter();
        Map<String, String> invProps = tmpl.getConcepts();
        if (!invProps.isEmpty()) {
          inventoryProperties.clear();
          for (String c : invProps.keySet()) {
            ExtensionProperty p = getProperty(c);
            inventoryProperties.put(p, invProps.get(c));
          }
        }
      } else {
        addError("Template defines neither a search nor an inventory");
      }
    } catch (MalformedURLException e) {
      addError("Template URL is not valid: " + template);
    } catch (ParseException e) {
      addError("Template parse exception", e);
    } catch (Exception e) {
      addError("Unknown template exception", e);
    }
  }

  //
  // SEARCH
  //
  private String search() throws ParseException {
    // check requested model
    if (model != null) {
      if (model.equalsIgnoreCase(DWC_MODEL_LOCATION) || model.equalsIgnoreCase(DWC_MODEL_ALIAS)) {
        parseFilter();
        doSearch();
        setNextRecord();
        addModelNamespaces();
        return SEARCH;
      }else if (model.equalsIgnoreCase(ABCD_MODEL_LOCATION) || model.equalsIgnoreCase(ABCD_MODEL_ALIAS)) {
          addFatal("The requested ABCD output model is not yet supported.");
      }
      addFatal("The requested output model is not supported: " + model);
    } else {
      addFatal("Illegal request. No template or output model has been specified");
    }
    return ERROR;
  }

  private void setNextRecord() {
    next = start + limit;
    if (count) {
      if (totalMatched <= next) {
        next = -1;
      }
    } else {
      if (records == null || limit > records.size()) {
        next = -1;
      }
    }
  }

  private void setNextValue() {
    next = start + limit;
    if (count) {
      if (totalMatched <= next) {
        next = -1;
      }
    } else {
      if (values == null || limit > values.size()) {
        next = -1;
      }
    }
  }

}