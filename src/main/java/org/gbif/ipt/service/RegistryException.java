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
package org.gbif.ipt.service;

import org.gbif.ipt.action.BaseAction;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * The base class used to indicate types of errors occurring during interaction with the GBIF registry.
 * All exceptions must provide a message and a cause.
 */
public class RegistryException extends RuntimeException {

  public enum Type {
    /**
     * Proper credentials weren't specified.
     */
    NOT_AUTHORISED,
    /**
     * No key was returned for registered resource.
     */
    MISSING_METADATA,
    /**
     * The response from the Registry was empty or invalid.
     */
    BAD_RESPONSE,
    /**
     * The request to the Registry used an invalid syntax.
     */
    BAD_REQUEST,
    /**
     * Some kind of IO error occurred.
     */
    IO_ERROR,
    /**
     * Unknown failure occurred while communicating with Registry.
     */
    UNKNOWN,
    /**
     * A connection exception occurred. Likely Proxy or Firewall related.
     */
    PROXY,
    /**
     * If server could connect to Google, but not to GBIF Registry.
     */
    SITE_DOWN,
    /**
     * There is no connection to the Internet. Indicates that the IP address of a host could not be determined.
     */
    NO_INTERNET
  }

  protected Type type;

  protected String url;

  public RegistryException(String url, Exception e) {
    super(e.getMessage(), e);

    if (e instanceof ClassCastException) {
      type = Type.BAD_RESPONSE;
    } else if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
      type = Type.PROXY;
    } else if (e instanceof UnknownHostException) {
      type = Type.NO_INTERNET;
    } else if (e instanceof IOException) {
      type = Type.IO_ERROR;
    } else if (e instanceof URISyntaxException) {
      type = Type.BAD_REQUEST;
    } else {
      type = Type.UNKNOWN;
    }

    this.url = url;
  }

  public RegistryException(Type type, String url, Exception e) {
    super(e.getMessage(), e);
    this.type = type;
    this.url = url;
  }

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   *
   * @param type    Is stored in the exception
   * @param message The message to use for logging (not display through the web application)
   */
  public RegistryException(Type type, String url, String message) {
    super(message);
    this.type = type;
    this.url = url;
  }

  public RegistryException(Type type, String url, String message, Exception e) {
    super(message, e);
    this.type = type;
    this.url = url;
  }

  /**
   * @return the type of configuration exception. This allows for internationalized display
   */
  public Type getType() {
    return type;
  }

  /**
   * Depending on the RegistryException Type, retrieves an i18n message, and returns it. Ideally, the message returned
   * will be the most specific error possible as to why the exception was thrown. Such a specific message will shield
   * the IPT admin, from having to interpret complex stacktrace exceptions.
   *
   * @param e      RegistryException
   * @param action BaseAction
   *
   * @return log message
   */
  public static String logRegistryException(RegistryException e, BaseAction action) {
    // retrieve specific log message, depending on Type
    String msg = action.getText("admin.registration.error.registry");
    if (e.type != null) {
      if (e.type == Type.PROXY) {
        msg = action.getText("admin.registration.error.proxy");
      } else if (e.type == Type.SITE_DOWN) {
        msg = action.getText("admin.registration.error.siteDown");
      } else if (e.type == Type.NO_INTERNET) {
        msg = action.getText("admin.registration.error.internetConnection");
      } else if (e.type == Type.BAD_RESPONSE) {
        msg = action.getText("admin.registration.error.badResponse");
      } else if (e.type == Type.IO_ERROR) {
        msg = action.getText("admin.registration.error.io");
      } else if (e.type == Type.BAD_REQUEST) {
        // this may occur when Registry WS rejects request, e.g. invalid email address (POR-1975)
        msg = action.getText("admin.registration.error.badRequest");
      } else if (e.type == Type.UNKNOWN) {
        msg = action.getText("admin.registration.error.unknown");
      }
    }
    return msg + " [" + e.url + "]";
  }
}
