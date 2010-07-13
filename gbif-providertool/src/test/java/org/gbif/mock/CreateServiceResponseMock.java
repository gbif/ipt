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
package org.gbif.mock;

import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.Request;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;

/**
 *
 */
public class CreateServiceResponseMock implements CreateServiceResponse {

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.registry.api.client.Gbrds.Response#getBody()
   */
  public String getBody() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.registry.api.client.Gbrds.Response#getError()
   */
  public Throwable getError() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.registry.api.client.Gbrds.Response#getRequest()
   */
  public Request getRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.registry.api.client.Gbrds.RpcResponse#getResult()
   */
  public GbrdsService getResult() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.registry.api.client.Gbrds.Response#getStatus()
   */
  public int getStatus() {
    // TODO Auto-generated method stub
    return 0;
  }

}
