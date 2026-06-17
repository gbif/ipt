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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublishingMonitorTest {

  @Test
  void monitorSendsPublicationFailureEmailWhenEnabledAndConfigured() throws Exception {
    AppConfig cfg = mock(AppConfig.class);
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
    when(resourceManager.getProcessFutures()).thenReturn(Collections.emptyMap());
    when(resourceManager.getExecutor()).thenReturn(new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()));
    when(resourceManager.list()).thenReturn(Collections.singletonList(resource));
    when(resourceManager.hasMaxProcessFailures(resource)).thenReturn(false);
    when(resourceManager.getProcessFailures()).thenReturn(new ArrayListValuedHashMap<>());
    doThrow(new PublicationException(PublicationException.TYPE.DWCA, "archive failed"))
        .when(resourceManager).publish(eq(resource), any(BigDecimal.class), isNull(), any(PublicationOptions.class));

    try (RecordingSmtpServer smtpServer = new RecordingSmtpServer()) {
      when(cfg.getMailSmtpHost()).thenReturn("127.0.0.1");
      when(cfg.getMailSmtpPort()).thenReturn(Integer.toString(smtpServer.getPort()));

      PublishingMonitor monitor = new PublishingMonitor(
          mock(SimpleTextProvider.class),
          cfg,
          mock(RegistrationManager.class),
          resourceManager);
      monitor.monitorOnce();

      assertEquals(1, smtpServer.getMessagesReceived());
    }
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
