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
package org.gbif.ipt.action;

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.struts2.ActionContext;
import org.apache.struts2.inject.Container;
import org.apache.struts2.locale.LocaleProvider;
import org.apache.struts2.locale.LocaleProviderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseActionTest extends IptBaseTest {

  /**
   * Test getLocale on none Struts action context.
   */
  @Test
  public void testGetLocaleOnNoneActionContext() {
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertNull(action.getLocale());
  }

  /**
   * Test getLocale on Struts action context.
   */
  @Test
  public void testGetLocaleOnActionContext() {
    Map<String, Object> contextMap = new HashMap<>();

    Container container = mock(Container.class);

    LocaleProviderFactory localeProviderFactory = mock(LocaleProviderFactory.class);
    LocaleProvider localeProvider = mock(LocaleProvider.class);

    when(localeProvider.getLocale()).thenReturn(Locale.JAPANESE);
    when(localeProviderFactory.createLocaleProvider()).thenReturn(localeProvider);
    when(container.getInstance(LocaleProviderFactory.class))
        .thenReturn(localeProviderFactory);

    ActionContext actionContext = ActionContext.of(contextMap)
        .withContainer(container);

    ActionContext.bind(actionContext);

    try {
      BaseAction action = new BaseAction(
          mock(SimpleTextProvider.class),
          mock(AppConfig.class),
          mock(RegistrationManager.class)
      );

      Locale locale = action.getLocale();

      assertEquals(Locale.JAPANESE, locale);
    } finally {
      ActionContext.clear();
    }
  }
}
