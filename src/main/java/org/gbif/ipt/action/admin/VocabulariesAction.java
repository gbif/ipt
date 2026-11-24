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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * The vocabulary page, under administration and under manage (opened from mappings and translations): the vocabulary,
 * the extension terms using it and its concepts.
 */
public class VocabulariesAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 7277675384287096912L;
  private static final Logger LOG = LogManager.getLogger(VocabulariesAction.class);

  private final VocabulariesManager vocabManager;
  private final ExtensionManager extensionManager;
  private final RegistryManager registryManager;
  @Getter
  private Vocabulary vocabulary;
  // installed extensions with terms taking their values from the vocabulary, by extension title
  @Getter
  private final List<ExtensionUsage> usedBy = new ArrayList<>();
  // languages of the preferred labels, by name, and how many concepts have a label in each
  @Getter
  private final Map<String, Integer> labelLanguages = new LinkedHashMap<>();
  // administration page only: whether the version was checked against the Registry, and the newer version's date
  @Getter
  private boolean versionChecked;
  @Getter
  private boolean registryUnreachable;
  @Getter
  private Date newerIssued;

  @Inject
  public VocabulariesAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      VocabulariesManager vocabManager,
      ExtensionManager extensionManager,
      RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager);
    this.vocabManager = vocabManager;
    this.extensionManager = extensionManager;
    this.registryManager = registryManager;
  }

  @Override
  public String execute() throws Exception {
    // the page is about one vocabulary: none asked for or an unknown one is not found
    vocabulary = id == null ? null : vocabManager.get(id);
    if (vocabulary == null) {
      return NOT_FOUND;
    }
    populateUsage();
    populateLanguages();
    return SUCCESS;
  }

  /**
   * The administration page: also checks the Registry for a newer version.
   */
  public String admin() throws Exception {
    String result = execute();
    if (vocabulary != null) {
      checkVersion();
    }
    return result;
  }

  /**
   * @return true if the IPT installs the vocabulary itself and keeps it at the latest version
   */
  public boolean isDefaultVocabulary() {
    return vocabulary != null && vocabManager.isDefault(vocabulary.getUriString());
  }

  /**
   * @return the concept's preferred label in the interface language, else in English, else none
   */
  public String labelOf(VocabularyConcept concept) {
    String lang = getLocaleLanguage();
    VocabularyTerm english = null;
    for (VocabularyTerm t : concept.getPreferredTerms()) {
      if (StringUtils.equalsIgnoreCase(lang, t.getLang())) {
        return t.getTitle();
      } else if ("en".equalsIgnoreCase(t.getLang())) {
        english = t;
      }
    }
    return english == null ? null : english.getTitle();
  }

  /**
   * @return true when the concept has preferred labels, but none in the interface language (English is shown instead)
   */
  public boolean isEnglishFallback(VocabularyConcept concept) {
    String lang = getLocaleLanguage();
    return !"en".equalsIgnoreCase(lang)
        && concept.getPreferredTerms().stream().noneMatch(t -> StringUtils.equalsIgnoreCase(lang, t.getLang()))
        && concept.getPreferredTerms().stream().anyMatch(t -> "en".equalsIgnoreCase(t.getLang()));
  }

  /**
   * @return the concept's alternative terms in the interface language or without a language
   */
  public List<String> alternativesOf(VocabularyConcept concept) {
    String lang = getLocaleLanguage();
    return concept.getAlternativeTerms().stream()
        .filter(t -> StringUtils.isBlank(t.getLang()) || StringUtils.equalsIgnoreCase(lang, t.getLang()))
        .map(VocabularyTerm::getTitle)
        .filter(StringUtils::isNotBlank)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();
  }

  /**
   * @return terms ordered by language (none first), then title
   */
  public List<VocabularyTerm> byLanguage(Collection<VocabularyTerm> terms) {
    return terms.stream()
        .sorted(Comparator.comparing((VocabularyTerm t) -> StringUtils.defaultString(t.getLang()).toLowerCase())
            .thenComparing(t -> StringUtils.defaultString(t.getTitle()), String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  /**
   * @return what the concept search matches: value, description and every label and alternative term, lower case
   */
  public String searchTextOf(VocabularyConcept concept) {
    return Stream.concat(Stream.of(concept.getIdentifier(), concept.getDescription()),
            concept.getTerms().stream().map(VocabularyTerm::getTitle))
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" "))
        .toLowerCase();
  }

  private void populateUsage() {
    extensionManager.list().stream()
        .sorted(Comparator.comparing(Extension::getTitle, String.CASE_INSENSITIVE_ORDER))
        .forEach(ext -> {
          List<ExtensionProperty> terms = ext.getProperties().stream()
              .filter(p -> p.getVocabulary() != null
                  && Objects.equals(p.getVocabulary().getUriString(), vocabulary.getUriString()))
              .toList();
          if (!terms.isEmpty()) {
            usedBy.add(new ExtensionUsage(ext, terms));
          }
        });
  }

  private void populateLanguages() {
    Map<String, Integer> counts = new HashMap<>();
    for (VocabularyConcept c : vocabulary.getConcepts()) {
      c.getPreferredTerms().stream()
          .map(t -> StringUtils.trimToNull(t.getLang()))
          .filter(Objects::nonNull)
          .map(String::toLowerCase)
          .distinct()
          .forEach(l -> counts.merge(l, 1, Integer::sum));
    }
    counts.entrySet().stream()
        .sorted(Comparator.comparing(e -> languageName(e.getKey()), String.CASE_INSENSITIVE_ORDER))
        .forEach(e -> labelLanguages.put(e.getKey(), e.getValue()));
  }

  /**
   * Compares the installed version with the latest registered one, as the extensions page does.
   */
  private void checkVersion() {
    try {
      // fetched first: the vocabulary is shared, so it isn't changed when the Registry can't be reached
      List<Vocabulary> registeredVocabularies = registryManager.getVocabularies();
      // one the Registry doesn't list counts as the latest
      Date newer = null;
      for (Vocabulary registered : registeredVocabularies) {
        if (registered.isLatest() && vocabulary.getUriString().equalsIgnoreCase(registered.getUriString())) {
          Date installed = vocabulary.getIssued();
          Date latest = registered.getIssued();
          if (latest != null && (installed == null || latest.after(installed))) {
            newer = latest;
          }
          break;
        }
      }
      vocabulary.setLatest(newer == null);
      newerIssued = newer;
      versionChecked = true;
    } catch (RegistryException e) {
      LOG.warn("Couldn't check the version of vocabulary {} with the Registry: {}", vocabulary.getUriString(), e.getMessage());
      registryUnreachable = true;
    }
  }

  /**
   * An installed extension and its terms taking their values from the vocabulary.
   */
  @Getter
  public static class ExtensionUsage {
    private final Extension extension;
    private final List<ExtensionProperty> terms;

    public ExtensionUsage(Extension extension, List<ExtensionProperty> terms) {
      this.extension = extension;
      this.terms = terms;
    }
  }
}
