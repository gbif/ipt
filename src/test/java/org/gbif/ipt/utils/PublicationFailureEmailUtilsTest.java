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
package org.gbif.ipt.utils;

import org.gbif.ipt.config.AppConfig;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicationFailureEmailUtilsTest {

  @Test
  void isConfiguredRequiresSmtpHostAndAdminEmail() {
    AppConfig cfg = mock(AppConfig.class);

    assertFalse(PublicationFailureEmailUtils.isConfigured(cfg));

    when(cfg.getMailSmtpHost()).thenReturn("smtp.example.org");
    assertFalse(PublicationFailureEmailUtils.isConfigured(cfg));

    when(cfg.getAdminEmail()).thenReturn("admin@example.org");
    assertTrue(PublicationFailureEmailUtils.isConfigured(cfg));
  }

  @Test
  void mailPropertiesIncludeTimeouts() {
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.getMailSmtpHost()).thenReturn("smtp.example.org");
    when(cfg.getMailSmtpPort()).thenReturn("587");
    when(cfg.isMailSmtpStartTlsEnabled()).thenReturn(true);
    when(cfg.getMailSmtpUsername()).thenReturn("smtp-user");

    Properties properties = PublicationFailureEmailUtils.mailProperties(cfg);

    assertEquals("smtp.example.org", properties.getProperty("mail.smtp.host"));
    assertEquals("587", properties.getProperty("mail.smtp.port"));
    assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
    assertEquals("true", properties.getProperty("mail.smtp.auth"));
    assertEquals("10000", properties.getProperty("mail.smtp.connectiontimeout"));
    assertEquals("10000", properties.getProperty("mail.smtp.timeout"));
    assertEquals("10000", properties.getProperty("mail.smtp.writetimeout"));
  }
}
