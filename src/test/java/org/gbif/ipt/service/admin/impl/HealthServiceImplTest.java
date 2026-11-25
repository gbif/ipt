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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.HealthService.Level;
import org.gbif.ipt.service.admin.HealthService.LocalChecks;
import org.gbif.ipt.service.admin.HealthService.RemoteChecks;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.gbif.ipt.config.DataDir.DirStatus.READ_ONLY;
import static org.gbif.ipt.config.DataDir.DirStatus.READ_WRITE;

public class HealthServiceImplTest {

  private static final long GB = FileUtils.ONE_GB;

  private static final long MB = FileUtils.ONE_MB;

  @Test
  public void testDiskLevelSmallResources() {
    // a few GB are plenty when the resources are small
    assertEquals("ok", HealthServiceImpl.diskLevel(3 * GB, 50 * MB));
    // the floors still apply: logs, uploads, resources not published yet
    assertEquals("warn", HealthServiceImpl.diskLevel(GB, 50 * MB));
    assertEquals("bad", HealthServiceImpl.diskLevel(400 * MB, 0));
  }

  @Test
  public void testDiskLevelLargeResource() {
    // 300 GB free on a 2 TB disk (85% used) is plenty
    assertEquals("ok", HealthServiceImpl.diskLevel(300 * GB, 30 * GB));
    // room for one more publication of the largest resource
    assertEquals("warn", HealthServiceImpl.diskLevel(45 * GB, 30 * GB));
    // not even that
    assertEquals("bad", HealthServiceImpl.diskLevel(15 * GB, 30 * GB));
  }

  @Test
  public void testDiskLevelThresholds() {
    long need = 5 * GB;
    assertEquals("ok", HealthServiceImpl.diskLevel(2 * need, need));
    assertEquals("warn", HealthServiceImpl.diskLevel(2 * need - 1, need));
    assertEquals("warn", HealthServiceImpl.diskLevel(need, need));
    assertEquals("bad", HealthServiceImpl.diskLevel(need - 1, need));
    assertEquals("ok", HealthServiceImpl.diskLevel(HealthServiceImpl.DISK_LOW_FLOOR_BYTES, 0));
    assertEquals("warn", HealthServiceImpl.diskLevel(HealthServiceImpl.DISK_LOW_FLOOR_BYTES - 1, 0));
    assertEquals("bad", HealthServiceImpl.diskLevel(HealthServiceImpl.DISK_FULL_FLOOR_BYTES - 1, 0));
  }

  @Test
  public void testPublishingNeedCountsFileSourcesAndLastArchive(@TempDir File tmp) throws Exception {
    Fixture f = new Fixture();
    Resource resource = new Resource();
    resource.setShortname("big");
    TextFileSource text = new TextFileSource();
    text.setName("occurrences");
    text.setFileSize(3 * GB);
    resource.addSource(text, false);
    // SQL sources have no known size
    SqlSource sql = new SqlSource();
    sql.setName("db");
    resource.addSource(sql, false);
    assertEquals(3 * GB, f.service.publishingNeed(resource));

    // the last published archive counts too
    VersionHistory published = new VersionHistory(new BigDecimal("1.2"), new Date(), PublicationStatus.PUBLIC);
    resource.addVersionHistory(published);
    File archive = new File(tmp, "dwca-1.2.zip");
    Files.write(archive.toPath(), new byte[1000]);
    when(f.dataDir.resourceArchiveFile(resource, new BigDecimal("1.2"))).thenReturn(archive);
    assertEquals(3 * GB + 1000, f.service.publishingNeed(resource));
  }

  private static final String URL = "https://ipt.example.org/ipt";

