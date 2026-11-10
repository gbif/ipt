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
package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.service.manage.ResourceManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.dispatcher.HttpParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceLockedInterceptorTest {

  private static final String SHORTNAME = "res1";

  private ResourceLockedInterceptor interceptor;
  private ResourceManager resourceManager;
  private ActionInvocation invocation;
  private Map<String, Object> params;

  @BeforeEach
  void setUp() throws Exception {
    resourceManager = mock(ResourceManager.class);
    interceptor = new ResourceLockedInterceptor();
    interceptor.setResourceManager(resourceManager);
    params = new HashMap<>();
    invocation = mock(ActionInvocation.class);
    when(invocation.invoke()).thenReturn(BaseAction.SUCCESS);
  }

  private String intercept() throws Exception {
    ActionContext context = ActionContext.of(new HashMap<>())
        .withSession(new HashMap<>())
        .withParameters(HttpParameters.create(params).build());
    when(invocation.getInvocationContext()).thenReturn(context);
    return interceptor.intercept(invocation);
  }

  @Test
  public void lockedResourceReturnsLocked() throws Exception {
    params.put(Constants.REQ_PARAM_RESOURCE, SHORTNAME);
    when(resourceManager.isLocked(SHORTNAME)).thenReturn(true);
    assertEquals(BaseAction.LOCKED, intercept());
    verify(invocation, never()).invoke();
  }

  @Test
  public void unlockedResourceProceeds() throws Exception {
    params.put(Constants.REQ_PARAM_RESOURCE, SHORTNAME);
    assertEquals(BaseAction.SUCCESS, intercept());
  }

  @Test
  public void noResourceParamProceeds() throws Exception {
    assertEquals(BaseAction.SUCCESS, intercept());
  }
}
