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
package org.gbif.ipt.config;

import org.gbif.ipt.model.PublicationOptions;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishingMonitorTest {

  @Test
  void monitorSendsPublicationFailureEmailWhenEnabledAndConfigured() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    when(cfg.getAdminEmail()).thenReturn("admin@example.org");
    when(cfg.getBaseUrl()).thenReturn("https://ipt.example.org");

    Resource resource = new Resource();
    resource.setShortname("birds");
    resource.setTitle("Birds");
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
    resource.setUpdateFrequency("daily");
    resource.setNextPublished(new Date(System.currentTimeMillis() - 1000));
    resource.setNotifyPublicationFailure(true);

    ResourceManager resourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourcePublicationManager = mock(ResourcePublicationManager.class);
    when(resourcePublicationManager.getProcessFutures()).thenReturn(Collections.emptyMap());
    when(resourcePublicationManager.getExecutor()).thenReturn(new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()));
    when(resourceManager.list()).thenReturn(Collections.singletonList(resource));
    when(resourcePublicationManager.hasMaxProcessFailures(resource)).thenReturn(false);
    when(resourcePublicationManager.getProcessFailures()).thenReturn(new ConcurrentHashMap<>());
    doThrow(new PublicationException(PublicationException.TYPE.DWCA, "archive failed"))
        .when(resourcePublicationManager).publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class));

    try (RecordingSmtpServer smtpServer = new RecordingSmtpServer()) {
      when(cfg.getMailSmtpHost()).thenReturn("127.0.0.1");
      when(cfg.getMailSmtpPort()).thenReturn(Integer.toString(smtpServer.getPort()));

      PublishingMonitor monitor = new PublishingMonitor(
          mock(SimpleTextProvider.class),
          cfg,
          mock(RegistrationManager.class),
          resourceManager,
          resourcePublicationManager);
      monitor.monitorOnce();

      assertEquals(1, smtpServer.getMessagesReceived());
    }
  }

  @Test
  void monitorKeepsRunningWhenCycleFails() {
    ResourcePublicationManager resourcePublicationManager = mock(ResourcePublicationManager.class);
    when(resourcePublicationManager.getProcessFutures()).thenThrow(new IllegalStateException("unexpected"));
    PublishingMonitor monitor = monitor(mock(AppConfig.class), mock(ResourceManager.class), resourcePublicationManager);

    assertDoesNotThrow(monitor::monitorSafely);
  }

  /**
   * A checked exception thrown without being declared, e.g. from a method annotated with Lombok's @SneakyThrows.
   */
  @Test
  void monitorKeepsRunningWhenCycleFailsWithUndeclaredCheckedException() {
    ResourcePublicationManager resourcePublicationManager = mock(ResourcePublicationManager.class);
    when(resourcePublicationManager.getProcessFutures()).thenAnswer(invocation -> {
      throw new IOException("undeclared");
    });
    PublishingMonitor monitor = monitor(mock(AppConfig.class), mock(ResourceManager.class), resourcePublicationManager);

    assertDoesNotThrow(monitor::monitorSafely);
  }

  @Test
  void monitorCountsUndeclaredCheckedPublicationErrorAsFailure() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    Resource resource = dueResource("bees");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of(resource));
    ResourcePublicationManager resourcePublicationManager = publicationManager();
    when(resourcePublicationManager.publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class)))
        .thenAnswer(invocation -> {
          throw new IOException("undeclared");
        });

    monitor(cfg, resourceManager, resourcePublicationManager).monitorOnce();

    assertEquals(1, resourcePublicationManager.getProcessFailures().get("bees").size());
  }

  @Test
  void monitorFinishesOtherPublicationsWhenOneFails() {
    ResourcePublicationManager resourcePublicationManager = publicationManager();
    Map<String, Future<Map<String, Integer>>> futures = new ConcurrentHashMap<>();
    futures.put("ants", new CompletableFuture<>());
    futures.put("bees", new CompletableFuture<>());
    when(resourcePublicationManager.getProcessFutures()).thenReturn(futures);
    when(resourcePublicationManager.isLocked(eq("ants"), any())).thenThrow(new IllegalStateException("unexpected"));
    when(resourcePublicationManager.isLocked(eq("bees"), any())).thenThrow(new IllegalStateException("unexpected"));
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of());

    monitor(mock(AppConfig.class), resourceManager, resourcePublicationManager).monitorOnce();

    verify(resourcePublicationManager).isLocked(eq("ants"), any());
    verify(resourcePublicationManager).isLocked(eq("bees"), any());
  }

  @Test
  void monitorAutoPublishesOtherResourcesWhenOneFails() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    Resource broken = dueResource("ants");
    Resource due = dueResource("bees");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of(broken, due));
    ResourcePublicationManager resourcePublicationManager = publicationManager();
    when(resourcePublicationManager.hasMaxProcessFailures(broken)).thenThrow(new IllegalStateException("unexpected"));

    monitor(cfg, resourceManager, resourcePublicationManager).monitorOnce();

    verify(resourcePublicationManager).publish(eq(due), any(BigDecimal.class), isNull(), any(PublicationOptions.class));
  }

  @Test
  void monitorCountsUnexpectedPublicationErrorAsFailure() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    Resource resource = dueResource("bees");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of(resource));
    ResourcePublicationManager resourcePublicationManager = publicationManager();
    doThrow(new IllegalStateException("unexpected"))
        .when(resourcePublicationManager).publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class));
    PublishingMonitor monitor = monitor(cfg, resourceManager, resourcePublicationManager);

    monitor.monitorOnce();

    verify(resourcePublicationManager).restoreVersion(eq(resource), any(BigDecimal.class), isNull());
    assertEquals(1, resourcePublicationManager.getProcessFailures().get("bees").size());

    // retried after the retry delay, not on the next cycle
    monitor.monitorOnce();
    verify(resourcePublicationManager, times(1))
        .publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class));
  }

  @Test
  void monitorCountsFailureWhenRestoringFails() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    Resource resource = dueResource("bees");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(List.of(resource));
    ResourcePublicationManager resourcePublicationManager = publicationManager();
    doThrow(new PublicationException(PublicationException.TYPE.DWCA, "archive failed"))
        .when(resourcePublicationManager).publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class));
    doThrow(new IllegalStateException("cannot restore"))
        .when(resourcePublicationManager).restoreVersion(eq(resource), any(BigDecimal.class), isNull());

    monitor(cfg, resourceManager, resourcePublicationManager).monitorOnce();

    assertEquals(1, resourcePublicationManager.getProcessFailures().get("bees").size());
  }

  private static PublishingMonitor monitor(AppConfig cfg, ResourceManager resourceManager,
                                           ResourcePublicationManager resourcePublicationManager) {
    return new PublishingMonitor(mock(SimpleTextProvider.class), cfg, mock(RegistrationManager.class),
        resourceManager, resourcePublicationManager);
  }

  private static ResourcePublicationManager publicationManager() {
    ResourcePublicationManager resourcePublicationManager = mock(ResourcePublicationManager.class);
    when(resourcePublicationManager.getProcessFutures()).thenReturn(Collections.emptyMap());
    when(resourcePublicationManager.getExecutor()).thenReturn(new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()));
    when(resourcePublicationManager.getProcessFailures()).thenReturn(new ConcurrentHashMap<>());
    return resourcePublicationManager;
  }

  private static Resource dueResource(String shortname) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
    resource.setUpdateFrequency("daily");
    resource.setNextPublished(new Date(System.currentTimeMillis() - 1000));
    return resource;
  }

  private static class RecordingSmtpServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final AtomicInteger messagesReceived = new AtomicInteger();
    private final Thread thread;

    RecordingSmtpServer() throws IOException {
      serverSocket = new ServerSocket(0);
      thread = new Thread(this::serve);
      thread.start();
    }

    int getPort() {
      return serverSocket.getLocalPort();
    }

    int getMessagesReceived() {
      return messagesReceived.get();
    }

    private void serve() {
      try (Socket socket = serverSocket.accept();
           BufferedReader reader = new BufferedReader(
               new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
           PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.US_ASCII)) {
        writer.println("220 localhost");

        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("DATA")) {
            writer.println("354 End data with <CR><LF>.<CR><LF>");
            while ((line = reader.readLine()) != null && !".".equals(line)) {
              // read message content
            }
            messagesReceived.incrementAndGet();
            writer.println("250 OK");
          } else if (line.startsWith("QUIT")) {
            writer.println("221 Bye");
            break;
          } else {
            writer.println("250 OK");
          }
        }
      } catch (IOException ignored) {
        // Closing the test server interrupts accept/read during cleanup.
      }
    }

    @Override
    public void close() throws Exception {
      serverSocket.close();
      thread.join(1000);
    }
  }
}
