/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.RecordFilter.Comparator;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceWithHeader;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import static org.gbif.ipt.config.Constants.CANCEL_RESULTNAME;

/**
 * A rather complex action that deals with a single mapping configuration.
 * The {@link MappingAction#prepare} method does a lot of work.
 * For initial GET requests linked from the overview the {@link MappingAction#prepare} method decides on the result name, i.e. which
 * template to call.
 * We don't use any regular validation here but only raise warnings to the user.
 * So the {@link MappingAction#save} method is always executed for POST requests, but not for GETs.
 * Please don't add any action errors as this will trigger the validation interceptor and causes problems, use
 * {@link MappingAction#addActionWarning} instead.
 */
public class MappingAction extends ManagerBaseAction {

  private static final long serialVersionUID = -831969146160030857L;

  // logging
  private static final Logger LOG = LogManager.getLogger(MappingAction.class);

  private static final Pattern NORM_TERM = Pattern.compile("[^a-zA-Z0-9:]+");

  public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1";
  public static final String DC_TERMS_NAMESPACE = "http://purl.org/dc/terms";
  public static final String DWC_NAMESPACE = "https://dwc.tdwg.org/terms";
  public static final String DC_PREFIX = "dc:";
  public static final String DC_TERMS_PREFIX = "dcterms:";
  public static final String DWC_PREFIX = "dwc:";

  private final ExtensionManager extensionManager;
  private final SourceManager sourceManager;
  private final VocabulariesManager vocabManager;
  // config
  private ExtensionMapping mapping;
  private List<String> columns;
  private final Comparator[] comparators = Comparator.values();
  private List<String[]> peek;
  private List<PropertyMapping> fields;
  private Map<String, Integer> fieldsTermIndices = new HashMap<>();
  private Map<String, List<PropertyMapping>> fieldsByGroup = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> vocabTerms = new HashMap<>();
  private ExtensionProperty coreid;
  private ExtensionProperty datasetId;
  private Integer mid;
  private PropertyMapping mappingCoreid;
  private boolean doiUsedForDatasetId;

