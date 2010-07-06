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

import org.gbif.registry.api.client.GbifRegistry.CreateOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.CreateResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.CreateServiceRequest;
import org.gbif.registry.api.client.GbifRegistry.DeleteOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.DeleteResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.DeleteServiceRequest;
import org.gbif.registry.api.client.GbifRegistry.ListExtensionsRequest;
import org.gbif.registry.api.client.GbifRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ListResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.ListServicesForResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadServiceRequest;
import org.gbif.registry.api.client.GbifRegistry.UpdateOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.UpdateResourceRequest;
import org.gbif.registry.api.client.GbifRegistry.UpdateServiceRequest;
import org.gbif.registry.api.client.GbifRegistry.ValidateOrgCredentialsRequest;

/**
 * Interface for the GBRDS.
 * 
 */
public interface Gbrds {

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

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Credentials)) {
        return false;
      }

      Credentials o = (Credentials) other;
      return Objects.equal(id, o.id) && Objects.equal(passwd, o.passwd);
    }

    public String getId() {
      return id;
    }

    public String getPasswd() {
      return passwd;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id, passwd);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this).add("Id", id).add("Password", passwd).toString();
    }
  }

  /**
   * This class surfaces an RPC-style interface to the GBIF Registry Extension
   * API.
   */
  public interface ExtensionApi {

    /**
     * Returns a list of {@link GbifExtension}.
     * 
     * @return ListExtensionsRequest
     */
    ListExtensionsRequest list();
  }

  /**
   * This class surfaces an RPC-style interface to the GBIF Registry
   * Organisation API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI
   * 
   */
  public interface OrganisationApi {

    /**
     * Returns a {@link CreateOrgRequest} that when executed creates a new
     * organisation.
     * 
     * If the organisation is null, a {@link NullPointerException} is thrown.
     * The organisation must include {@code name}, {@code primaryContactType},
     * {@code primaryContactEmail}, and {@code nodeKey}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param org the organisation to create
     * @return RpcRequest the RPC request for creating the organisation
     */
    CreateOrgRequest create(GbifOrganisation org);

    /**
     * Returns a new {@link DeleteOrgRequest} requiring authentication that when
     * executed deletes an organisation. The organisation must include a {@code
     * key} and {@code password}, otherwise an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @see http://goo.gl/qJql
     * 
     * @param org the organisation to delete
     * @return RpcRequest the RPC request for deleting the organisation
     */
    DeleteOrgRequest delete(GbifOrganisation org);

    /**
     * Returns a new {@link ListOrgRequest} that when executed lists all
     * organisations.
     * 
     * @see http://goo.gl/D6qH
     * 
     * @return RpcRequest the RPC request for listing all organisations
     */
    ListOrgRequest list();

    /**
     * Returns a new {@link ReadOrgRequest} that when executed reads an
     * organisation from the GBIF Registry based on an organisation key. The
     * {@code key} is required, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @see http://goo.gl/68dV
     * 
     * @param key the organisation key
     * @return RpcRequest the RPC request that reads the organisation
     */
    ReadOrgRequest read(String orgKey);

    /**
     * Returns a {@link UpdateOrgRequest} requiring authentication that when
     * executed updates the organisation. The organisation must include {@code
     * key}, {@code primaryContactType}, and {@code password}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param org the organisation to update
     * @return RpcRequest the RPC request for updating the organisation
     */
    UpdateOrgRequest update(GbifOrganisation org);

    ValidateOrgCredentialsRequest validateCredentials(String organisationKey,
        Credentials credentials);
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
   * This class surfaces an RPC-style interface to the GBIF Registry Resource
   * API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/ResourceAPI
   * 
   */
  public interface ResourceApi {

    /**
     * Returns a {@link CreateResourceRequest} that when executed creates a new
     * resource.
     * 
     * If the resource is null, a {@link NullPointerException} is thrown. The
     * resource must include {@code name}, {@code primaryContactType}, {@code
     * primaryContactEmail}, and {@code nodeKey}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param resource the organisation to create
     * @return RpcRequest the RPC request for creating the resource
     */
    CreateResourceRequest create(GbifResource resource);

    /**
     * Returns a new {@link DeleteResourceRequest} requiring authentication that
     * when executed deletes an resource. The resource must include a {@code
     * key} and {@code password}, otherwise an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @see http://goo.gl/qJql
     * 
     * @param resource the resource to delete
     * @return RpcRequest the RPC request for deleting the resource
     */
    DeleteResourceRequest delete(GbifResource resource);

    /**
     * Returns a new {@link ListResourceRequest} that when executed lists all
     * resources.
     * 
     * @see http://goo.gl/D6qH
     * 
     * @return RpcRequest the RPC request for listing all resources
     */
    ListResourceRequest list();

    /**
     * Returns a new {@link ReadResourceRequest} that when executed reads an
     * resource from the GBIF Registry based on an organisation key. The {@code
     * key} is required, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @see http://goo.gl/68dV
     * 
     * @param key the organisation key
     * @return RpcRequest the RPC request that reads the resource
     */
    ReadResourceRequest read(String orgKey);

    /**
     * Returns a {@link UpdateResourceRequest} requiring authentication that
     * when executed updates the resource. The resource must include {@code key}
     * , {@code primaryContactType}, and {@code password}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param resource the resource to update
     * @return RpcRequest the RPC request for updating the resource
     */
    UpdateResourceRequest update(GbifResource resource);
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
   * This class surfaces an RPC-style interface to the GBIF Registry Service
   * API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/ServiceAPI
   * 
   */
  public interface ServiceApi {

    /**
     * Returns a {@link CreateServiceRequest} that when executed creates a new
     * service.
     * 
     * If the service is null, a {@link NullPointerException} is thrown. The
     * service must include {@code name}, {@code primaryContactType}, {@code
     * primaryContactEmail}, and {@code nodeKey}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param service the organisation to create
     * @return RpcRequest the RPC request for creating the service
     */
    CreateServiceRequest create(GbifService service);

    /**
     * Returns a new {@link DeleteServiceRequest} requiring authentication that
     * when executed deletes an service. The service must include a {@code key}
     * and {@code password}, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @see http://goo.gl/qJql
     * 
     * @param service the service to delete
     * @return RpcRequest the RPC request for deleting the service
     */
    DeleteServiceRequest delete(GbifService service);

    /**
     * Returns a new {@link ListServicesForResourceRequest} that when executed
     * lists all GBIF Services from the GBRDS that are associated with a
     * <code>resourceKey</code>.
     * 
     * @param resourceKey TODO
     * 
     * @see http://goo.gl/D6qH
     * 
     * @return RpcRequest the RPC request for listing all resources
     */
    ListServicesForResourceRequest list(String resourceKey);

    /**
     * Returns a new {@link ReadServiceRequest} that when executed reads an
     * service from the GBIF Registry based on an organisation key. The {@code
     * key} is required, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @see http://goo.gl/68dV
     * 
     * @param key the organisation key
     * @return RpcRequest the RPC request that reads the service
     */
    ReadServiceRequest read(String orgKey);

    /**
     * Returns a {@link UpdateServiceRequest} requiring authentication that when
     * executed updates the service. The service must include {@code key} ,
     * {@code primaryContactType}, and {@code password}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param service the service to update
     * @return RpcRequest the RPC request for updating the service
     */
    UpdateServiceRequest update(GbifService service);
  }

  public ExtensionApi getExtensionApi();

  public OrganisationApi getOrganisationApi();

  public ResourceApi getResourceApi();

  public ServiceApi getServiceApi();

  /**
   * Executes an {@link Request} and returns an {@link Response}.
   * 
   * @param rpc the RPC request to execute
   * @return {@link Response} the response
   */
  Response execute(Request request);
}