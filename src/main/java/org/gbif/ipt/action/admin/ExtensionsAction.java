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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * The Action responsible for all user input relating to extension management.
 */
public class ExtensionsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = -8804785290324044090L;

  private static final Logger LOG = LogManager.getLogger(ExtensionsAction.class);

  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private final RegistryManager registryManager;
  // all registered extensions (latest and older versions), loaded once per request in prepare()
  private List<Extension> registeredExtensions;
  // list of the latest registered extension versions
  @Getter
  private List<Extension> latestExtensionVersions;
  @Getter
  private List<Extension> extensions;
  @Getter
  private List<Vocabulary> vocabularies;
  @Getter
  private Map<String, List<ExtensionProperty>> propertiesByGroup = new HashMap<>();
  @Getter
  private Extension extension;
  private String url;
  private Boolean synchronise = false;
  @Getter
  private Date lastSynchronised;
  @Getter
  private List<Extension> newExtensions;
  private ConfigWarnings configWarnings;
  private ResourceManager resourceManager;
  // true if all installed core types and extensions (not vocabularies) are the latest version
  @Getter
  private boolean extensionsUpToDate = true;
  // true if the list of registered extensions or vocabularies couldn't be loaded from the Registry
  @Getter
  private boolean registryUnreachable;
  // issued date of the newer registered version, by row type, for installed extensions that aren't the latest
  @Getter
  private final Map<String, Date> newerIssued = new HashMap<>();
  // issued date of the newer registered version, by URI, for installed vocabularies that aren't the latest
  @Getter
  private final Map<String, Date> newerVocabularyIssued = new HashMap<>();
  // resources that map each installed extension, by row type
  @Getter
  private final Map<String, List<Resource>> usedBy = new HashMap<>();
  // short names of the core types each extension (installed or not) can be added to, by row type
  @Getter
  private final Map<String, List<String>> worksWith = new HashMap<>();
  // installed extensions that use each vocabulary, by vocabulary URI
  @Getter
  private final Map<String, List<Extension>> vocabularyUsedBy = new HashMap<>();
  // number of languages of each vocabulary's labels, by vocabulary URI
  @Getter
  private final Map<String, Integer> vocabularyLanguages = new HashMap<>();
  // extension page: the resources mapping the extension, with how many of its terms each maps, by resource title
  @Getter
  private final List<ResourceUsage> extensionUsage = new ArrayList<>();
  // extension page: the resources mapping each term, by qualified name
  @Getter
  private final Map<String, List<Resource>> termUsedBy = new HashMap<>();
  // extension page: number of terms with a label in each language, by language name
  @Getter
  private final Map<String, Integer> labelTranslations = new LinkedHashMap<>();

  @Inject
  public ExtensionsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ExtensionManager extensionManager,
      VocabulariesManager vocabManager,
      RegistryManager registryManager,
      ConfigWarnings configWarnings,
      ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.extensionManager = extensionManager;
    this.vocabManager = vocabManager;
    this.registryManager = registryManager;
    this.configWarnings = configWarnings;
    this.resourceManager = resourceManager;
  }

  @Override
  public String delete() throws Exception {
    try {
      // check if its used by some resources
      for (Resource r : resourceManager.list()) {
        if (!r.getMappings(id).isEmpty()) {
          LOG.warn("Extension mapped in resource {}", r.getShortname());
          String msg = getText("admin.extension.delete.error.mapped", new String[]{r.getShortname()});
          throw new DeletionNotAllowedException(DeletionNotAllowedException.Reason.EXTENSION_MAPPED, msg);
        }
      }
      extensionManager.uninstallSafely(id);
      addActionMessage(getText("admin.extension.delete.success", new String[]{id}));
    } catch (DeletionNotAllowedException e) {
      addActionWarning(getText("admin.extension.delete.error", new String[]{id}));
      addActionExceptionWarning(e);
    }
    return SUCCESS;
  }

  /**
   * Update installed extension to the latest version.
   * </br>
   * This involves migrating all associated resource mappings over to the new version.
   * </br>
   * If there are no associated resource mappings, the new version can simply be installed.
   *
   * @return struts2 result
   */
  @RequirePost
  public String update() throws Exception {
    // a blank or unknown id: prepare() found no installed extension
    if (notFound) {
      return NOT_FOUND;
    }
    try {
      LOG.info("Updating extension {} to latest version...", id);
      Extension installed = extensionManager.get(id);
      Extension latestVersion = extensionManager.update(id);
      List<Resource> migrated = new ArrayList<>();
      if (installed != null && latestVersion != null) {
        if (latestVersion.getUrl() != null) {
          // check if there are any associated resource mappings
          List<Resource> resourcesToMigrate = new ArrayList<>();
          for (Resource r : resourceManager.list()) {
            if (!r.getMappings(id).isEmpty()) {
              resourcesToMigrate.add(r);
            }
          }

          // if there are mappings to this extension - do migrations to latest version, save resources
          if (!resourcesToMigrate.isEmpty()) {
            for (Resource r : resourcesToMigrate) {
              LOG.info("Updating {} mappings for resource: {}...", id, r.getTitleAndShortname());
              extensionManager.migrateResourceToNewExtensionVersion(r, installed, latestVersion);
              resourceManager.save(r);
              migrated.add(r);
              LOG.info("Updated {} mappings successfully for resource: {}", id, r.getTitleAndShortname());
            }
          }
        }
      }

      String title = latestVersion != null ? latestVersion.getTitle() : id;
      if (migrated.isEmpty()) {
        addActionMessage(getText("admin.extension.update.success", new String[]{title}));
      } else {
        String names = migrated.stream().map(this::titleOf).collect(Collectors.joining(", "));
        addActionMessage(getText("admin.extension.update.success.resources", new String[]{title, names}));
      }
    } catch (Exception e) {
      LOG.error(e);
      addActionWarning(getText("admin.extension.update.error", new String[]{e.getMessage()}), e);
    }
    return SUCCESS;
  }

  /**
   * Handles the population of installed and uninstalled extensions on the "Core Types and Extensions" page.
   * This method always tries to pick up newly registered extensions from the Registry.
   * </br>
   * Optionally, the user may have triggered synchronise action, which updates default vocabularies to use the latest
   * versions, and synchronises all installed extensions and vocabularies with the registry to ensure their content
   * is up-to-date. Only with a POST request (the synchronise form), which is CSRF-checked, so a link can't trigger it.
   *
   * @return struts2 result
   */
  public String list() {
    if (synchronise && isHttpPost()) {
      try {
        synchronise();
        addActionMessage(getText("admin.extensions.synchronise.success"));
      } catch (Exception e) {
        String errorMsg = e.getMessage();
        if (e instanceof RegistryException) {
          errorMsg = RegistryException.logRegistryException(((RegistryException) e), this);
        }
        addActionWarning(getText("admin.extensions.synchronise.error", new String[]{errorMsg}));
        LOG.error(e);
      }
    }

    // retrieve all extensions that have been installed already
    extensions = extensionManager.list();
    extensions.sort(Comparator.comparing(Extension::getTitle));

    // retrieve all vocabularies
    vocabularies = vocabManager.list();
    vocabularies.sort(Comparator.comparing(Vocabulary::getTitle));

    // update each installed extension indicating whether it is the latest version (for its rowType) or not
    updateIsLatest(extensions);
    updateIsLatestVocabularies(vocabularies);

    // populate list of uninstalled extensions, removing extensions installed already, showing only latest versions
    newExtensions = getLatestExtensionVersions();
    for (Extension e : extensions) {
      newExtensions.remove(e);
    }

    newExtensions.sort(Comparator.comparing(Extension::getTitle));

    // find date extensions were last synchronised
    for (Extension ex : extensions) {
      if (lastSynchronised == null || lastSynchronised.before(ex.getModified())) {
        lastSynchronised = ex.getModified();
      }
    }

    populateUsage();

    return SUCCESS;
  }

  /**
   * Populates what the list page shows about use: the resources mapping each installed extension, the core types each
   * extension can be added to, and the installed extensions using each vocabulary.
   */
  private void populateUsage() {
    List<Resource> resources = resourceManager.list();
    for (Extension ext : extensions) {
      List<Resource> mapping = resources.stream()
          .filter(r -> !r.getMappings(ext.getRowType()).isEmpty())
          .sorted(Comparator.comparing(this::titleOf, String.CASE_INSENSITIVE_ORDER))
          .collect(Collectors.toList());
      usedBy.put(ext.getRowType(), mapping);

      for (Vocabulary v : ext.listVocabularies()) {
        if (v.getUriString() != null) {
          List<Extension> using = vocabularyUsedBy.computeIfAbsent(v.getUriString(), k -> new ArrayList<>());
          if (!using.contains(ext)) {
            using.add(ext);
          }
        }
      }
    }
    vocabularyUsedBy.values().forEach(l -> l.sort(Comparator.comparing(Extension::getTitle)));

    List<Extension> all = new ArrayList<>(extensions);
    all.addAll(newExtensions);
    all.forEach(this::putWorksWith);

    for (Vocabulary v : vocabularies) {
      long languages = v.getConcepts().stream()
          .flatMap(c -> c.getPreferredTerms().stream())
          .map(t -> StringUtils.trimToNull(t.getLang()))
          .filter(Objects::nonNull)
          .distinct()
          .count();
      vocabularyLanguages.put(v.getUriString(), (int) languages);
    }
  }

  private void putWorksWith(Extension ext) {
    if (ext.getRowType() != null && !ext.isCore()) {
      worksWith.put(ext.getRowType(), AppConfig.getCoreRowTypes().stream()
          .filter(core -> extensionManager.isSuitableFor(ext, core))
          .map(ExtensionsAction::shortName)
          .collect(Collectors.toList()));
    }
  }

  /**
   * The extension page: a GET with an installed extension's id.
   */
  @Override
  public String execute() throws Exception {
    String result = super.execute();
    if (INPUT.equals(result) && extension != null) {
      populateExtensionPage();
    }
    return result;
  }

  /**
   * Populates what the extension page shows besides the extension itself: whether it is the latest version, the core
   * types it can be added to, the resources mapping it and the terms each maps, and the languages of its labels.
   * A term counts as mapped when it has a source column or a default value, as in {@link ExtensionMapping#isMapped}.
   */
  void populateExtensionPage() {
    updateIsLatest(Collections.singletonList(extension));
    putWorksWith(extension);

    Set<String> terms = extension.getProperties().stream()
        .map(ExtensionProperty::getQualname)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    List<Resource> resources = new ArrayList<>(resourceManager.list());
    resources.sort(Comparator.comparing(this::titleOf, String.CASE_INSENSITIVE_ORDER));
    for (Resource r : resources) {
      List<ExtensionMapping> mappings = r.getMappings(extension.getRowType());
      if (mappings.isEmpty()) {
        continue;
      }
      // a resource can map the same extension more than once, e.g. from two sources
      Set<String> mapped = new HashSet<>();
      for (ExtensionMapping m : mappings) {
        for (PropertyMapping pm : m.getFields()) {
          if (pm.getTerm() != null && (pm.getIndex() != null || pm.getDefaultValue() != null)
              && terms.contains(pm.getTerm().qualifiedName())) {
            mapped.add(pm.getTerm().qualifiedName());
          }
        }
      }
      extensionUsage.add(new ResourceUsage(r, mapped.size()));
      mapped.forEach(t -> termUsedBy.computeIfAbsent(t, k -> new ArrayList<>()).add(r));
    }

    Map<String, Integer> counts = new HashMap<>();
    for (ExtensionProperty p : extension.getProperties()) {
      p.getTranslations().forEach((lang, t) -> {
        if (lang != null && t != null && StringUtils.isNotBlank(t.getLabel())) {
          counts.merge(lang, 1, Integer::sum);
        }
      });
    }
    counts.entrySet().stream()
        .sorted(Comparator.comparing(e -> languageName(e.getKey()), String.CASE_INSENSITIVE_ORDER))
        .forEach(e -> labelTranslations.put(e.getKey(), e.getValue()));
  }

  /**
   * @return a language's name in the interface language, e.g. French for fr; the code if Java doesn't know it
   */
  public String languageName(String code) {
    Locale locale = getLocale() != null ? getLocale() : Locale.ENGLISH;
    Locale language = Locale.forLanguageTag(code);
    String name = StringUtils.trimToNull(language.getDisplayLanguage(locale));
    // an unknown language comes back as its own code
    return name == null || name.equalsIgnoreCase(language.getLanguage()) ? code : StringUtils.capitalize(name);
  }

  /**
   * A resource mapping the extension shown on the extension page, with the number of its terms the resource maps.
   */
  @Getter
  public static class ResourceUsage {
    private final Resource resource;
    private final int termsMapped;

    public ResourceUsage(Resource resource, int termsMapped) {
      this.resource = resource;
      this.termsMapped = termsMapped;
    }
  }

  /**
   * @return the resource's title, or its shortname if it has none
   */
  public String titleOf(Resource r) {
    return StringUtils.defaultIfBlank(r.getTitle(), r.getShortname());
  }

  /**
   * @return the last part of a row type, e.g. Occurrence for http://rs.tdwg.org/dwc/terms/Occurrence
   */
  private static String shortName(String rowType) {
    int i = Math.max(rowType.lastIndexOf('/'), rowType.lastIndexOf('#'));
    return i >= 0 && i < rowType.length() - 1 ? rowType.substring(i + 1) : rowType;
  }

  /**
   * @return short names of the row types that can be cores, for the filters of the extensions not installed
   */
  public List<String> getCoreNames() {
    return AppConfig.getCoreRowTypes().stream().map(ExtensionsAction::shortName).distinct().collect(Collectors.toList());
  }

  /**
   * @return true if the IPT installs the vocabulary itself and keeps it at the latest version
   */
  public boolean isDefaultVocabulary(String uri) {
    return vocabManager.isDefault(uri);
  }

  @Override
  public void prepare() {
    super.prepare();

    // load the latest extension versions from Registry
    loadLatestExtensionVersions();

    // ensure mandatory vocabs are always loaded
    vocabManager.load();

    if (id != null) {
      extension = extensionManager.get(id);
      if (extension == null) {
        // set notFound flag to true so POSTAction will return a NOT_FOUND 404 result name
        notFound = true;
      } else {
        // groups in the order of the extension's definition
        propertiesByGroup = extension.getProperties().stream()
            .collect(Collectors.groupingBy(prop -> StringUtils.trimToEmpty(prop.getGroup()), LinkedHashMap::new, Collectors.toList()));
      }
    }
  }

  /**
   * Method used for 1) updating each extensions' isLatest field, and 2) for action logging (logging if at least
   * one extension is not up-to-date).
   * </br>
   * Works by iterating through list of installed extensions. Updates each one, indicating if it is the latest version
   * or not. Plus, updates boolean "extensionsUpToDate", set to false if there is at least one extension that is not up-to-date.
   */
  protected void updateIsLatest(List<Extension> extensions) {
    // the Registry already failed in prepare(): don't wait for it a second time, the versions show as not checked
    if (!extensions.isEmpty() && !registryUnreachable) {
      try {
        // complete list of registered extensions (latest and non-latest versions), as loaded in prepare()
        List<Extension> registered = registeredExtensions != null ? registeredExtensions : registryManager.getExtensions();
        for (Extension extension : extensions) {
          extension.setLatest(true);
          for (Extension rExtension : registered) {
            // check if registered extension is latest, and if it is, try to use it in comparison
            if (rExtension.isLatest() && extension.getRowType().equalsIgnoreCase(rExtension.getRowType())) {
              Date issuedOne = extension.getIssued();
              Date issuedTwo = rExtension.getIssued();
              if (issuedOne == null && issuedTwo != null) {
                extensionsUpToDate = false;
                newerIssued.put(extension.getRowType(), issuedTwo);
                extension.setLatest(false);
                LOG.debug("Installed extension with rowType {} has no issued date. A newer version issued {} exists.", extension.getRowType(), issuedTwo);
              } else if (issuedTwo != null && issuedTwo.compareTo(issuedOne) > 0) {
                extensionsUpToDate = false;
                newerIssued.put(extension.getRowType(), issuedTwo);
                extension.setLatest(false);
                LOG.debug("Installed extension with rowType {} was issued {}. A newer version issued {} exists.", extension.getRowType(), issuedOne, issuedTwo);
              } else {
                LOG.debug("Installed extension with rowType {} is the latest version", extension.getRowType());
              }
              break;
            }
          }
        }
      } catch (RegistryException e) {
        registryUnreachable = true;
        // add startup error message about Registry error
        String msg = RegistryException.logRegistryException(e, this);
        configWarnings.addStartupError(msg);
        LOG.error(msg);

        // add startup error message that explains the consequence of the Registry error
        msg = getText("admin.extensions.couldnt.load", new String[]{cfg.getRegistryUrl()});
        configWarnings.addStartupError(msg);
        LOG.error(msg);
      }
    }
  }

  protected void updateIsLatestVocabularies(List<Vocabulary> vocabularies) {
    if (!vocabularies.isEmpty()) {
      try {
        // complete list of registered vocabularies (latest and non-latest versions)
        List<Vocabulary> registered = registryManager.getVocabularies();
        for (Vocabulary vocabulary : vocabularies) {
          vocabulary.setLatest(true);
          for (Vocabulary rVocabulary : registered) {
            // check if registered vocabulary is latest, and if it is, try to use it in comparison
            if (rVocabulary.isLatest() && vocabulary.getUriString().equalsIgnoreCase(rVocabulary.getUriString())) {
              Date issuedOne = vocabulary.getIssued();
              Date issuedTwo = rVocabulary.getIssued();
              if (issuedOne == null && issuedTwo != null) {
                newerVocabularyIssued.put(vocabulary.getUriString(), issuedTwo);
                vocabulary.setLatest(false);
                LOG.debug("Installed vocabulary {} has no issued date. A newer version issued {} exists.", vocabulary.getUriString(), issuedTwo);
              } else if (issuedTwo != null && issuedTwo.compareTo(issuedOne) > 0) {
                newerVocabularyIssued.put(vocabulary.getUriString(), issuedTwo);
                vocabulary.setLatest(false);
                LOG.debug("Installed vocabulary {} was issued {}. A newer version issued {} exists.", vocabulary.getUriString(), issuedOne, issuedTwo);
              } else {
                LOG.debug("Installed vocabulary {} is the latest version", vocabulary.getUriString());
              }
              break;
            }
          }
        }
      } catch (RegistryException e) {
        registryUnreachable = true;
        // add startup error message about Registry error
        String msg = RegistryException.logRegistryException(e, this);
        configWarnings.addStartupError(msg);
        LOG.error(msg);

        // add startup error message that explains the consequence of the Registry error
        msg = getText("admin.extensions.vocabularies.couldnt.load", new String[]{cfg.getRegistryUrl()});
        configWarnings.addStartupError(msg);
        LOG.error(msg);
      }
    }
  }

  /**
   * Reload the list of registered extensions, loading only the latest extension versions.
   */
  private void loadLatestExtensionVersions() {
    try {
      // list of all registered extensions
      List<Extension> all = registryManager.getExtensions();
      registeredExtensions = all;
      if (!all.isEmpty()) {
        // list of latest extension versions
        setLatestExtensionVersions(getLatestVersions(all));
      }
    } catch (RegistryException e) {
      registryUnreachable = true;
      // add startup error message that explains why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      configWarnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = getText("admin.extensions.couldnt.load", new String[]{cfg.getRegistryUrl()});
      configWarnings.addStartupError(msg);
      LOG.error(msg);
    } finally {
      // initialize list as empty list if the list could not be populated
      if (getLatestExtensionVersions() == null) {
        setLatestExtensionVersions(new ArrayList<>());
      }
    }
  }

  /**
   * Filter a list of extensions, returning the latest version for each rowType. The latest version of an extension
   * is determined by its issued date.
   *
   * @param extensions unfiltered list of all registered extensions
   * @return filtered list of extensions
   */
  protected List<Extension> getLatestVersions(List<Extension> extensions) {
    // sort extensions by issued date, starting with latest issued
    List<Extension> sorted = extensions.stream()
        .sorted(Comparator.comparing(Extension::getIssued, Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();

    // populate list of latest extension versions
    Map<String, Extension> extensionsByRowtype = new HashMap<>();
    if (!sorted.isEmpty()) {
      for (Extension extension : sorted) {
        String rowType = extension.getRowType();
        if (rowType != null && !extensionsByRowtype.containsKey(rowType)) {
          extensionsByRowtype.put(rowType, extension);
        }
      }
    }

    return new ArrayList<>(extensionsByRowtype.values());
  }

  @Override
  public String save() {
    try {
      extensionManager.install(new URL(url));
      addActionMessage(getText("admin.extension.install.success", new String[]{url}));
    } catch (Exception e) {
      LOG.error(e);
      addActionWarning(getText("admin.extension.install.error", new String[]{url}), e);
    }
    return SUCCESS;
  }

  /**
   * Ensures the default installed vocabularies always use the latest version.
   * </br>
   * Then synchronises all installed extensions and vocabularies with registry to make sure their content is
   * up-to-date.
   *
   * @throws IOException            if an extension or vocabulary file cannot be downloaded
   * @throws RegistryException      if the list of registered extensions or vocabularies cannot be loaded from Registry
   * @throws InvalidConfigException if any of the extensions or vocabularies synchronised is invalid (e.g. bad URL)
   */
  private void synchronise() throws IOException, RegistryException, InvalidConfigException {
    LOG.info("Update default vocabularies to use latest versions...");
    vocabManager.installOrUpdateDefaults();

    LOG.info("Updating content of all installed vocabularies...");
    vocabManager.updateIfChanged();

    LOG.info("Updating content of all installed extensions...");
    extensionManager.updateIfChanged();
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public boolean getSynchronise() {
    return synchronise;
  }

  /**
   * @param synchronise form variable. Only acted on with a POST request, see {@link #list()}.
   */
  public void setSynchronise(String synchronise) {
    this.synchronise = StringUtils.trimToNull(synchronise) != null;
  }

  @StrutsParameter
  public void setSynchronise(boolean synchronise) {
    this.synchronise = synchronise;
  }

  @StrutsParameter
  public void setUrl(String url) {
    this.url = url;
  }

  public void setLatestExtensionVersions(List<Extension> latestExtensionVersions) {
    this.latestExtensionVersions = latestExtensionVersions;
  }


}
