package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Action responsible for all user input relating to extension management.
 */
public class ExtensionsAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(ExtensionsAction.class);

  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private final RegistryManager registryManager;
  // list of latest registered extension versions
  private List<Extension> latestExtensionVersions;
  private List<Extension> extensions;
  private Extension extension;
  private String url;
  private Boolean synchronise = false;
  private Date lastSynchronised;
  private List<Extension> newExtensions;
  private ConfigWarnings warnings;
  private boolean upToDate = true;

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
      extensionManager.uninstallSafely(id);
      addActionMessage(getText("admin.extension.delete.success", new String[] {id}));
    } catch (DeletionNotAllowedException e) {
      addActionWarning(getText("admin.extension.delete.error", new String[] {id}));
      addActionExceptionWarning(e);
    }
    return SUCCESS;
  }

  /**
   * Update installed extension to latest version.
   * </br>
   * This involves migrating all associated resource mappings over to the new version.
   * </br>
   * If there are no associated resource mappings, the new version can simply be installed.
   *
   * @return struts2 result
   */
  public String update() throws Exception {
    try {
      LOG.info("Updating extension " + id + " to latest version...");
      extensionManager.update(id);
      addActionMessage(getText("admin.extension.update.success", new String[] {id}));
    } catch (Exception e) {
      LOG.error(e);
      addActionWarning(getText("admin.extension.update.error", new String[] {e.getMessage()}), e);
    }
    return SUCCESS;
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

  /**
   * Handles the population of installed and uninstalled extensions on the "Core Types and Extensions" page.
   * This method always tries to pick up newly registered extensions from the Registry.
   * </br>
   * Optionally, the user may have triggered synchronise action, which updates default vocabularies to use latest
   * versions, and synchronises all installed extensions and vocabularies with the registry to ensure their content
   * is up-to-date.
   *
   * @return struts2 result
   */
  public String list() {
    if (synchronise) {
      try {
        synchronise();
        addActionMessage(getText("admin.extensions.synchronise.success"));
      } catch (Exception e) {
        String errorMsg = e.getMessage();
        if (e instanceof RegistryException) {
          errorMsg = RegistryException.logRegistryException(((RegistryException)e), this);
        }
        addActionWarning(getText("admin.extensions.synchronise.error", new String[] {errorMsg}));
        LOG.error(e);
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

    // find date extensions were last synchronised
    for (Extension ex : extensions) {
      if (lastSynchronised == null || lastSynchronised.before(ex.getModified())) {
        lastSynchronised = ex.getModified();
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
   * Method used for 1) updating each extensions' isLatest field, and 2) for action logging (logging if at least
   * one extension is not up-to-date).
   * </br>
   * Works by iterating through list of installed extensions. Updates each one, indicating if it is the latest version
   * or not. Plus, updates boolean "upToDate", set to false if there is at least one extension that is not up-to-date.
   */
  @VisibleForTesting
  protected void updateIsLatest(List<Extension> extensions) {
    if (!extensions.isEmpty()) {
      try {
        // complete list of registered extensions (latest and non-latest versions)
        List<Extension> registered = registryManager.getExtensions();
        for (Extension extension : extensions) {
          extension.setLatest(true);
          for (Extension rExtension : registered) {
            // check if registered extension is latest, and if it is, try to use it in comparison
            if (rExtension.isLatest() && extension.getRowType().equalsIgnoreCase(rExtension.getRowType())) {
              Date issuedOne = extension.getIssued();
              Date issuedTwo = rExtension.getIssued();
              if (issuedOne == null && issuedTwo != null) {
                setUpToDate(false);
                extension.setLatest(false);
                LOG.debug("Installed extension with rowType " + extension.getRowType() + " has no issued date. A newer version issued " + issuedTwo.toString() + " exists.");
              } else if (issuedTwo != null && issuedTwo.compareTo(issuedOne) > 0) {
                setUpToDate(false);
                extension.setLatest(false);
                LOG.debug("Installed extension with rowType " + extension.getRowType() + " was issued " + issuedOne.toString() + ". A newer version issued " + issuedTwo.toString() + " exists.");
              } else {
                LOG.debug("Installed extension with rowType " + extension.getRowType() + " is the latest version");
              }
              break;
            }
          }
        }
        // warn user if updates to installed extensions are available
        if (isUpToDate()) {
          addActionMessage(getText("admin.extensions.upToDate"));
        } else {
          addActionWarning(getText("admin.extensions.not.upToDate"));
        }
      } catch (RegistryException e) {
        // add startup error message about Registry error
        String msg = RegistryException.logRegistryException(e, this);
        warnings.addStartupError(msg);
        LOG.error(msg);

        // add startup error message that explains the consequence of the Registry error
        msg = getText("admin.extensions.couldnt.load", new String[] {cfg.getRegistryUrl()});
        warnings.addStartupError(msg);
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
      if (!all.isEmpty()) {
        // list of latest extension versions
        setLatestExtensionVersions(getLatestVersions(all));
      }
    } catch (RegistryException e) {
      // add startup error message that explains why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
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
    Ordering<Extension> byIssuedDate = Ordering.natural().nullsFirst().onResultOf(new Function<Extension, Date>() {
      @Override
      public Date apply(Extension extension) {
        return extension.getIssued();
      }
    });
    // sort extensions by issued date, starting with latest issued
    List<Extension> sorted = byIssuedDate.immutableSortedCopy(extensions).reverse();
    // populate list of latest extension versions
    Map<String, Extension> extensionsByRowtype = new HashMap<String, Extension>();
    if (!sorted.isEmpty()) {
      for (Extension extension : sorted) {
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
      LOG.error(e);
      addActionWarning(getText("admin.extension.install.error", new String[] {url}), e);
    }
    return SUCCESS;
  }

  /**
   * Ensures the default installed vocabularies always use the latest version.
   * </br>
   * Then synchronises all installed extensions and vocabularies with registry to make sure their content is
   * up-to-date.
   *
   * @throws IOException if an extension or vocabulary file cannot be downloaded
   * @throws RegistryException if the list of registered extensions or vocabularies cannot be loaded from Registry
   * @throws InvalidConfigException if any of the extensions or vocabularies synchronised is invalid (e.g. bad URL)
   */
  private void synchronise() throws IOException, RegistryException, InvalidConfigException {
    LOG.info("Update default vocabularies to use latest versions...");
    vocabManager.installOrUpdateDefaults();

    LOG.info("Updating content of all installed vocabularies...");
    for (Vocabulary v : vocabManager.list()) {
      LOG.debug("Updating vocabulary " + v.getUriString());
      vocabManager.updateIfChanged(v.getUriString());
    }

    LOG.info("Updating content of all installed extensions...");
    for (Extension ex : extensionManager.list()) {
      LOG.debug("Updating extension " + ex.getRowType());
      extensionManager.updateIfChanged(ex.getRowType());
    }
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  /**
   * To hold the state transition request, so the same request triggered purely by a URL will not work.
   *
   * @param synchronise form variable
   */
  public void setSynchronise(String synchronise) {
    this.synchronise = StringUtils.trimToNull(synchronise) != null;
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

  /**
   * @return true if all installed extensions are the latest version, false otherwise
   */
  public boolean isUpToDate() {
    return upToDate;
  }

  public void setUpToDate(boolean upToDate) {
    this.upToDate = upToDate;
  }
}
