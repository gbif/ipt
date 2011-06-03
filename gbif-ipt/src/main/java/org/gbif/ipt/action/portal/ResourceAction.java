package org.gbif.ipt.action.portal;

import java.util.List;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.metadata.eml.Eml;

import com.google.inject.Inject;

public class ResourceAction extends PortalBaseAction {
  @Inject
  private RegistrationManager registrationManager;
  private List<Resource> resources;
  private Integer page = 1;

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Return the size of the DwC-A file.
   * 
   * @return
   */
  public String getDwcaFormattedSize() {
    String size = FileUtils.formatSize(resourceManager.getDwcaSize(resource), 0);
    return size;
  }

  public Eml getEml() {
    return resource.getEml();
  }

  /**
   * Return the size of the EML file.
   * 
   * @return
   */
  public String getEmlFormattedSize() {
    String size = FileUtils.formatSize(resourceManager.getEmlSize(resource), 0);
    return size;
  }

  public Ipt getIpt() {
    if (registrationManager.getIpt() == null) {
      return new Ipt();
    }
    return registrationManager.getIpt();
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Return the RTF size file format
   * 
   * @return
   */
  public String getRtfFormattedSize() {
    String size = FileUtils.formatSize(resourceManager.getRtfSize(resource), 0);
    return size;
  }

  public boolean isRtfFileExisting() {
    return resourceManager.isRtfExisting(resource.getShortname());
  }

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }
}
