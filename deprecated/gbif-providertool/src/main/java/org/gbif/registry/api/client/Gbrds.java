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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Interface for the GBRDS.
 * 
 */
public interface Gbrds {

  /**
   * Interface for executing an {@link RpcRequest} that requires authentication.
   * 
   * @param <R> the type of response
   * @param <T> the type of response result
   */
  public interface AuthRpcRequest<R extends RpcResponse<T>, T> extends Request {

    /**
     * Executes the request with the given credentials, returning a response of
     * type R. Throws {@link NullPointerException} if the {@code creds} are
     * null.
     * 
     * @param creds
     * @return R
     */
    R execute(OrgCredentials creds) throws BadCredentialsException;
  }

  /**
   * This class represents a bad credentials exeception.
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
   * Interface that encapsulates an {@link RpcRequest} for creating a GBRDS
   * organisation. Executing this request returns a {@link CreateOrgResponse}
   * with {@link OrgCredentials} results.
   * 
   */
  public static interface CreateOrgRequest extends
      RpcRequest<CreateOrgResponse, OrgCredentials> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a
   * {@link OrgCredentials} result.
   * 
   */
  public static interface CreateOrgResponse extends RpcResponse<OrgCredentials> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for creating a GBRDS
   * resource. Executing this request returns a {@link CreateResourceResponse}
   * with {@link GbrdsResource} results.
   * 
   */
  public static interface CreateResourceRequest extends
      AuthRpcRequest<CreateResourceResponse, GbrdsResource> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a
   * {@link GbrdsResource} result.
   * 
   */
  public static interface CreateResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for creating a GBRDS
   * service. Executing this request returns a {@link CreateServiceResponse}
   * with a {@link GbrdsService} result.
   * 
   */
  public static interface CreateServiceRequest extends
      AuthRpcRequest<CreateServiceResponse, GbrdsService> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a
   * {@link GbrdsService} result.
   * 
   */
  public static interface CreateServiceResponse extends
      RpcResponse<GbrdsService> {
  }

