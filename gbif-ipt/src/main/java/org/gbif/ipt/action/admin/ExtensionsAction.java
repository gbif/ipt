/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.Gbrds.ExtensionApi;
import org.gbif.registry.api.client.GbrdsExtension;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    public List<GbrdsExtension> extensions = new ArrayList<GbrdsExtension>();
    private Gbrds client;

    @Inject
    public RegisteredExtensions(Gbrds client) {
      super();
      this.client = client;
    }

    public void load() throws RuntimeException {
      ExtensionApi api = client.getExtensionApi();
      extensions = api.list().execute().getResult();
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

  @Override
  public String delete() {
    extensionManager.delete(id);
    addActionMessage(getText("admin.extension.delete.success", new String[]{id}));
    return SUCCESS;
  }

  public Extension getExtension() {
    return extension;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public List<GbrdsExtension> getGbrdsExtensions() {
    List<GbrdsExtension> newExts = new ArrayList<GbrdsExtension>();
    // only add non installed ones
    for (GbrdsExtension e : registered.extensions) {
      if (extensionManager.get(e.getRowType()) == null) {
        newExts.add(e);
      }
    }
    return newExts;
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
      addActionMessage(result.updated.size() + " vocabularies have been updated");
      addActionMessage(result.unchanged.size() + " vocabularies have not been modifed since last download");
      if (!result.errors.isEmpty()) {
        addActionError(result.errors.size() + " vocabularies produced an error when updating. Please consult logs!");
      }
    }

    extensions = extensionManager.list();
    Collection<Vocabulary> vocabs = vocabManager.list();
    numVocabs = vocabs.size();
    // find latest update data of any of all vocabularies
    for (Vocabulary v : vocabs) {
      if (vocabsLastUpdated == null || vocabsLastUpdated.before(v.getLastUpdate())) {
        vocabsLastUpdated = v.getLastUpdate();
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
        addActionError("Couldnt load registered extensions: " + e.getMessage());
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
      System.out.println(getText("admin.extension.install.error", new String[]{url}));
      addActionError(getText("admin.extension.install.error", new String[]{url}));
    }
    return SUCCESS;
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
