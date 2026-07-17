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
import org.gbif.ipt.model.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublicationFailureEmailUtils {

  private static final Logger LOG = LogManager.getLogger(PublicationFailureEmailUtils.class);

  private static final String DEFAULT_SMTP_PORT = "25";
  private static final String DEFAULT_SMTP_TIMEOUT_MS = "10000";

  private PublicationFailureEmailUtils() {
  }

  public static boolean isConfigured(AppConfig cfg) {
    return StringUtils.isNotBlank(cfg.getMailSmtpHost()) && StringUtils.isNotBlank(cfg.getAdminEmail());
  }

  public static void send(AppConfig cfg, Resource resource, BigDecimal version, String reason) throws MessagingException {
    Transport.send(createMessage(cfg, resource, version, reason));
  }

  static Message createMessage(AppConfig cfg, Resource resource, BigDecimal version, String reason) throws MessagingException {
    Session session = Session.getInstance(mailProperties(cfg), authenticator(cfg));
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(cfg.getAdminEmail()));
    message.setRecipients(Message.RecipientType.TO, getRecipients(cfg, resource));
    message.setSubject("IPT publication failed: " + resource.getTitleAndShortname());
    message.setText(messageText(cfg, resource, version, reason));
    return message;
  }

  static Address[] getRecipients(AppConfig cfg, Resource resource) throws MessagingException {
    List<String> recipients = resource.getPublicationFailureEmails();
    Address[] result;

    if (recipients == null || recipients.isEmpty()) {
      // fallback: no resource-specific recipients configured, use the admin address
      LOG.warn("No publication failure email recipients configured for resource {}, using admin address {}",
          resource.getShortname(), cfg.getAdminEmail());
      result = InternetAddress.parse(cfg.getAdminEmail());
    } else {
      String joined = String.join(",", recipients);
      result = InternetAddress.parse(joined);
    }

    return result;
  }

  static Properties mailProperties(AppConfig cfg) {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", cfg.getMailSmtpHost());
    properties.put("mail.smtp.port", StringUtils.defaultIfBlank(cfg.getMailSmtpPort(), DEFAULT_SMTP_PORT));
    properties.put("mail.smtp.starttls.enable", Boolean.toString(cfg.isMailSmtpStartTlsEnabled()));
    properties.put("mail.smtp.connectiontimeout", DEFAULT_SMTP_TIMEOUT_MS);
    properties.put("mail.smtp.timeout", DEFAULT_SMTP_TIMEOUT_MS);
    properties.put("mail.smtp.writetimeout", DEFAULT_SMTP_TIMEOUT_MS);

    if (StringUtils.isNotBlank(cfg.getMailSmtpUsername())) {
      properties.put("mail.smtp.auth", "true");
    }

    return properties;
  }

  private static Authenticator authenticator(AppConfig cfg) {
    if (StringUtils.isBlank(cfg.getMailSmtpUsername())) {
      return null;
    }

    return new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(cfg.getMailSmtpUsername(), StringUtils.trimToEmpty(cfg.getMailSmtpPassword()));
      }
    };
  }

  private static String messageText(AppConfig cfg, Resource resource, BigDecimal version, String reason) {
    StringBuilder text = new StringBuilder();
    text.append("Auto-publication failed for resource ")
        .append(resource.getTitleAndShortname())
        .append(" version ")
        .append(version.toPlainString())
        .append(".\n\nReason:\n")
        .append(Objects.toString(reason, "Unknown error"))
        .append("\n\n");

    if (StringUtils.isNotBlank(cfg.getBaseUrl())) {
      text.append("Resource: ")
          .append(cfg.getBaseUrl())
          .append("/manage/resource.do?r=")
          .append(resource.getShortname())
          .append('\n');
    }

    return text.toString();
  }
}
