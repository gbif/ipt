package org.gbif.ipt.action.portal;

import org.gbif.ipt.task.GenerateDCAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action to create the DCAT feed.
 */
public class DCATAction extends ActionSupport {

  private GenerateDCAT generateDCAT;

  // used to print the feed
  private InputStream dcatInfo;

  @Inject
  public DCATAction(GenerateDCAT generateDCAT) {
    this.generateDCAT = generateDCAT;
  }

  /**
   * Regenerates the DCAT feed.
   *
   * @return Struts2 result string
   */
  @Override
  public String execute() throws Exception {
    dcatInfo = new ByteArrayInputStream(generateDCAT.getFeed().getBytes(StandardCharsets.UTF_8));
    return SUCCESS;
  }

  /**
   * @return DCAT feed
   */
  public InputStream getDcatInfo() {
    return dcatInfo;
  }
}
