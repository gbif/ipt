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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteResourceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteServiceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ListExtensionsRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ListResourceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ListThesauriRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ReadServiceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceRequest;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsRequest;

/**
 * Interface for the GBRDS.
 * 
 */
public interface Gbrds {

  /**
   * Interface for executing an {@link RpcRequest} requiring authentication.
   * 
   * @param <R> the type of response
   * @param <T> the type of response result
   */
  public interface AuthRpcRequest<R extends RpcResponse<T>, T> extends Request {

    /**
     * Executes the request, returning a response of type R. Throws
     * {@link NullPointerException} if the <code>creds</code> are null.
     * 
     * @param creds
     * @return R
     */
    R execute(OrgCredentials creds) throws BadCredentialsException;
  }

  /**
   * This class represents registry specific exceptions.
   * 
   */
  @SuppressWarnings("serial")
  public static class BadCredentialsException extends Exception {
    public BadCredentialsException() {
      super();
    }

    public BadCredentialsException(String string) {
      super(string);
    }

    public BadCredentialsException(String string, Throwable e) {
      super(string, e);
    }
  }

  /**
   * This class surfaces an RPC-style interface to the GBRDS API for extensions
   * and thesauri.
   * 
   */
  public interface IptApi {

    /**
     * Returns a list of {@link GbrdsExtension}.
     * 
     * @return ListExtensionsRequest
     */
    ListExtensionsRequest listExtensions();

    /**
     * Returns a list of {@link GbrdsThesaurus}.
     * 
     * @return ListThesauriRequest
     */
    ListThesauriRequest listThesauri();
  }

  /**
   * This interface surfaces an RPC-style interface to the GBRDS Organisation
   * API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI
   * 
   */
  public interface OrganisationApi {

    /**
     * Returns a {@link CreateOrgRequest} that when executed creates a new GBRDS
     * organisation.
     * 
     * Throws {@link NullPointerException} if {@code org} is null. Throws
     * {@link IllegalArgumentException} if any of the following {@code org}
     * properties are null or the empty string:
     * 
     * <pre>
     * {@code name}
     * {@code primaryContactType}
     * {@code primaryContactEmail}
     * {@code nodeKey}
     * </pre>
     * 
     * Additionally throws {@link IllegalArgumentException} if {@code
     * primaryContactType} is not 'administrative' or 'technical'. Note that the
     * GBRDS expects the {@code nodeKey} to match a 'key' value returned by:
     * http://gbrdsdev.gbif.org/registry/node.json
     * 
     * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI#
     *      CREATE_ORGANISATION
     * 
     * @param org the GBRDS organisation to create
     * @return CreateOrgRequest
     */
    CreateOrgRequest create(GbrdsOrganisation org);

    /**
     * Returns a new {@link DeleteOrgRequest} that when executed with valid
     * {@link OrgCredentials} deletes a GBRDS organisation.
     * 
     * Throws {@link NullPointerException} if the {@code organisationKey} is
     * null. Throws {@link BadCredentialsException} if executed with bad
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code organisationKey} is null or the empty string.
     * 
     * @param organisationKey the key identifying the organisation to delete
     * @return DeleteOrgRequest
     */
    DeleteOrgRequest delete(String organisationKey);

    /**
     * Returns a new {@link ListOrgRequest} that when executed lists all GBRDS
     * organisations.
     * 
     * @return ListOrgRequest
     */
    ListOrgRequest list();

    /**
     * Returns a new {@link ReadOrgRequest} that when executed reads a GBRDS
     * organisation for the given {@code orgkey}. Throws
     * {@link NullPointerException} if the {@code orgkey} is null and throws
     * {@link IllegalArgumentException} if the {@code orgkey} is the empty
     * string.
     * 
     * @param orgKey the key identifying the organisation to read
     * @return ReadOrgRequest
     */
    ReadOrgRequest read(String orgKey);

    /**
     * Returns a {@link UpdateOrgRequest} that when executed with valid
     * {@link OrgCredentials} updates the GBRDS {@code org} and returns the
     * corresponding {@link UpdateOrgResponse}.
     * 
     * Throws {@link NullPointerException} if the {@code org} is null. Throws
     * {@link BadCredentialsException} if executed with invalid
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code key} or {@code primaryContactType} properties are null or the
     * empty string.
     * 
     * @param org the GBRDS org to update
     * @return UpdateOrgRequest
     */
    UpdateOrgRequest update(GbrdsOrganisation org);

    /**
     * Returns a {@link ValidateOrgCredentialsRequest} that when executed
     * validates the {@code creds} against the GBRDS. Throws
     * {@link NullPointerException} if {@code creds} is null.
     * 
     * @param creds the credentials to validate
     * @return ValidateOrgCredentialsRequest
     */
    ValidateOrgCredentialsRequest validateCredentials(OrgCredentials creds);
  }

  /**
   * Authentication credentials.
   * 
   */
  public static class OrgCredentials {

