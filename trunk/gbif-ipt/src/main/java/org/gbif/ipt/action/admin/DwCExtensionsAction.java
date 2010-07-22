/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.admin.DwCExtensionManager;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.Gbrds.ExtensionApi;
import org.gbif.registry.api.client.GbrdsExtension;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The Action responsible for all user input relating to the DarwinCore extension management.
 * 
 * @author tim
 */
public class DwCExtensionsAction extends POSTAction {
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
  private DwCExtensionManager extensionManager;
  @Inject
  private RegisteredExtensions registered;
  private List<Extension> extensions;
  private Extension extension;
  private String url;

  @Override
  public String delete() {
    extensionManager.delete(id);
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

  public void install() throws Exception {
    URL extensionURL;
    try {
      extensionURL = new URL(url);
      extensionManager.install(extensionURL);
      addActionMessage(getText("admin.extension.install.success", id));
    } catch (Exception e) {
      log.debug(e);
      System.out.println(getText("admin.extension.install.error", new String[]{id}));
      addActionError(getText("admin.extension.install.error", new String[]{id}));
    }
  }

  public String list() {
    extensions = extensionManager.list();
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
      install();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return SUCCESS;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
