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

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IptI18nInterceptorTest {

  @Test
  public void testGetLocaleFromParam() {
    IptI18nInterceptor interceptor = new IptI18nInterceptor();

    Locale defaultLocale = Locale.getDefault();

    // Test Italian, which is not yet supported by the IPT and should return the default Locale
    assertEquals(defaultLocale, interceptor.getLocaleFromParam(Locale.ITALIAN));

    // Test non-interpretable language that should return default Locale unless the default one is English
    // which will be transformed into English UK
    if (!defaultLocale.equals(Locale.ENGLISH)) {
      assertEquals(defaultLocale, interceptor.getLocaleFromParam("$"));
    } else {
      assertEquals(Locale.UK, interceptor.getLocaleFromParam("$"));
    }

    // Test support for Persian, which is soon supported by the IPT, but not supported by Struts2/JRE by default
    assertEquals(new Locale("fa"), interceptor.getLocaleFromParam(new Locale("fa")));

    // Test support for existing 7 languages as of v2.5.0 working as expected:
    // Spanish, Japanese, Portuguese, Traditional Chinese, Russian, French and English
    assertEquals(Locale.UK, interceptor.getLocaleFromParam(new Locale("en")));
    assertEquals(Locale.FRENCH, interceptor.getLocaleFromParam(new Locale("fr")));
    assertEquals(Locale.CHINESE, interceptor.getLocaleFromParam(new Locale("zh")));
    assertEquals(Locale.JAPANESE, interceptor.getLocaleFromParam(new Locale("ja")));
    assertEquals(new Locale("es"), interceptor.getLocaleFromParam(new Locale("es")));
    assertEquals(new Locale("pt"), interceptor.getLocaleFromParam(new Locale("pt")));
    assertEquals(new Locale("ru"), interceptor.getLocaleFromParam(new Locale("ru")));

    // Test null in, null out
    assertNull(interceptor.getLocaleFromParam(null));
  }
}