    /**
     * Creates new credentials with for an <code>id</code> and
     * <code>passwd</code>. Throws {@link NullPointerException} or
     * {@link IllegalArgumentException} if {@code id} or {@code passwd} values
     * are null or the empty string.
     * 
     * @param key the id
     * @param password the password
     * @return Credentials
     */
    public static OrgCredentials with(String key, String password) {
      checkNotNull(key, "Key is null");
      checkArgument(key.trim().length() > 0, "Key is empty");
      checkNotNull(password, "Password is null");
      checkArgument(password.trim().length() > 0, "Password is empty");
      return new OrgCredentials(key.trim(), password.trim());
    }

    private final String key;
    private final String password;

    public OrgCredentials(String key, String password) {
      this.key = key;
      this.password = password;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof OrgCredentials)) {
        return false;
      }

      OrgCredentials o = (OrgCredentials) other;
      return Objects.equal(key, o.key) && Objects.equal(password, o.password);
    }

    public String getKey() {
      return key;
    }

    public String getPassword() {
      return password;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(key, password);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).add("Id", key).add("Password",
          password).toString();
    }
  }

  /**
   * An RPC request that gets executed by {@link Gbrds} implementations.
   * 
   */
  public interface Request {

    /**
     * Returns the credentials to use for the request.
     * 
     * @return Credentials
     */
    OrgCredentials getCredentials();

    /**
     * Returns the HTTP method type (GET, POST, DELTE, etc).
     * 
     * @return String
     */
    String getMethod();

    /**
     * Returns an {@link ImmutableMap} of request parameter names and values
     * that are automatically URL encoded and appended as query parameters.
     * 
     * @return ImmutableMap<String,String> the request parameters
     */
    ImmutableMap<String, String> getParams();

    /**
     * The host path used to execute the request. For example, the path in
     * http://foo.com/rpc/bar would be /rpc/bar.
     * 
     * @return String the request path
     */
    String getPath();

    /**
     * Returns an {@link ImmutableMap} of request parameter names and values
     * that are automatically URL encoded and appended to the request body.
     * 
     * @return ImmutableMap<String,String> the request parameters
     */
    ImmutableMap<String, String> getPayload();
  }

  /**
   * This interface surfaces an RPC-style interface to the GBRDS Resource API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/ResourceAPI
   * 
   */
  public interface ResourceApi {

    /**
     * Returns a {@link CreateResourceRequest} that when executed with valid
     * {@link OrgCredentials} creates a new GBRDS resource.
     * 
     * Throws {@link NullPointerException} if {@code resource} is null of if any
     * of the following {@code resource} properties are null:
     * 
     * <pre>
     * {@code name}
     * {@code primaryContactType}
     * {@code primaryContactEmail}
     * {@code organisationKey}
     * </pre>
     * 
     * Throws {@link IllegalArgumentException} if any of the following {@code
     * resource} properties are the empty string:
     * 
     * <pre>
     * {@code name}
     * {@code primaryContactType}
     * {@code primaryContactEmail}
     * {@code organisationKey}
     * </pre>
     * 
     * @param resource the GBRDS resource to create
     * @return CreateResourceRequest
     */
    CreateResourceRequest create(GbrdsResource resource);

    /**
     * Returns a new {@link DeleteResourceRequest} that when executed with valid
     * {@link OrgCredentials} deletes a GBRDS resource.
     * 
     * Throws {@link NullPointerException} if the {@code resourceKey} is null.
     * Throws {@link BadCredentialsException} if executed with bad
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code resourceKey} is null or the empty string.
     * 
     * @param resourceKey the key identifying the resource to delete
     * @return DeleteResourceRequest
     */
    DeleteResourceRequest delete(String resourceKey);

    /**
     * Returns a new {@link ListResourceRequest} that when executed lists all
     * GBRDS resources that are associated with the {@code organisationKey}.
     * 
     * Throws {@link NullPointerException} if {@code organisationKey} is null.
     * Throws {@link IllegalArgumentException} if {@code organisationKey} is the
     * empty string.
     * 
     * @param organisationKey the GBRDS organisation key
     * 
     * @return ListResourceRequest
     */
    ListResourceRequest list(String organisationKey);

    /**
     * Returns a new {@link ReadResourceRequest} that when executed reads a
     * GBRDS resource for the given {@code resourcekey}. Throws
     * {@link NullPointerException} if the {@code resourcekey} is null and
     * throws {@link IllegalArgumentException} if the {@code resourcekey} is the
     * empty string.
     * 
     * @param resourceKey the key identifying the resource to read
     * @return ReadResourceRequest
     */
    ReadResourceRequest read(String resourceKey);

    /**
     * Returns a {@link UpdateResourceRequest} that when executed with valid
     * {@link OrgCredentials} updates the GBRDS {@code resource} and returns the
     * corresponding {@link UpdateResourceResponse}.
     * 
     * Throws {@link NullPointerException} if the {@code resource} is null.
     * Throws {@link BadCredentialsException} if executed with invalid
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code key} or {@code primaryContactType} is null or the empty string.
     * 
     * @param resource the GBRDS resource to update
     * @return UpdateResourceRequest
     */
    UpdateResourceRequest update(GbrdsResource resource);
  }

  /**
   * An RPC response returned by the {@link Gbrds} {@code execute} method that
   * encapsulates the response body, error, status code, and result.
   * 
   * @param <T> the type of result
   */
  public interface Response {

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

    public Request getRequest();

    /**
     * Returns the response HTTP status code.
     * 
     * @return int
     */
    public int getStatus();
  }

  public interface RpcRequest<R extends RpcResponse<T>, T> extends Request {
    R execute();
  }

  public interface RpcResponse<T> extends Response {

    /**
     * Returns the result.
     * 
     * @return T
     */
    public T getResult();

  }

  /**
   * This interface surfaces an RPC-style interface to the GBRDS Service API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/ServiceAPI
   * 
   */
  public interface ServiceApi {

    /**
     * Returns a {@link CreateServiceRequest} that when executed with valid
     * {@link OrgCredentials} creates a new GBRDS {@code service} and returns
     * the corresponding {@link CreateServiceResponse}.
     * 
     * Throws {@link NullPointerException} if the {@code service} is null.
     * Throws {@link BadCredentialsException} if executed with bad
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if any of
     * the following properties are null or empty strings:
     * 
     * <pre>
     * {@code resourceKey}
     * {@code type}
     * {@code accessPointUrl}
     * </pre>
     * 
     * @param service the service to create
     * @return CreateServiceRequest
     */
    CreateServiceRequest create(GbrdsService service);

    /**
     * Returns a {@link DeleteServiceRequest} that when executed with valid
     * {@link OrgCredentials} deletes the GBRDS {@code service} and returns the
     * corresponding {@link DeleteServiceResponse}.
     * 
     * Throws {@link NullPointerException} if the {@code service} is null.
     * Throws {@link BadCredentialsException} if executed with bad
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code key} is null or the empty string.
     * 
     * @param serviceKey the service to delete
     * @return DeleteServiceRequest
     */
    DeleteServiceRequest delete(String serviceKey);

    /**
     * Returns a new {@link ListServicesRequest} that when executed lists all
     * GBRDS services that are associated with the {@code resourceKey}.
     * 
     * Throws {@link NullPointerException} if {@code resourceKey} is null.
     * Throws {@link IllegalArgumentException} if {@code resourceKey} is the
     * empty string.
     * 
     * @param resourceKey the GBRDS resource key
     * @return ListServicesRequest
     */
    ListServicesRequest list(String resourceKey);

    /**
     * Returns a new {@link ReadServiceRequest} that when executed reads a GBRDS
     * service for the given {@code serviceKey}. Throws
     * {@link NullPointerException} if {@code serviceKey} is null. Throws
     * {@link IllegalArgumentException} if {@code serviceKey} is the empty
     * string.
     * 
     * @param serviceKey the GBRDS service key
     * @return ReadServiceRequest
     */
    ReadServiceRequest read(String serviceKey);

    /**
     * Returns a {@link UpdateServiceRequest} that when executed with valid
     * {@link OrgCredentials} updates the GBRDS {@code service} and returns the
     * corresponding {@link UpdateServiceResponse}.
     * 
     * Throws {@link NullPointerException} if the {@code service} is null.
     * Throws {@link BadCredentialsException} if executed with invalid
     * {@link OrgCredentials}. Throws {@link IllegalArgumentException} if the
     * {@code key} is null or the empty string.
     * 
     * @param service the GBRDS service to update
     * @return UpdateServiceRequest
     */
    UpdateServiceRequest update(GbrdsService service);
  }

  /**
   * Returns the {@link IptApi}.
   * 
   * @return IptApi
   */
  public IptApi getIptApi();

  /**
   * Returns the {@link OrganisationApi}.
   * 
   * @return OrganisationApi
   */
  public OrganisationApi getOrganisationApi();

  /**
   * Returns the {@link ResourceApi}.
   * 
   * @return ResourceApi
   */
  public ResourceApi getResourceApi();

  /**
   * Returns the {@link ServiceApi}.
   * 
   * @return ServiceApi
   */
  public ServiceApi getServiceApi();

  /**
   * Executes a {@link Request} and returns the {@link Response}.
   * 
   * Throws {@link NullPointerException} if any of the following are null:
   * 
   * <pre>
   * {@code request}
   * {@code request.getPayload()}
   * {@code request.getParams()}
   * {@code request.getMethod()}
   * {@code request.getPath()}
   * </pre>
   * 
   * Throws {@link IllegalArgumentException} if any of the following are the
   * empty string:
   * 
   * <pre>
   * {@code request.getMethod()}
   * {@code request.getPath()}
   * </pre>
   * 
   * @param request the request to execute
   * @return Response
   */
  Response execute(Request request);
}