  @Inject
  public MappingAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, ExtensionManager extensionManager, SourceManager sourceManager,
    VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.extensionManager = extensionManager;
    this.sourceManager = sourceManager;
    this.vocabManager = vocabManager;
  }

  /**
   * Validate the mapping and report any warning or errors, shown on the mapping page.
   */
  private void validateAndReport() {
    if (mapping.getSource() == null) {
      return;
    }

    ExtensionMappingValidator validator = new ExtensionMappingValidator();
    ValidationStatus v = validator.validate(mapping, resource, peek, columns);
    if (v != null && !v.isValid()) {
      if (v.getIdProblem() != null) {
        addActionWarning(getText(v.getIdProblem(), v.getIdProblemParams()));
      }
      for (Term t : v.getMissingRequiredFields()) {
        addActionWarning(getText("validation.required", new String[] {t.simpleName()}));
      }
      for (Term t : v.getWrongDataTypeFields()) {
        addActionWarning(getText("validation.wrong.datatype", new String[] {t.simpleName()}));
      }
      // report columns that have been translated multiple times
      for (String columnName : v.getMultipleTranslationsForSameColumn()) {
        addActionError(getText("validation.column.multipleTranslations", new String[] {columnName}));
      }
    }
  }

  /**
   * This method auto-maps a source's columns. First it tries to automap the mappingCoreId column, and then it tries
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
      String normCol = normalizeColumnName(col);
      if (normCol != null && (
          termNormalizedPrefixedName(mappingCoreid.getTerm()).equalsIgnoreCase(normCol) ||
              TermFactory.normaliseTerm(mappingCoreid.getTerm().simpleName()).equalsIgnoreCase(normCol)
      )) {
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
        String normCol = normalizeColumnName(col);
        if (normCol != null && (
            termNormalizedPrefixedName(f.getTerm()).equalsIgnoreCase(normCol) ||
                TermFactory.normaliseTerm(f.getTerm().simpleName()).equalsIgnoreCase(normCol)
        )) {
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

  @Override
  public String cancel() {
    // remove empty mapping on cancel
    if (mapping != null && mapping.getSource() == null && mapping.getFields().isEmpty()) {
      resource.deleteMapping(mapping);
      // save resource
      saveResource();
    }
    return CANCEL_RESULTNAME;
  }

  @Override
  public String delete() {
    if (resource.deleteMapping(mapping)) {
      addActionMessage(getText("manage.mapping.deleted", new String[] {id}));
      // set mappings modified date
      resource.setMappingsModified(new Date());
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

  public ExtensionProperty getDatasetId() {
    return datasetId;
  }

  public Integer getMid() {
    return mid;
  }

  /**
   * @return list of columns in the source data that have not been mapped to field(s) yet, or an empty list if the
   * source data has no columns
   */
  public List<String> getNonMappedColumns() {
    List<String> mapped = new ArrayList<>();

    // return empty list if source data has no columns
    if (columns.isEmpty()) {
      return mapped;
    }

    // get list of all columns mapped to fields
    for (PropertyMapping field : fields) {
      if (field.getIndex() != null && field.getIndex() >=0 && field.getIndex() < columns.size()) {
        String sourceColumn = columns.get(field.getIndex());
        if (sourceColumn != null) {
          mapped.add(sourceColumn);
        }
      }
    }

    // get column mapped to coreId field
    if (mappingCoreid.getIndex() != null && mappingCoreid.getIndex() >= 0 && mappingCoreid.getIndex() < columns.size()
        && columns.get(mappingCoreid.getIndex()) != null) {
      mapped.add(columns.get(mappingCoreid.getIndex()));
    }

    // return list all source columns excluding those mapped
    List<String> nonMapped = new ArrayList<>(columns);
    nonMapped.removeAll(mapped);
    return nonMapped;
  }

  /**
   * @return list of groups in extension that are redundant (are already included in the core extension)
   */
  public List<String> getRedundantGroups() {
    List<String> redundantGroups = new ArrayList<>();
    if (resource.getCoreRowType() != null && !resource.getCoreRowType()
      .equalsIgnoreCase(mapping.getExtension().getRowType())) {
      Extension core = extensionManager.get(resource.getCoreRowType());
      redundantGroups = extensionManager.getRedundantGroups(mapping.getExtension(), core);
    }
    return redundantGroups;
  }

  public List<String[]> getPeek() {
    return peek;
  }

  public Map<String, Map<String, String>> getVocabTerms() {
    return vocabTerms;
  }

  /**
   * Normalizes an incoming column name so that it can later be compared against a ConceptTerm's simpleName.
   * This method converts the incoming string to lower case.
   *
   * @param col column name
   * @return the normalized column name, or null if the incoming name was null or empty
   */
  String normalizeColumnName(String col) {
    if (StringUtils.isNotBlank(col)) {
      col = NORM_TERM.matcher(col.toLowerCase()).replaceAll("");
      return col;
    }
    return null;
  }

  /**
   * Return term's prefixed with namespace name.
   * If namespace does not match any then simple name is returned.
   *
   * @param term term
   * @return term's normalized prefixed name
   */
  String termNormalizedPrefixedName(Term term) {
    String termNamespace = term.namespace().toString();

    if (termNamespace.startsWith(DC_NAMESPACE)) {
      return DC_PREFIX + TermFactory.normaliseTerm(term.simpleName());
    } else if (termNamespace.startsWith(DC_TERMS_NAMESPACE)) {
      return DC_TERMS_PREFIX + TermFactory.normaliseTerm(term.simpleName());
    } else if (termNamespace.startsWith(DWC_NAMESPACE)) {
      return DWC_PREFIX + TermFactory.normaliseTerm(term.simpleName());
    } else {
      return TermFactory.normaliseTerm(term.simpleName());
    }
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
        // The extension could have been null if:
        // 1. The user tried to add a core mapping with the select help option, no extension would have been found
        // 2. No extension could be retrieved for the id (rowtype)
        // The result should be the user stays on the overview page, and displays a warning informing them that they
        // need to perform another selection.
        else {
          addActionError(getText("manage.overview.DwC.Mappings.select.invalid"));
          defaultResult = "error";
        }
      } else {
        List<ExtensionMapping> maps = resource.getMappings(id);
        mapping = maps.get(mid);
      }
    } else {
      // worst case, just redirect to resource not found page
      notFound = true;
    }

    // skip if it's cancel request
    if (!cancel && !delete && mapping != null && mapping.getExtension() != null) {

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
      // determine the core row type
      String coreRowType = resource.getCoreRowType();
      if (coreRowType == null) {
        // not yet set, the current mapping must be the core type
        coreRowType = mapping.getExtension().getRowType();
      }
      LOG.info("Core row type: " + coreRowType);


      String coreIdTerm = AppConfig.coreIdTerm(coreRowType);
      coreid = extensionManager.get(coreRowType).getProperty(coreIdTerm);
      LOG.info("Field representing the id for the core: " + coreid);

      // setup the core record id term
      mappingCoreid = mapping.getField(coreid.getQualname());
      if (mappingCoreid == null) {
        mappingCoreid = new PropertyMapping();
        mappingCoreid.setTerm(coreid);
        mappingCoreid.setIndex(mapping.getIdColumn());
        fields = new ArrayList<>(mapping.getExtension().getProperties().size());
      } else {
        fields = new ArrayList<>(mapping.getExtension().getProperties().size() -1);
      }

      // inspect source
      readSource();

      datasetId = extensionManager.get(mapping.getExtension().getRowType()).getProperty(Constants.DWC_DATASET_ID);

      // prepare all other fields
      for (int i = 0; i < mapping.getExtension().getProperties().size(); i++) {
        ExtensionProperty ep = mapping.getExtension().getProperties().get(i);
        // ignore core id term
        if (!ep.equals(coreid)) {
          PropertyMapping pm = populatePropertyMapping(ep);
          fields.add(pm);

          // also store PropertyMapping by group/class
          String group = ep.getGroup();
          if (group != null) {
            fieldsByGroup.computeIfAbsent(group, k -> new ArrayList<>());
            fieldsByGroup.get(group).add(pm);
          }

          // for easy retrieval of PropertyMapping index by qualifiedName...
          fieldsTermIndices.put(ep.getQualname(), fields.lastIndexOf(pm));

          // populate vocabulary terms
          if (ep.getVocabulary() != null) {
            String id = ep.getVocabulary().getUriString();
            vocabTerms.put(id, vocabManager.getI18nVocab(id, getLocaleLanguage(), true));
          }
        }
      }

      // finally, do automapping if no fields are found
      if (mapping.getFields().isEmpty()) {
        int automapped = automap();
        if (automapped > 0) {
          addActionMessage(getText("manage.mapping.automaped", new String[] {String.valueOf(automapped)}));
        }
      }

      // ensure existing configuration re-loaded
      setDoiUsedForDatasetId(mapping.isDoiUsedForDatasetId());

      if (!isHttpPost()) {
        validateAndReport();
      }
    }
  }

  /**
   * Populate a PropertyMapping from an ExtensionProperty. If the ExtensionProperty is already mapped, preserves
   * the existing PropertyMapping. Otherwise, creates a brand new PropertyMapping.
   *
   * @param ep ExtensionProperty
   *
   * @return PropertyMapping created
   */
  private PropertyMapping populatePropertyMapping(ExtensionProperty ep) {
    // mapped already?
    PropertyMapping pm = mapping.getField(ep.getQualname());
    if (pm == null) {
      // no, create brand new PropertyMapping
      pm = new PropertyMapping();
    }
    pm.setTerm(ep);
    return pm;
  }

  private void readSource() {
    Source src = mapping.getSource();
    if (src == null) {
      columns = new ArrayList<>();
    } else {
      peek = sourceManager.peek(src, 5);
      // If user wants to import a source without a header lines, the columns are going to be numbered with the first
      // non-null value as an example. Otherwise, read the file/database normally.
      if ((src.isUrlSource() || src.isFileSource())
          && ((SourceWithHeader) src).getIgnoreHeaderLines() == 0) {
        columns = mapping.getColumns(peek);
      } else {
        columns = sourceManager.columns(src);
      }
      if (columns.isEmpty() && src.getName() != null) {
        addActionWarning(getText("manage.mapping.source.no.columns", new String[] {src.getName()}));
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
      Set<PropertyMapping> mappedFields = new TreeSet<>();
      for (PropertyMapping f : fields) {
        int index = f.getIndex() != null ? f.getIndex() : -9999;
        if (index >= 0 || StringUtils.trimToNull(f.getDefaultValue()) != null) {
          mappedFields.add(f);
        }
      }
      // save coreid field (e.g. occurrenceID) so that it is included in mapping, despite being a duplicate of coreid
      // Careful: only save coreid field for core extension mappings, not for extension mapping
      if (resource.getCoreRowType() != null &&
          resource.getCoreRowType().equalsIgnoreCase(mapping.getExtension().getRowType())) {
        mappingCoreid.setIndex(mapping.getIdColumn());
        mappingCoreid.setDefaultValue(mapping.getIdSuffix());
        int index = mappingCoreid.getIndex() != null ? mappingCoreid.getIndex() : -9999;
        if (index >= 0 || StringUtils.trimToNull(mappingCoreid.getDefaultValue()) != null) {
          mappedFields.add(mappingCoreid);
        }
      }
      // back to mapping object
      mapping.setFields(mappedFields);
      // persist other configurations, e.g. using DOI as datasetId
      mapping.setDoiUsedForDatasetId(doiUsedForDatasetId);
    }
    // update last modified dates
    Date lastModified = new Date();
    mapping.setLastModified(lastModified);
    resource.setMappingsModified(lastModified);

    // save entire resource config
    saveResource();
    // report validation without skipping this save
    validateAndReport();
    LOG.debug("mapping saved..");

    return defaultResult;
  }

  public String saveSetSource() {
    return INPUT;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
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

  public void setDatasetId(ExtensionProperty datasetId) {
    this.datasetId = datasetId;
  }

  public void setMid(Integer mid) {
    this.mid = mid;
  }

  /**
   * @return true if the DOI should be used for the datasetId, false otherwise
   */
  public boolean isDoiUsedForDatasetId() {
    return doiUsedForDatasetId;
  }

  public void setDoiUsedForDatasetId(boolean doiUsedForDatasetId) {
    this.doiUsedForDatasetId = doiUsedForDatasetId;
  }

  /**
   * Called from Freemarker template.
   */
  public Map<String, List<PropertyMapping>> getFieldsByGroup() {
    return fieldsByGroup;
  }

  /**
   * Called from Freemarker template.
   */
  public Map<String, Integer> getFieldsTermIndices() {
    return fieldsTermIndices;
  }

  /**
   * @return true if mapping is a core mapping, false if mapping is an extension mapping
   */
  public boolean isCoreMapping() {
    if (mapping.isCore() && resource.getCoreRowType() != null) {
      return resource.getCoreRowType().equalsIgnoreCase(mapping.getExtension().getRowType());
    }
    return false;
  }
}
