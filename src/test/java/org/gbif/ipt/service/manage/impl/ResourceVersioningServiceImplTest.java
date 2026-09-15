/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.math.BigDecimal;
import java.util.Date;

import org.gbif.ipt.service.manage.ResourceVersioningService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResourceVersioningServiceImplTest {

  private final DataDir mockedDataDir = MockDataDir.buildMock();

  private final ResourceVersioningService resourceVersioningService = new ResourceVersioningServiceImpl(mockedDataDir);

  @Test
  public void testConvertVersion() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(BigDecimal.valueOf(4));
    assertEquals(0, r.getEmlVersion().scale());
    assertEquals(4, r.getEmlVersion().intValueExact());
    // do conversion 4 -> 4.0
    BigDecimal converted = resourceVersioningService.convertVersion(r);
    assertEquals(new BigDecimal("4.0"), converted);
    // ensure conversions aren't repeated
    r.setMetadataVersion(converted);
    assertNull(resourceVersioningService.convertVersion(r));
  }

  @Test
  public void testConvertVersionZero() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(BigDecimal.valueOf(0));
    assertEquals(0, r.getEmlVersion().scale());
    assertEquals(0, r.getEmlVersion().intValueExact());
    // do conversion 0 -> 1.0
    BigDecimal converted = resourceVersioningService.convertVersion(r);
    assertEquals(new BigDecimal("1.0"), converted);
    // ensure conversions aren't repeated
    r.setMetadataVersion(converted);
    assertNull(resourceVersioningService.convertVersion(r));
  }

  @Test
  public void testConstructVersionHistoryForLastPublishedVersion() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(new BigDecimal("4.0"));
    r.setStatus(PublicationStatus.PUBLIC);
    r.setRecordsPublished(100);
    Date lastPublished = new Date();
    r.setLastPublished(lastPublished);

    VersionHistory history = resourceVersioningService.constructVersionHistoryForLastPublishedVersion(r);
    assertNotNull(history);
    assertEquals("4.0", history.getVersion());
    assertEquals(lastPublished, history.getReleased());
    assertEquals(PublicationStatus.PUBLIC, history.getPublicationStatus());
    assertEquals(100, history.getRecordsPublished());

    // properties aren't set
    assertNull(history.getDoi());
    assertNull(history.getStatus());
    assertNull(history.getChangeSummary());
    assertNull(history.getModifiedBy());

    // next version?
    assertEquals("4.1", r.getNextVersion().toPlainString());
  }
}
