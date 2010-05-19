/**
 * 
 */
package org.gbif.provider.util;

import static org.junit.Assert.assertNotNull;

import org.gbif.file.FileUtils;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.util.ArchiveUtil.Request;
import org.gbif.provider.util.ArchiveUtil.Response;

import java.io.File;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author eighty
 * 
 */
public class ArchiveUtilTest extends ResourceTestBase {

  @Autowired
  private ArchiveUtil archiveUtil;

  @SuppressWarnings("unchecked")
  @Test
  public void testProcess() {
    File location = FileUtils.getClasspathFile("dwc-archives/unit-testing/1.zip");
    OccurrenceResource resource = new OccurrenceResource();
    occResourceManager.save(resource);
    resource.getExtensionMappingsMap().clear();
    Request<OccurrenceResource> request = Request.with(location, resource);
    Response<OccurrenceResource> response = archiveUtil.init(request).process();
    resource = response.getResource();
    assertNotNull(resource);
  }
}
