package org.gbif.ipt.action.portal;

import org.gbif.ipt.task.GenerateDCAT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Action to create the DCAT feed.
 */
public class DCATAction extends ActionSupport {

  // logging
  private static final Logger LOG = LogManager.getLogger(DCATAction.class);

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
    Closer closer = Closer.create();
    try {
      dcatInfo = closer.register(new ByteArrayInputStream(generateDCAT.getFeed().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error generating DCAT feed: " + e.getMessage(), e);
    } finally {
      try {
        closer.close();
      } catch (IOException e) {
        LOG.error("Failed to close input stream on DCAT feed", e);
      }
    }
    return SUCCESS;
  }

  /**
   * @return DCAT feed
   */
  public InputStream getDcatInfo() {
    return dcatInfo;
  }
}
