/*
 * Copyright 2010 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.registry.api.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;

/**
 * Interface for a registry.
 */
public interface Registry {

  /**
   * Authentication credentials.
   * 
   */
  public static class Credentials {

    /**
     * Creates new credentials with an id and passwd. Throws
     * {@link NullPointerException} or {@link IllegalArgumentException} if
     * {@code id} or {@code passwd} values are null or the empty string.
     * 
     * @param id the id
     * @param passwd the password
     * @return Credentials
     */
    public static Credentials with(String id, String passwd) {
      checkNotNull(id, "Id is null");
      checkArgument(id.length() > 0, "Id is empty");
      checkNotNull(passwd, "Password is null");
      checkArgument(passwd.length() > 0, "Password is empty");
      return new Credentials(id, passwd);
    }

    private final String id;
    private final String passwd;

    public Credentials(String id, String passwd) {
      this.id = id;
      this.passwd = passwd;
    }

    public String getId() {
      return id;
    }

    public String getPasswd() {
      return passwd;
    }
  }

  /**
   * An RPC request that gets executed by {@link Registry} implementations.
   * 
   */
  public interface RpcRequest {

    /**
     * Returns the credentials to use for the request.
     * 
     * @return Credentials
     */
    Credentials getCredentials();

    /**
     * Returns the HTTP method type (GET, POST, DELTE, etc).
     * 
     * @return String
     */
    String getHttpMethodType();

    /**
     * Returns an {@link ImmutableMap} of request parameter names and values
     * that are automatically URL encoded and appended to the request body.
     * 
     * @return ImmutableMap<String,String> the request parameters
     */
    ImmutableMap<String, String> getPayload();

    /**
     * Returns an {@link ImmutableMap} of request parameter names and values
     * that are automatically URL encoded and appended as query parameters.
     * 
     * @return ImmutableMap<String,String> the request parameters
     */
    ImmutableMap<String, String> getRequestParams();

    /**
     * The host path used to execute the request. For example, the path in
     * http://foo.com/rpc/bar would be /rpc/bar.
     * 
     * @return String the request path
     */
    String getRequestPath();
  }

  /**
   * An RPC response returned by the {@link Registry} {@code execute} method
   * that encapsulates the response body, error, status code, and result.
   * 
   * @param <T> the type of result
   */
  public interface RpcResponse<T> {

    /**
     * Returns the original response body.
     * 
     * @return String
     */
    public String getBody();

    /**
     * Returns the error that occurred during the request, or null if no error
     * occurred.
     * 
     * @return Throwable
     */
    public Throwable getError();

    /**
     * Returns the result.
     * 
     * @return T
     */
    public T getResult();

    /**
     * Returns the response HTTP status code.
     * 
     * @return int
     */
    public int getStatusCode();
  }

  /**
   * Executes an {@link RpcRequest} and returns an {@link RpcResponse}.
   * 
   * @param rpc the RPC request to execute
   * @return {@link RpcResponse} the response
   */
  <T> RpcResponse<T> execute(RpcRequest rpc);
}