  private static ExtendedResponse response(int statusCode, String content) {
    HttpResponse httpResponse = mock(HttpResponse.class);
    when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, ""));
    ExtendedResponse resp = new ExtendedResponse(httpResponse);
    resp.setContent(content);
    return resp;
  }

  @Test
  public void testPublicAccessReachable() {
    HealthServiceImpl.PublicAccessCheck check = HealthServiceImpl.readPublicAccessResponse(
        response(200, "{\"success\":true,\"status\":200,\"url\":\"" + URL + "\"}"), URL);
    assertTrue(check.checked());
    assertTrue(check.reachable());
  }

  @Test
  public void testPublicAccessNotReachable() {
    // the validator ran but couldn't open the IPT
    HealthServiceImpl.PublicAccessCheck check = HealthServiceImpl.readPublicAccessResponse(
        response(200, "{\"success\":false,\"error\":\"IOException\",\"url\":\"" + URL + "\"}"), URL);
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertEquals("IOException", check.message());

    // the IPT answered the validator with an error status
    check = HealthServiceImpl.readPublicAccessResponse(response(200, "{\"success\":false,\"status\":503}"), URL);
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertEquals("503", check.status());

    // validurl-ws refuses localhost and private network addresses
    check = HealthServiceImpl.readPublicAccessResponse(
        response(200, "{\"success\":false,\"error\":\"NonPublicAddress\",\"url\":\"http://localhost:8080\"}"),
        "http://localhost:8080");
    assertTrue(check.checked());
    assertFalse(check.reachable());
    assertTrue(check.message().contains("private network"));
  }

  @Test
  public void testPublicAccessNotCheckedWhenValidatorFails() {
    HealthServiceImpl.PublicAccessCheck check = HealthServiceImpl.readPublicAccessResponse(null, URL);
    assertFalse(check.checked());
    assertEquals("NO_RESPONSE", check.status());

    check = HealthServiceImpl.readPublicAccessResponse(response(502, "Bad Gateway"), URL);
    assertFalse(check.checked());
    assertEquals("502", check.status());

    check = HealthServiceImpl.readPublicAccessResponse(response(200, "<html>maintenance</html>"), URL);
    assertFalse(check.checked());
    assertFalse(check.reachable());
    assertEquals("BAD_RESPONSE", check.status());
  }

  private static LocalChecks local(String diskLevel, DataDir.DirStatus subResources) {
    return new LocalChecks(100 * GB, 50 * GB, diskLevel, GB, "big", READ_WRITE, READ_WRITE, READ_WRITE, READ_WRITE, subResources);
  }

  private static RemoteChecks remote(boolean registry, boolean repository, boolean publicAccess) {
    return new RemoteChecks("https://api.gbif.org", registry, "http://rs.gbif.org", repository, URL, true,
        publicAccess, "", "", 0);
  }

  @Test
  public void testLevelLocalProblemsAreBad() {
    assertEquals(Level.OK, HealthServiceImpl.level(local("ok", READ_WRITE), remote(true, true, true), true));
    assertEquals(Level.BAD, HealthServiceImpl.level(local("ok", READ_ONLY), null, false));
    assertEquals(Level.BAD, HealthServiceImpl.level(local("bad", READ_WRITE), null, false));
    // a directory problem wins over a remote warning
    assertEquals(Level.BAD, HealthServiceImpl.level(local("ok", READ_ONLY), remote(false, true, true), true));
    assertEquals(Level.WARN, HealthServiceImpl.level(local("warn", READ_WRITE), null, false));
  }

  @Test
  public void testLevelRemoteProblemsAreWarnings() {
    assertEquals(Level.WARN, HealthServiceImpl.level(local("ok", READ_WRITE), remote(false, true, true), false));
    assertEquals(Level.WARN, HealthServiceImpl.level(local("ok", READ_WRITE), remote(true, false, true), false));
    // before the first remote checks only the local ones count
    assertEquals(Level.OK, HealthServiceImpl.level(local("ok", READ_WRITE), null, true));
  }

  @Test
  public void testPublicAccessCountsOnlyWhenRegistered() {
    // an IPT on a local network can't be reached from the internet, and doesn't need to be until it's registered
    assertEquals(Level.OK, HealthServiceImpl.level(local("ok", READ_WRITE), remote(true, true, false), false));
    assertEquals(Level.WARN, HealthServiceImpl.level(local("ok", READ_WRITE), remote(true, true, false), true));
  }

  @Test
  public void testNextDelayBacksOff() {
    long passed = HealthServiceImpl.nextDelay(0);
    assertTrue(passed >= TimeUnit.HOURS.toMillis(12) && passed < TimeUnit.HOURS.toMillis(13));
    assertEquals(TimeUnit.MINUTES.toMillis(5), HealthServiceImpl.nextDelay(1));
    assertEquals(TimeUnit.MINUTES.toMillis(15), HealthServiceImpl.nextDelay(2));
    assertEquals(TimeUnit.HOURS.toMillis(1), HealthServiceImpl.nextDelay(3));
    // then hourly until the checks pass
    assertEquals(TimeUnit.HOURS.toMillis(1), HealthServiceImpl.nextDelay(4));
    assertEquals(TimeUnit.HOURS.toMillis(1), HealthServiceImpl.nextDelay(50));
  }

  private static class Fixture {
    final AppConfig cfg = mock(AppConfig.class);
    final DataDir dataDir = mock(DataDir.class);
    final HttpClient http = mock(HttpClient.class);
    final RegistrationManager registration = mock(RegistrationManager.class);
    final ResourceManager resources = mock(ResourceManager.class);
    final AtomicLong now = new AtomicLong(1_000_000_000L);
    final HealthServiceImpl service = new HealthServiceImpl(cfg, dataDir, http, registration, resources);

    Fixture() throws Exception {
      service.clock = now::get;
      when(dataDir.isConfigured()).thenReturn(true);
      when(cfg.getBaseUrl()).thenReturn(URL);
      when(cfg.getRegistryUrl()).thenReturn("https://api.gbif.org");
      ExtendedResponse ok = response(200, "{\"success\":true}");
      when(http.get(anyString())).thenReturn(ok);
    }
  }

  @Test
  public void testPageViewsReuseRecentChecks() throws Exception {
    Fixture f = new Fixture();
    RemoteChecks first = f.service.refreshRemoteChecks(false);
    // registry, repository, validator
    verify(f.http, times(3)).get(anyString());
    assertTrue(first.registry() && first.repository() && first.publicAccess());

    f.now.addAndGet(TimeUnit.MINUTES.toMillis(4));
    assertSame(first, f.service.refreshRemoteChecks(false));
    verify(f.http, times(3)).get(anyString());

    // an admin asking to check again
    f.service.refreshRemoteChecks(true);
    verify(f.http, times(6)).get(anyString());

    f.now.addAndGet(TimeUnit.MINUTES.toMillis(6));
    f.service.refreshRemoteChecks(false);
    verify(f.http, times(9)).get(anyString());
  }

  @Test
  public void testNoChecksBeforeSetup() throws Exception {
    Fixture f = new Fixture();
    when(f.dataDir.isConfigured()).thenReturn(false);
    assertNull(f.service.refreshRemoteChecks(true));
    verify(f.http, never()).get(anyString());
    assertEquals(Level.OK, f.service.getLevel());
  }

  @Test
  public void testLevelUsesCachedRemoteChecks() throws Exception {
    Fixture f = new Fixture();
    when(f.dataDir.getDataDirUsableSpace()).thenReturn(50 * GB);
    when(f.dataDir.getDirectoryReadWriteStatus(any())).thenReturn(READ_WRITE);
    when(f.dataDir.getSubDirectoriesReadWriteStatus(any())).thenReturn(READ_WRITE);
    when(f.registration.getIpt()).thenReturn(new Ipt());
    ExtendedResponse notReachable = response(200, "{\"success\":false,\"error\":\"IOException\"}");
    when(f.http.get(anyString())).thenReturn(notReachable);

    // no remote checks yet: the menu never calls GBIF itself
    assertEquals(Level.OK, f.service.getLevel());
    verify(f.http, never()).get(anyString());

    // the registered IPT isn't reachable from the internet
    f.service.refreshRemoteChecks(true);
    assertEquals(Level.WARN, f.service.getLevel());
  }
}
