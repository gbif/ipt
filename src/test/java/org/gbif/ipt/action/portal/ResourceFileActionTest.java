package org.gbif.ipt.action.portal;

import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

public class ResourceFileActionTest {

  /**
   * Manual test to tryout the conditional get archive download
   */
  @Test
  @Ignore
  public void testDwca() throws Exception {
    HttpClient client = new HttpClient(HttpUtil.newMultithreadedClient(1000, 1, 1));
    File down = new File("/Users/markus/Desktop/dwca.zip");
    URL url = new URL("http://localhost:7001/ipt/archive.do?r=condiget");
    boolean status = client.downloadIfChanged(url, down);
    System.out.println("Download success=" + status);
  }
}
