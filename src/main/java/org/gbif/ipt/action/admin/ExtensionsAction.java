package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * The Action responsible for all user input relating to extension management.
 */
public class ExtensionsAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(ExtensionsAction.class);

  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private final RegistryManager registryManager;
  // list of latest registered extension versions
  private List<Extension> latestExtensionVersions;
  private List<Extension> extensions;
  private Extension extension;
  private String url;
  private Boolean updateVocabs = false;
  private int numVocabs = 0;
  private Date vocabsLastUpdated;
  private String dateFormat;
  private List<Extension> newExtensions;
  private ConfigWarnings warnings;

  @Inject
  public ExtensionsAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ExtensionManager extensionManager, VocabulariesManager vocabManager, RegistryManager registryManager,
    ConfigWarnings warnings) {
    super(textProvider, cfg, registrationManager);
    this.extensionManager = extensionManager;
    this.vocabManager = vocabManager;
    this.registryManager = registryManager;
    this.warnings = warnings;
  }

  @Override
  public String delete() throws Exception {
    try {
      extensionManager.delete(id);
      addActionMessage(getText("admin.extension.delete.success", new String[] {id}));
    } catch (DeletionNotAllowedException e) {
      addActionWarning(getText("admin.extension.delete.error", new String[] {id}));
      addActionExceptionWarning(e);
    }
    return SUCCESS;
  }

  public String update() throws Exception {
    // TODO
    LOG.info("Update extension " + id);
    return SUCCESS;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public Extension getExtension() {
    return extension;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public List<Extension> getNewExtensions() {
    return newExtensions;
  }

  public int getNumVocabs() {
    return numVocabs;
  }

  public Boolean getUpdateVocab() {
    return updateVocabs;
  }

  public Date getVocabsLastUpdated() {
    return vocabsLastUpdated;
  }

  /**
   * Handles the population of installed and uninstalled extensions on the "Core Types and Extensions" page.
   * Optionally, the user may have triggered an update vocabularies. This method always tries to pick up newly
   * registered extensions from the Registry.
   *
   * @return struts2 result
   */
  public String list() {
    if (updateVocabs) {
      UpdateResult result = vocabManager.updateAll();
      addActionMessage(
        getText("admin.extensions.vocabularies.updated", new String[] {String.valueOf(result.updated.size())}));
      addActionMessage(
        getText("admin.extensions.vocabularies.unchanged", new String[] {String.valueOf(result.unchanged.size())}));
      if (!result.errors.isEmpty()) {
        addActionWarning(
          getText("admin.extensions.vocabularies.errors", new String[] {String.valueOf(result.errors.size())}));
        for (Entry<String, String> err : result.errors.entrySet()) {
          addActionError(getText("admin.extensions.error.updating", new String[] {err.getKey(), err.getValue()}));
        }
      }
    }

    // retrieve all extensions that have been installed already
    extensions = extensionManager.list();

    // update each installed extension indicating whether it is the latest version (for its rowType) or not
    updateIsLatest(extensions);

    // populate list of uninstalled extensions, removing extensions installed already, showing only latest versions
    newExtensions = getLatestExtensionVersions();
    for (Extension e : extensions) {
      newExtensions.remove(e);
    }

    // find latest update date of all vocabularies
    List<Vocabulary> vocabs = vocabManager.list();
    numVocabs = vocabs.size();
    for (Vocabulary v : vocabs) {
      if (vocabsLastUpdated == null || vocabsLastUpdated.before(v.getLastUpdate())) {
        Locale locale = getLocale();
        vocabsLastUpdated = v.getLastUpdate();
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(vocabsLastUpdated);
      }
    }
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();

    // load latest extension versions from Registry
    loadLatestExtensionVersions();

    // ensure mandatory vocabs are always loaded
    vocabManager.load();

    if (id != null) {
      extension = extensionManager.get(id);
      if (extension == null) {
        // set notFound flag to true so POSTAction will return a NOT_FOUND 404 result name
        notFound = true;
      }
    }
  }

  /**
   * Iterate through list of installed extensions. Update each one, indicating if it is the latest version or not.
   */
  @VisibleForTesting
  protected void updateIsLatest(List<Extension> extensions) {
    if (!extensions.isEmpty()) {
      // complete list of registered extensions (latest and non-latest versions)
      List<Extension> registered = registryManager.getExtensions();
      for (Extension extension : extensions) {
        // is this the latest version?
        for (Extension rExtension : registered) {
          if (extension.getRowType() != null && rExtension.getRowType() != null) {
            String rowTypeOne = extension.getRowType();
            String rowTypeTwo = rExtension.getRowType();
            // first compare on rowType
            if (rowTypeOne.equalsIgnoreCase(rowTypeTwo)) {
              Date issuedOne = extension.getIssued();
              Date issuedTwo = rExtension.getIssued();
              // next compare on issued date: can both be null, or issued date must be same
              if ((issuedOne == null && issuedTwo == null) || (issuedOne != null && issuedTwo != null
                                                               && issuedOne.compareTo(issuedTwo) == 0)) {
                extension.setLatest(rExtension.isLatest());
              }
            }
          }
        }
        LOG.debug("Installed extension with rowType " + extension.getRowType() + " latest=" + extension.isLatest());
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
      if (!all.isEmpty()) {
        // list of latest extension versions
        setLatestExtensionVersions(getLatestVersions(all));
      }
    } catch (RegistryException e) {
      // add startup error message that explains why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), this);
      warnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = getText("admin.extensions.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    } finally {
      // initialize list as empty list if the list could not be populated
      if (getLatestExtensionVersions() == null) {
        setLatestExtensionVersions(new ArrayList<Extension>());
      }
    }
  }

  /**
   * Filter a list of extensions, returning the latest version for each rowType. The latest version of an extension
   * is determined by its issued date.
   *
   * @param extensions unfiltered list of all registered extensions
   *
   * @return filtered list of extensions
   */
  @VisibleForTesting
  protected List<Extension> getLatestVersions(List<Extension> extensions) {
    Ordering<Extension> byIssuedDate = Ordering.natural().nullsLast().onResultOf(new Function<Extension, Date>() {
      public Date apply(Extension extension) {
        return extension.getIssued();
      }
    });
    // sort extensions by issued date
    List<Extension> sorted = byIssuedDate.immutableSortedCopy(extensions);
    // populate list of latest extension versions
    Map<String, Extension> extensionsByRowtype = new HashMap<String, Extension>();
    if (!sorted.isEmpty()) {
      for (Extension extension: sorted) {
        String rowType = extension.getRowType();
        if (rowType != null && !extensionsByRowtype.containsKey(rowType)) {
          extensionsByRowtype.put(rowType, extension);
        }
      }
    }
    return new ArrayList<Extension>(extensionsByRowtype.values());
  }

  @Override
  public String save() {
    try {
      extensionManager.install(new URL(url));
      addActionMessage(getText("admin.extension.install.success", new String[] {url}));
    } catch (Exception e) {
      LOG.debug(e);
      addActionWarning(getText("admin.extension.install.error", new String[] {url}), e);
    }
    return SUCCESS;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setUpdateVocabs(String x) {
    if (StringUtils.trimToNull(x) != null) {
      this.updateVocabs = true;
    }
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return list of latest registered extensions
   */
  public List<Extension> getLatestExtensionVersions() {
    return latestExtensionVersions;
  }

  public void setLatestExtensionVersions(List<Extension> latestExtensionVersions) {
    this.latestExtensionVersions = latestExtensionVersions;
  }
}
