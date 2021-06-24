package org.gbif.ipt.struts2;


import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IptI18nInterceptorTest {
  @Test
  public void testGetLocaleFromParam() {
    IptI18nInterceptor interceptor = new IptI18nInterceptor();

    // Test Italian, which is not yet supported by the IPT and should return the default Locale
    assertEquals(Locale.getDefault(), interceptor.getLocaleFromParam(Locale.ITALIAN));

    // Test non-interpretable language that should return default Locale
    assertEquals(Locale.getDefault(), interceptor.getLocaleFromParam("$"));

    // Test support for Persian, which is soon supported by the IPT, but not supported by Struts2/JRE by default
    assertEquals(new Locale("fa"), interceptor.getLocaleFromParam(new Locale("fa")));

    // Test support for existing 7 languages as of v2.3.5 working as expected:
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
