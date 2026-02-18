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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.model.UrlMetadata;
import org.gbif.ipt.utils.FileUtils;

import java.io.IOException;
import java.io.Serial;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionSupport;

public class UrlMetadataAction extends ActionSupport {

  @Serial
  private static final long serialVersionUID = 4678767005519916360L;

  private static final Logger LOG = LogManager.getLogger(UrlMetadataAction.class);

  private static final String JSON_RESPONSE = "{" +
      "\"status\": %d, " +
      "\"contentType\": \"%s\", " +
      "\"contentLength\": %d, " +
      "\"lastModified\": \"%s\", " +
      "\"acceptRanges\": \"%s\"" +
      "}";

  private String url;

  @Override
  public String execute() {
    HttpServletResponse response = ServletActionContext.getResponse();
    response.setContentType("application/json");

    if (url == null || url.isEmpty()) {
      try {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'url' parameter");
      } catch (IOException e) {
        LOG.error("Failed to send error response: {}", e.getMessage());
      }
      return NONE;
    }

    try {
      UrlMetadata urlMetadata = FileUtils.fetchUrlMetadata(url);

      String json = String.format(
          JSON_RESPONSE,
          urlMetadata.getStatus(),
          escapeJson(urlMetadata.getContentType()),
          urlMetadata.getContentLength(),
          escapeJson(urlMetadata.getLastModified()),
          escapeJson(urlMetadata.getAcceptRanges())
      );

      response.getWriter().write(json);
    } catch (IOException e) {
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching metadata: " + e.getMessage());
      } catch (IOException ex) {
        LOG.error("Failed to send error response: {}", e.getMessage());
      }
    }

    return NONE;
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\"", "\\\"");
  }

  @StrutsParameter
  public void setUrl(String url) {
    this.url = url;
  }
}
