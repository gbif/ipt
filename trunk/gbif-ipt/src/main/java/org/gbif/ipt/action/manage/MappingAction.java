/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.RecordFilter.Comparator;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.ExtensionMappingValidator;
import org.gbif.ipt.validation.ExtensionMappingValidator.ValidationStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang.xwork.StringUtils;

/**
 * A rather complex action that deals with a single mapping configuration.
 * The prepare method does a lot of work.
 * For initial GET requests linked from the overview the prepare() method decides on the result name, i.e. which
 * template to call.
 * We dont use any regular validation here but only raise warnings to the user.
 * So the save method is always executed for POST requests, but not for GETs.
 * Please dont add any action errors as this will trigger the validation interceptor and causes problems, use
 * addActionWarning() instead.
 *
 * @author markus
 */
public class MappingAction extends ManagerBaseAction {

  private static final Pattern NORM_TERM = Pattern.compile("[\\W\\s_0-9]+");

  private ExtensionManager extensionManager;
  private SourceManager sourceManager;
  private VocabulariesManager vocabManager;
  // config
  private ExtensionMapping mapping;
  private List<String> columns;
  private final Comparator[] comparators = Comparator.values();
  private List<String[]> peek;
  private List<PropertyMapping> fields;
  private final Map<String, Map<String, String>> vocabTerms = new HashMap<String, Map<String, String>>();
  private ExtensionProperty coreid;
  private Integer mid;
  private PropertyMapping mappingCoreid;

