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

import java.net.URI;
import java.net.URL;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.gbif.ipt.struts2.StaticResourceCacheFilter.IMMUTABLE;
import static org.gbif.ipt.struts2.StaticResourceCacheFilter.REVALIDATE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaticResourceCacheFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  @Mock
  private ServletContext context;

  private final StaticResourceCacheFilter filter = new StaticResourceCacheFilter();

  private void request(String path, boolean exists) throws Exception {
    when(request.getContextPath()).thenReturn("/ipt");
    when(request.getRequestURI()).thenReturn("/ipt" + path);
    lenient().when(request.getServletContext()).thenReturn(context);
    URL url = exists ? URI.create("file:/webapp" + path).toURL() : null;
    lenient().when(context.getResource(anyString())).thenReturn(url);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/js/jquery/jquery-3.7.1.min.js",
    "/js/bootstrap/bootstrap-5.3.8.bundle.min.js",
    "/js/select2/select2-4.0.13.full.min.js",
    "/js/leaflet/locationfilter-0.1-leaflet1.js",
    "/styles/dataTables/dataTables-3.1.5.min.css"
  })
  void versionedFilesAreImmutable(String path) throws Exception {
    request(path, true);
    filter.doFilter(request, response, chain);
    verify(response).setHeader("Cache-Control", IMMUTABLE);
    verify(chain).doFilter(request, response);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/styles/main.css",
    "/js/global.js",
    "/js/docbook/docbook-editor.js",
    "/images/GBIF-2015-standard-ipt.png",
    "/styles/bootstrap-icons/font/fonts/bootstrap-icons.woff2"
  })
  void otherFilesAreRevalidated(String path) throws Exception {
    request(path, true);
    filter.doFilter(request, response, chain);
    verify(response).setHeader("Cache-Control", REVALIDATE);
    verify(chain).doFilter(request, response);
  }

  @Test
  void missingVersionedFileIsRevalidated() throws Exception {
    request("/js/jquery/jquery-3.6.0.min.js", false);
    filter.doFilter(request, response, chain);
    verify(response).setHeader("Cache-Control", REVALIDATE);
  }
}