  /**
   * Interface that encapsulates an {@link AuthRpcRequest} for deleting a GBRDS
   * organisation. Executing this request with valid {@link OrgCredentials}
   * returns a {@link DeleteOrgResponse} with a {@link Boolean} result that is
   * true if the organisation was deleted and false if it was not.
   * 
   */
  public static interface DeleteOrgRequest extends
      AuthRpcRequest<DeleteOrgResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the organisation was deleted and false if it was
   * not.
   * 
   */
  public static interface DeleteOrgResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that encapsulates an {@link AuthRpcRequest} for deleting a GBRDS
   * resource. Executing this request with valid {@link OrgCredentials} returns
   * a {@link DeleteResourceResponse} with a {@link Boolean} result that is true
   * if the resource was deleted and false if it was not.
   * 
   */
  public static interface DeleteResourceRequest extends
      AuthRpcRequest<DeleteResourceResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the resource was deleted and false if it was not.
   * 
   */
  public static interface DeleteResourceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that encapsulates an {@link AuthRpcRequest} for deleting a GBRDS
   * service. Executing this request with valid {@link OrgCredentials} returns a
   * {@link DeleteServiceResponse} with a {@link Boolean} result that is true if
   * the resource was deleted and false if it was not.
   * 
   */
  public static interface DeleteServiceRequest extends
      AuthRpcRequest<DeleteServiceResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the service was deleted and false if it was not.
   * 
   */
  public static interface DeleteServiceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that encapsulated the GBRDS Extension and Thesaurus API.
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
   * Interface that encapsulates an {@link RpcRequest} for listing GBRDS
   * extensions. Executing this request returns a {@link ListExtensionsResponse}
   * with an {@link ImmutableList} of {@link GbrdsExtension} results.
   * 
   */
  public static interface ListExtensionsRequest extends
      RpcRequest<ListExtensionsResponse, ImmutableList<GbrdsExtension>> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link ImmutableList} of {@link GbrdsExtension} results or an empty list if
   * there are no results.
   * 
   */
  public static interface ListExtensionsResponse extends
      RpcResponse<ImmutableList<GbrdsExtension>> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for listing GBRDS
   * organisations. Executing this request returns a {@link ListOrgResponse}
   * with an {@link ImmutableList} of {@link GbrdsOrganisation} results.
   * 
   */
  public static interface ListOrgRequest extends
      RpcRequest<ListOrgResponse, ImmutableList<GbrdsOrganisation>> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link ImmutableList} of {@link GbrdsOrganisation} results or an empty list
   * if there are no results.
   * 
   */
  public static interface ListOrgResponse extends
      RpcResponse<ImmutableList<GbrdsOrganisation>> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for listing GBRDS
   * resources. Executing this request returns a {@link ListResourceResponse}
   * with an {@link ImmutableList} of {@link GbrdsResource} results.
   * 
   */
  public static interface ListResourceRequest extends
      RpcRequest<ListResourceResponse, ImmutableList<GbrdsResource>> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link ImmutableList} of {@link GbrdsResource} results or an empty list if
   * there are no results.
   * 
   */
  public static interface ListResourceResponse extends
      RpcResponse<ImmutableList<GbrdsResource>> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for listing GBRDS
   * services. Executing this request returns a {@link ListServiceResponse} with
   * an {@link ImmutableList} of {@link GbrdsResource} results.
   * 
   */
  public static interface ListServiceRequest extends
      RpcRequest<ListServiceResponse, ImmutableList<GbrdsService>> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link ImmutableList} of {@link GbrdsService} results or an empty list if
   * there are no results.
   * 
   */
  public static interface ListServiceResponse extends
      RpcResponse<ImmutableList<GbrdsService>> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for listing GBRDS
   * thesauri. Executing this request returns a {@link ListThesauriResponse}
   * with an {@link ImmutableList} of {@link GbrdsThesaurus} results.
   * 
   */
  public static interface ListThesauriRequest extends
      RpcRequest<ListThesauriResponse, ImmutableList<GbrdsThesaurus>> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link ImmutableList} of {@link GbrdsService} results or an empty list if
   * there are no results.
   * 
   */
  public static interface ListThesauriResponse extends
      RpcResponse<ImmutableList<GbrdsThesaurus>> {
  }

  /**
   * Interface that encapsulated the GBRDS Organisation API.
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
     * empty string. Throws {@link IllegalArgumentException} if the {@code
     * primaryContactType} is not 'technical' or 'administrative'.
     * 
     * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI#
     *      UPDATE_ORGANISATION
     * 
     * @param org the GBRDS organisation to update
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
   * Interface that encapsulates an {@link RpcRequest} for reading a GBRDS
   * organisation. Executing this request returns a {@link ReadOrgResponse} with
   * a {@link GbrdsOrganisation} result.
   * 
   */
  public static interface ReadOrgRequest extends
      RpcRequest<ReadOrgResponse, GbrdsOrganisation> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link GbrdsOrganisation} result.
   * 
   */
  public static interface ReadOrgResponse extends
      RpcResponse<GbrdsOrganisation> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for reading a GBRDS
   * resource. Executing this request returns a {@link ReadResourceResponse}
   * with a {@link GbrdsResource} result.
   * 
   */
  public static interface ReadResourceRequest extends
      RpcRequest<ReadResourceResponse, GbrdsResource> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link GbrdsResource} result.
   * 
   */
  public static interface ReadResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  /**
   * Interface that encapsulates an {@link RpcRequest} for reading a GBRDS
   * service. Executing this request returns a {@link ReadServiceResponse} with
   * a {@link GbrdsService} result.
   * 
   */
  public static interface ReadServiceRequest extends
      RpcRequest<ReadServiceResponse, GbrdsService> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with an
   * {@link GbrdsService} result.
   * 
   */
  public static interface ReadServiceResponse extends RpcResponse<GbrdsService> {
  }

  /**
   * Interface for encapsulating a GBRDS request that provides credentials, the
   * HTTP method type to use, a mapping of request parameter names and values,
   * and the request path.
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
   * Interface that encapsulated the GBRDS Resource API.
   * 
   */
  public interface ResourceApi {

    /**
     * Returns a {@link CreateResourceRequest} that when executed with valid
     * {@link OrgCredentials} creates a new GBRDS resource.
     * 
     * Throws {@link NullPointerException} if {@code resource} is null. Throws
     * {@link IllegalArgumentException} if any of the following {@code resource}
     * properties are null or the empty string:
     * 
     * <pre>
   * {@code name}
   * {@code primaryContactType}
   * {@code primaryContactEmail}
   * {@code organisationKey}
   * </pre>
     * 
     * Additionally throws {@link IllegalArgumentException} if {@code
     * primaryContactType} is not 'administrative' or 'technical'.
     * 
     * @see http 
     *      ://code.google.com/p/gbif-registry/wiki/ResourceAPI#CREATE_RESOURCE
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
   * Interface for encapsulating a GBRDS response that provides the original
   * request, the response body, the HTTP status code, and an error if one
   * occurred during the request.
   * 
   */
  public interface Response {

    /**
     * Returns the response body created by the GBRDS.
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
     * Returns the original request.
     * 
     * @return Request
     */
    public Request getRequest();

    /**
     * Returns the HTTP status code.
     * 
     * @return int
     */
    public int getStatus();
  }

  /**
   * Interface for an RPC style GBRDS request. Executing this request returns an
   * {@link RpcResponse}.
   * 
   * @param <R> the type of RpcResponse returned from the execute method
   * @param <T> the result type returned by the RpcResponse
   */
  public interface RpcRequest<R extends RpcResponse<T>, T> extends Request {
    /**
     * Executes the request and returns the corresponding response.
     * 
     * @return R the type of RpcRespose returned by the execute method
     */
    R execute();
  }

  /**
   * Interface for an RPC style GBRDS response that returns a result.
   * 
   * @param <T> the result type encapsulated by the response
   */
  public interface RpcResponse<T> extends Response {

    /**
     * Returns the response result.
     * 
     * @return T the type of result
     */
    public T getResult();

  }

  /**
   * Interface that encapsulated the GBRDS Service API.
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
     * Returns a new {@link ListServiceRequest} that when executed lists all
     * GBRDS services that are associated with the {@code resourceKey}.
     * 
     * Throws {@link NullPointerException} if {@code resourceKey} is null.
     * Throws {@link IllegalArgumentException} if {@code resourceKey} is the
     * empty string.
     * 
     * @param resourceKey the GBRDS resource key
     * @return ListServicesRequest
     */
    ListServiceRequest list(String resourceKey);

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
   * Interface that encapsulates an {@link AuthRpcRequest} for updating a GBRDS
   * organisation. Executing this request with valid {@link OrgCredentials}
   * returns a {@link UpdateOrgResponse} with a {@link Boolean} result that is
   * true if the organisation was deleted and false if it was not.
   * 
   */
  public static interface UpdateOrgRequest extends
      AuthRpcRequest<UpdateOrgResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the organisation was updated and false if it was
   * not.
   * 
   */
  public static interface UpdateOrgResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that encapsulates an {@link AuthRpcRequest} for updating a GBRDS
   * resource. Executing this request with valid {@link OrgCredentials} returns
   * a {@link UpdateResourceResponse} with a {@link Boolean} result that is true
   * if the organisation was deleted and false if it was not.
   * 
   */
  public static interface UpdateResourceRequest extends
      AuthRpcRequest<UpdateResourceResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the resource was udpated and false if it was not.
   * 
   */
  public static interface UpdateResourceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that encapsulates an {@link AuthRpcRequest} for updating a GBRDS
   * service. Executing this request with valid {@link OrgCredentials} returns a
   * {@link UpdateOrgResponse} with a {@link Boolean} result that is true if the
   * organisation was deleted and false if it was not.
   * 
   */
  public static interface UpdateServiceRequest extends
      AuthRpcRequest<UpdateServiceResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the service was updated and false if it was not.
   * 
   */
  public static interface UpdateServiceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface that validates organisation credentials. Executing this request
   * returns a {@link ValidateOrgCredentialsResponse} with a {@link Boolean}
   * result that is true if the credentials are valid and false if they are not.
   */
  public static interface ValidateOrgCredentialsRequest extends
      RpcRequest<ValidateOrgCredentialsResponse, Boolean> {
  }

  /**
   * Interface that encapsulates an {@link RpcResponse} with a {@link Boolean}
   * result that is true if the credentials are valid and false if they are not.
   */
  public static interface ValidateOrgCredentialsResponse extends
      RpcResponse<Boolean> {
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