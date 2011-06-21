/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * The Action responsible for all user input relating to the DarwinCore extension management.
 * 
 * @author tim
 */
public class ExtensionsAction extends POSTAction {
  /**
   * A session scoped bean to keep a list of all extensions with basic metadata as exposed by the registry directly.
   * There wont be any properties listed. The reason for keeping this in the session is to load the extension list only
   * once - but not to store it continuesly in memory. Once the admin has logged out all this info will be removed again
   * and only the installed extensions remain in memory.
   * 
   * @author markus
   */
  @SessionScoped
  public static class RegisteredExtensions {
    // public List<GbrdsExtension> extensions = new ArrayList<GbrdsExtension>();
    public List<Extension> extensions = new ArrayList<Extension>();
    // private Gbrds client;
    private RegistryManager registryManager;

    @Inject
    public RegisteredExtensions(RegistryManager registryManager) {
      super();
      this.registryManager = registryManager;
    }

    public void load() throws RuntimeException {
      extensions = registryManager.getExtensions();
    }
  }

  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private VocabulariesManager vocabManager;
  @Inject
  private RegisteredExtensions registered;
  private List<Extension> extensions;
  private Extension extension;
  private String url;
  private Boolean updateVocabs = false;
  private int numVocabs = 0;
  private Date vocabsLastUpdated = null;
  private String dateFormat = null;
  private ArrayList<Extension> newExtensions;

  @Override
  public String delete() throws Exception {
    try {
      extensionManager.delete(id);
      addActionMessage(getText("admin.extension.delete.success", new String[]{id}));
    } catch (DeletionNotAllowedException e) {
      addActionWarning(getText("admin.extension.delete.error", new String[]{id}));
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

  public ArrayList<Extension> getNewExtensions() {
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

  public String list() {
    if (updateVocabs) {
      UpdateResult result = vocabManager.updateAll();
      addActionMessage(getText("admin.extensions.vocabularies.updated", new String[]{result.updated.size() + ""}));
      addActionMessage(getText("admin.extensions.vocabularies.unchanged", new String[]{result.unchanged.size() + ""}));
      if (!result.errors.isEmpty()) {
        addActionWarning(getText("admin.extensions.vocabularies.errors", new String[]{result.errors.size() + ""}));
        for (Entry<String, String> err : result.errors.entrySet()) {
          addActionError(getText("admin.extensions.error.updating", new String[]{err.getKey(), err.getValue()}));
        }
      }
    }

    extensions = extensionManager.list();
    newExtensions = new ArrayList<Extension>(registered.extensions);
    // remove allready installed ones
    for (Extension e : extensions) {
      newExtensions.remove(e);
    }
    Collection<Vocabulary> vocabs = vocabManager.list();
    numVocabs = vocabs.size();
    // find latest update data of any of all vocabularies
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
  public void prepare() throws Exception {
    super.prepare();
    // in case session just started
    if (registered.extensions.isEmpty()) {
      try {
        registered.load();
      } catch (Exception e) {
        log.error("Couldnt load registered extensions", e);
        addActionWarning(getText("admin.extensions.couldnt.load", new String[]{e.getMessage()}));
      }
    }
    if (id != null) {
      extension = extensionManager.get(id);
      if (extension == null) {
        // set notFound flag to true so POSTAction will return a NOT_FOUND 404 result name
        notFound = true;
      }
    }
  }

  @Override
  public String save() {
    try {
      extensionManager.install(new URL(url));
      addActionMessage(getText("admin.extension.install.success", new String[]{url}));
    } catch (Exception e) {
      log.debug(e);
      addActionWarning(getText("admin.extension.install.error", new String[]{url}), e);
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

}
