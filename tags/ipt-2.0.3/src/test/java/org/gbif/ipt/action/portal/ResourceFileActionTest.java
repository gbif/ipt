package org.gbif.ipt.action.portal;

import org.gbif.utils.HttpUtil;

import java.io.File;
import java.net.URL;

import org.apache.http.StatusLine;
import org.junit.Ignore;
import org.junit.Test;

public class ResourceFileActionTest {

  @Test
  @Ignore
  /**
   * Manual test to tryout the conditional get archive download
   */
  public void testDwca() throws Exception {
    HttpUtil http = new HttpUtil();
    File down = new File("/Users/markus/Desktop/dwca.zip");
    URL url = new URL("http://localhost:7001/ipt/archive.do?r=condiget");
    boolean status = http.downloadIfChanged(url, down);
    System.out.println("Download success="+status);
  }
}
