/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.LocaleProviderFactory;
import com.opensymphony.xwork2.inject.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseActionTest {

  /**
   * Test getLocale on none Struts action context.
   */
  @Test
  public void testGetLocaleOnNoneActionContext() {
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertEquals(null, action.getLocale());
  }

  /**
   * Test getLocale on Struts action context.
   */
  @Test
  public void testGetLocaleOnActionContext() {

    //Simple mocked ActionContext
    ActionContext mockActionContext = mock(ActionContext.class);
    Container mockContainer = mock(Container.class);
    LocaleProviderFactory mockLocaleProviderFactory = mock(LocaleProviderFactory.class);
    LocaleProvider mockLocaleProvider = mock(LocaleProvider.class);

    when(mockLocaleProvider.getLocale()).thenReturn(Locale.JAPANESE);
    when(mockLocaleProviderFactory.createLocaleProvider()).thenReturn(mockLocaleProvider);
    when(mockContainer.getInstance(LocaleProviderFactory.class)).thenReturn(mockLocaleProviderFactory);
    when(mockActionContext.getContainer()).thenReturn(mockContainer);

    //Set threadLocal ActionContext
    ActionContext.setContext(mockActionContext);

    //TEST
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertEquals(Locale.JAPANESE, action.getLocale());
  }
}
