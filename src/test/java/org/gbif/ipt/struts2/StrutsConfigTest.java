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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the interceptor configuration of the Struts packages.
 * Note: declaring any interceptor-ref on an action replaces the package default interceptor stack.
 */
public class StrutsConfigTest {

  private static final Set<String> ROLE_INTERCEPTORS = Set.of(
      "managerStack", "managerAjaxStack", "requireManager",
      "adminStack", "adminAjaxStack", "requireAdmin");

  private static Document parse(String config) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    try (InputStream in = StrutsConfigTest.class.getResourceAsStream(config)) {
      assertNotNull(in, "Missing " + config);
      return builder.parse(in);
    }
  }

  private static List<String> stackRefs(Document doc, String stackName) {
    NodeList stacks = doc.getElementsByTagName("interceptor-stack");
    for (int i = 0; i < stacks.getLength(); i++) {
      Element stack = (Element) stacks.item(i);
      if (stackName.equals(stack.getAttribute("name"))) {
        List<String> refs = new ArrayList<>();
        NodeList children = stack.getElementsByTagName("interceptor-ref");
        for (int j = 0; j < children.getLength(); j++) {
          refs.add(((Element) children.item(j)).getAttribute("name"));
        }
        return refs;
      }
    }
    throw new AssertionError("Stack not found: " + stackName);
  }

  /**
   * The role and resource interceptors run before iptStackWithoutSetup and resourceSession.
   */
  @ParameterizedTest
  @CsvSource({"portalStack,protectPrivateResource", "managerStack,requireManager", "adminStack,requireAdmin"})
  public void interceptorOrder(String stack, String interceptor) throws Exception {
    List<String> refs = stackRefs(parse("/struts.xml"), stack);
    int idx = refs.indexOf(interceptor);
    assertTrue(idx >= 0, stack + " misses " + interceptor);
    assertTrue(idx < refs.indexOf("iptStackWithoutSetup"), stack + ": " + refs);
    if (refs.contains("resourceSession")) {
      assertTrue(idx < refs.indexOf("resourceSession"), stack + ": " + refs);
    }
  }

  /**
   * Resource-scoped manage actions return a 400 without the r parameter, so redirects to them must pass it on.
   */
  @Test
  public void manageRedirectsKeepResourceParam() throws Exception {
    Document doc = parse("/struts-manage.xml");

    List<String> missing = new ArrayList<>();
    NodeList results = doc.getElementsByTagName("result");
    for (int i = 0; i < results.getLength(); i++) {
      Element result = (Element) results.item(i);
      String location = result.getTextContent().trim();
      if ("redirect".equals(result.getAttribute("type"))
          && location.contains("/manage/")
          && !location.matches(".*/manage/(home|create)\\.do")
          && !location.matches(".*[?&]r=%\\{[^}]+}.*")) {
        missing.add(result.getAttribute("name") + " -> " + location);
      }
    }
    assertTrue(missing.isEmpty(), "Redirects without the r parameter: " + missing);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/struts-manage.xml", "/struts-admin.xml"})
  public void actionsKeepRoleInterceptors(String config) throws Exception {
    Document doc = parse(config);

    List<String> missing = new ArrayList<>();
    NodeList actions = doc.getElementsByTagName("action");
    for (int i = 0; i < actions.getLength(); i++) {
      Element action = (Element) actions.item(i);
      List<String> refs = new ArrayList<>();
      NodeList children = action.getChildNodes();
      for (int j = 0; j < children.getLength(); j++) {
        Node child = children.item(j);
        if (child instanceof Element el && "interceptor-ref".equals(el.getTagName())) {
          refs.add(el.getAttribute("name"));
        }
      }
      // no interceptor-ref: the package default (managerStack/adminStack) applies
      if (!refs.isEmpty() && refs.stream().noneMatch(ROLE_INTERCEPTORS::contains)) {
        missing.add(action.getAttribute("name") + " " + refs);
      }
    }
    assertTrue(missing.isEmpty(),
        config + " has actions without a role interceptor: " + missing);
  }
}
