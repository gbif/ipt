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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;

import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultLocaleInterceptorTest extends IptBaseTest {

  @InjectMocks
  private DefaultLocaleInterceptor interceptor = new DefaultLocaleInterceptor();

  @Spy
  private AppConfig appConfigSpy;

  @Mock
  private ActionInvocation invocationMock;

  private ActionContext actionContext;

  @BeforeEach
  void setUp() {
    // Use a real instance instead of a mock (Mockito can't mock ActionContext)
    actionContext = ActionContext.of(new HashMap<>());
    when(invocationMock.getInvocationContext()).thenReturn(actionContext);
  }

  @ParameterizedTest
  @ValueSource(strings = {"en_GB", "fr", "es", "pt", "ru", "ja", "zh"})
  public void testSupportedDefaultLocale(String locale) throws Exception {
    when(appConfigSpy.getDefaultLocale()).thenReturn(locale);
    when(appConfigSpy.isSupportedLocale(any())).thenReturn(true);

    interceptor.intercept(invocationMock);

    assertEquals(locale.toLowerCase(), actionContext.getLocale().toString().toLowerCase());
  }

  @Test
  public void testDefaultLocaleNullDefaultsToEnGb() throws Exception {
    when(appConfigSpy.getDefaultLocale()).thenReturn(null);

    interceptor.intercept(invocationMock);

    assertEquals(Locale.UK, actionContext.getLocale());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "it", "ar", "fa", "en_DK"})
  public void testNotSupportedLocaleDefaultToEnGb(String locale) throws Exception {
    when(appConfigSpy.getDefaultLocale()).thenReturn(locale);
    if (StringUtils.isNotEmpty(locale)) {
      when(appConfigSpy.isSupportedLocale(any())).thenReturn(false);
    }

    interceptor.intercept(invocationMock);

    assertEquals(Locale.UK, actionContext.getLocale());
  }
}
