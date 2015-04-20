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
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

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
  // list of all registered extensions
  private List<Extension> registeredExtensions;
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
    ExtensionManager extensionManager, VocabulariesManager vocabManager,
    RegistryManager registryManager, ConfigWarnings warnings) {
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
      addActionMessage(getText("admin.extensions.vocabularies.updated",
        new String[] {String.valueOf(result.updated.size())}));
      addActionMessage(getText("admin.extensions.vocabularies.unchanged",
        new String[] {String.valueOf(result.unchanged.size())}));
      if (!result.errors.isEmpty()) {
        addActionWarning(getText("admin.extensions.vocabularies.errors",
          new String[] {String.valueOf(result.errors.size())}));
        for (Entry<String, String> err : result.errors.entrySet()) {
          addActionError(getText("admin.extensions.error.updating", new String[] {err.getKey(), err.getValue()}));
        }
      }
    }

    // retrieve all extensions that have been installed already
    extensions = extensionManager.list();

    // populate list of uninstalled extensions, removing extensions installed already
    newExtensions = getRegisteredExtensions();
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

    // load all registered extensions from registry
    loadRegisteredExtensions();

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
   * Reload all the list of registered extensions.
   */
  private void loadRegisteredExtensions() {
    try {
      setRegisteredExtensions(registryManager.getExtensions());
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), this);
      // add startup error message about Registry error
      warnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = getText("admin.extensions.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    } finally {
      // initialize list as empty list had the list not been populated
      if (getRegisteredExtensions() == null) {
        setRegisteredExtensions(new ArrayList<Extension>());
      }
    }
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

  public List<Extension> getRegisteredExtensions() {
    return registeredExtensions;
  }

  public void setRegisteredExtensions(List<Extension> registeredExtensions) {
    this.registeredExtensions = registeredExtensions;
  }
}