  @Inject
  public MappingAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, ExtensionManager extensionManager, SourceManager sourceManager,
    VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.extensionManager = extensionManager;
    this.sourceManager = sourceManager;
    this.vocabManager = vocabManager;
  }

  public void addWarnings() {
    if (mapping.getSource() == null) {
      return;
    }

    ExtensionMappingValidator validator = new ExtensionMappingValidator();
    ValidationStatus v = validator.validate(mapping, resource, peek);
    if (v != null && !v.isValid()) {
      if (v.getIdProblem() != null) {
        addActionWarning(getText(v.getIdProblem(), v.getIdProblemParams()));
      }
      for (ConceptTerm t : v.getMissingRequiredFields()) {
        addActionWarning(getText("validation.required", new String[] {t.simpleName()}));
      }
      for (ConceptTerm t : v.getWrongDataTypeFields()) {
        addActionWarning(getText("validation.wrong.datatype", new String[] {t.simpleName()}));
      }
    }
  }

  /**
   * This method automaps a source's columns. First it tries to automap the mappingCoreId column, and then it tries
   * to automap the source's remaining fields against the core/extension.
   *
   * @return the number of terms that have been automapped
   */
  int automap() {
    // keep track of how many terms were automapped
    int automapped = 0;

    // start by trying to automap the mappingCoreId (occurrenceId/taxonId) to a column in source
    int idx1 = 0;
    for (String col : columns) {
      col = normalizeColumnName(col);
      if (col != null && mappingCoreid.getTerm().simpleNormalisedName().equalsIgnoreCase(col)) {
        // mappingCoreId and mapping id column must both be set (and have the same index) to automap successfully.
        mappingCoreid.setIndex(idx1);
        mapping.setIdColumn(idx1);
        // we have automapped the core id column, so increment automapped counter and exit
        automapped++;
        break;
      }
      idx1++;
    }

    // next, try to automap the source's remaining columns against the extensions fields
    for (PropertyMapping f : fields) {
      int idx2 = 0;
      for (String col : columns) {
        col = normalizeColumnName(col);
        if (col != null && f.getTerm().simpleNormalisedName().equalsIgnoreCase(col)) {
          f.setIndex(idx2);
          // we have automapped the term, so increment automapped counter and exit
          automapped++;
          break;
        }
        idx2++;
      }
    }

    return automapped;
  }

  /**
   * Normalizes an incoming column name so that it can later be compared against a ConceptTerm's simpleNormalizedName.
   * This method converts the incoming string to lower case, and will take the substring up to, but no including the
   * first ":".
   *
   * @param col column name
   *
   * @return the normalized column name, or null if the incoming name was null or empty
   */
  String normalizeColumnName(String col) {
    if (!Strings.isNullOrEmpty(col)) {
      col = NORM_TERM.matcher(col.toLowerCase()).replaceAll("");
      if (col.contains(":")) {
        col = StringUtils.substringAfter(col, ":");
      }
      return col;
    }
    return null;
  }

  public String cancel() {
    resource.deleteMapping(mapping);
    saveResource();
    return SUCCESS;
  }

  @Override
  public String delete() {
    if (resource.deleteMapping(mapping)) {
      addActionMessage(getText("manage.mapping.deleted", new String[] {id}));
      // set modified date
      resource.setModified(new Date());
      // save resource
      saveResource();
    } else {
      addActionMessage(getText("manage.mapping.couldnt.delete", new String[] {id}));
    }
    return SUCCESS;
  }

  public List<String> getColumns() {
    return columns;
  }

  public Comparator[] getComparators() {
    return comparators;
  }

  public ExtensionProperty getCoreid() {
    return coreid;
  }

  public List<PropertyMapping> getFields() {
    return fields;
  }

  public ExtensionMapping getMapping() {
    return mapping;
  }

  public PropertyMapping getMappingCoreid() {
    return mappingCoreid;
  }

  public Integer getMid() {
    return mid;
  }

  public List<String> getNonMappedColumns() {
    List<String> nonMappedColumns = new ArrayList<String>();
    nonMappedColumns.addAll(columns);
    for (int index = 0; index < columns.size(); index++) {
      if (columns.get(index).length() == 0) {
        nonMappedColumns.remove(columns.get(index));
      } else {
        if (mappingCoreid.getIndex() != null && mappingCoreid.getIndex() == index) {
          nonMappedColumns.remove(columns.get(index));
        } else {
          for (PropertyMapping field : fields) {
            if (field.getIndex() != null && field.getIndex() == index) {
              nonMappedColumns.remove(columns.get(index));
            }
          }
        }
      }
    }
    return nonMappedColumns;
  }

  public List<String[]> getPeek() {
    return peek;
  }

  public Map<String, Map<String, String>> getVocabTerms() {
    return vocabTerms;
  }

  @Override
  public void prepare() {
    super.prepare();
    // get mapping sequence id from parameters as setters are not called yet
    String midStr = StringUtils.trimToNull(req.getParameter("mid"));
    if (midStr != null) {
      mid = Integer.valueOf(midStr);
    }
    // id is rowtype
    if (id != null) {
      // mapping id, i.e. list index for the given rowtype, is given
      if (mid == null) {
        Extension ext = extensionManager.get(id);
        if (ext != null) {
          mapping = new ExtensionMapping();
          mapping.setExtension(ext);
        }
      } else {
        List<ExtensionMapping> maps = resource.getMappings(id);
        mapping = maps.get(mid);
      }
    }

    if (mapping == null || mapping.getExtension() == null) {
      notFound = true;
    } else {
      // is source assigned yet?
      if (mapping.getSource() == null) {
        // get source parameter as setters are not called yet
        String source = StringUtils.trimToNull(req.getParameter("source"));
        if (source != null) {
          Source src = resource.getSource(source);
          mapping.setSource(src);
        } else {
          // show set source form
          defaultResult = "source";
        }
      }
      // set empty filter if not existing
      if (mapping.getFilter() == null) {
        mapping.setFilter(new RecordFilter());
      }
      // setup the core record id term
      String coreRowType = resource.getCoreRowType();
      if (coreRowType == null) {
        // not yet set, the current mapping must be the core type
        coreRowType = mapping.getExtension().getRowType();
      }
      resource.setCoreType(StringUtils.capitalize(CoreRowType.OCCURRENCE.toString().toLowerCase()));
      String coreIdTerm = Constants.DWC_OCCURRENCE_ID;
      if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
        coreIdTerm = Constants.DWC_TAXON_ID;
        resource.setCoreType(StringUtils.capitalize(CoreRowType.CHECKLIST.toString().toLowerCase()));
      }
      coreid = extensionManager.get(coreRowType).getProperty(coreIdTerm);
      mappingCoreid = mapping.getField(coreid.getQualname());
      if (mappingCoreid == null) {
        // no, create bare mapping field
        mappingCoreid = new PropertyMapping();
        mappingCoreid.setTerm(coreid);
        mappingCoreid.setIndex(mapping.getIdColumn());
      }

      // inspect source
      readSource();

      // prepare all other fields
      fields = new ArrayList<PropertyMapping>(mapping.getExtension().getProperties().size());
      for (ExtensionProperty p : mapping.getExtension().getProperties()) {
        // ignore core id term
        if (p.equals(coreid)) {
          continue;
        }
        // uses a vocabulary?
        if (p.getVocabulary() != null) {
          vocabTerms.put(p.getVocabulary().getUri(),
            vocabManager.getI18nVocab(p.getVocabulary().getUri(), getLocaleLanguage(), true));
        }
        // mapped already?
        PropertyMapping f = mapping.getField(p.getQualname());
        if (f == null) {
          // no, create bare mapping field
          f = new PropertyMapping();
        }
        f.setTerm(p);
        fields.add(f);
      }

      // finally do automapping if no fields are found
      if (mapping.getFields().isEmpty()) {
        int automapped = automap();
        if (automapped > 0) {
          addActionMessage(getText("manage.mapping.automaped", new String[] {String.valueOf(automapped)}));
        }
      }

      if (!isHttpPost()) {
        // save, which does the validation, is not called for GET requests
        addWarnings();
      }
    }
  }

  private void readSource() {
    if (mapping.getSource() == null) {
      columns = new ArrayList<String>();
    } else {
      peek = sourceManager.peek(mapping.getSource(), 5);
      // If user wants to import a source without a header lines, the columns are going to be numbered with the first
      // non-null value as an example. Otherwise, read the file/database normally.
      if (mapping.getSource().isFileSource() && ((FileSource) mapping.getSource()).getIgnoreHeaderLines() == 0) {
        columns = mapping.getColumns(peek);
      } else {
        columns = sourceManager.columns(mapping.getSource());
      }
    }
  }

  @Override
  public String save() throws IOException {
    // a new mapping?
    if (resource.getMapping(id, mid) == null) {
      mid = resource.addMapping(mapping);
    } else {
      // save field mappings
      Set<PropertyMapping> mappedFields = new HashSet<PropertyMapping>();
      for (PropertyMapping f : fields) {
        if (f.getIndex() != null || StringUtils.trimToNull(f.getDefaultValue()) != null) {
          mappedFields.add(f);
        }
      }
      // save coreid field
      mappingCoreid.setIndex(mapping.getIdColumn());
      mappingCoreid.setDefaultValue(mapping.getIdSuffix());
      if (mappingCoreid.getIndex() != null || StringUtils.trimToNull(mappingCoreid.getDefaultValue()) != null) {
        mappedFields.add(mappingCoreid);
      }
      // back to mapping object
      mapping.setFields(mappedFields);
    }
    // set modified date
    resource.setModified(new Date());
    // save entire resource config
    saveResource();
    // report validation without skipping this save
    addWarnings();

    return defaultResult;
  }

  public String saveSetSource() {
    return INPUT;
  }

  public void setFields(List<PropertyMapping> fields) {
    this.fields = fields;
  }

  public void setMapping(ExtensionMapping mapping) {
    this.mapping = mapping;
  }

  public void setMappingCoreid(PropertyMapping mappingCoreid) {
    this.mappingCoreid = mappingCoreid;
  }

  public void setMid(Integer mid) {
    this.mid = mid;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }
}
