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

import org.gbif.ipt.config.AppConfig;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultLocaleInterceptorTest {

    @InjectMocks
    private DefaultLocaleInterceptor interceptor = new DefaultLocaleInterceptor();

    // inject spy into interceptor
    @Spy
    private AppConfig appConfigSpy;

    @Mock
    private ActionInvocation invocationMock;
    @Mock
    private ActionContext actionContextMock;

    @ParameterizedTest
    @ValueSource(strings = {"en_GB", "fr", "es", "pt", "ru", "ja", "zh"})
    public void testSupportedDefaultLocale(String locale) throws Exception {
        when(invocationMock.getInvocationContext()).thenReturn(actionContextMock);
        when(appConfigSpy.getDefaultLocale()).thenReturn(locale);

        interceptor.intercept(invocationMock);
        verify(invocationMock, Mockito.times(1)).getInvocationContext();
        verify(actionContextMock, Mockito.times(1)).setLocale(LocaleUtils.toLocale(locale));
    }

    @Test
    public void testDefaultLocaleNullDefaultsToEnGb() throws Exception {
        when(invocationMock.getInvocationContext()).thenReturn(actionContextMock);
        when(appConfigSpy.getDefaultLocale()).thenReturn(null);

        interceptor.intercept(invocationMock);
        verify(invocationMock, Mockito.times(1)).getInvocationContext();
        verify(actionContextMock, Mockito.times(1)).setLocale(LocaleUtils.toLocale("en_GB"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "it", "ar", "fa", "en_DK"})
    public void testNotSupportedLocaleDefaultToEnGb(String locale) throws Exception {
        when(invocationMock.getInvocationContext()).thenReturn(actionContextMock);
        when(appConfigSpy.getDefaultLocale()).thenReturn(locale);

        interceptor.intercept(invocationMock);
        verify(invocationMock, Mockito.times(1)).getInvocationContext();
        verify(actionContextMock, Mockito.times(1)).setLocale(LocaleUtils.toLocale("en_GB"));
    }
}
