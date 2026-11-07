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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  /**
   * Actions mapped to a method other than execute() that may be requested with GET, because they don't change state
   * or only change it on POST. Every other such method must be annotated with {@link RequirePost}.
   */
  private static final Map<String, Set<String>> READ_ONLY_METHOD_MAPPINGS = Map.of(
      "/struts.xml", Set.of(
          // show the form on GET and only save on POST
          "setupDataDirectory", "setupDefaultAdministrator", "setupMode", "setupPublicUrl",
          // redirect target of the last setup step; logs the new admin in once, see SetupAction
          "setupInstallationComplete",
          // shows the form on GET; a POST is checked against the login token by LoginAction
          "login",
          "rss"),
      "/struts-manage.xml", Set.of(
          "locked", "raw-source", "eml", "peek", "detail", "report", "mappingPeek", "dataPackageMappingSourceNew",
          // change the translation being edited, which is only saved by a form
          "translationReload", "translationAutomap", "dataPackageFieldTranslationReload",
          "dataPackageFieldTranslationAutomap",
          // linked from the publication status message (manage.locked); restorable by publishing again
          "cancel",
          // maps to a method that doesn't exist
          "canceloverwrite"),
      "/struts-admin.xml", Set.of("users", "extensions", "dataPackages", "logfile", "organisations"));

  /**
   * Manage and admin actions that change state in execute() without checking for a POST themselves.
   */
  private static final Map<String, Set<String>> STATE_CHANGING_EXECUTE_MAPPINGS = Map.of(
      "/struts-admin.xml", Set.of("publishAll", "deleteVersion"));

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
   * The CSRF interceptor runs before parameters are set, and in the AJAX stacks after the role check.
   */
  @Test
  public void csrfInterceptorInStacks() throws Exception {
    Document doc = parse("/struts.xml");
    assertEquals("csrf", stackRefs(doc, "iptStackWithoutSetup").get(0));
    for (String stack : List.of("managerAjaxStack", "adminAjaxStack")) {
      List<String> refs = stackRefs(doc, stack);
      assertTrue(refs.indexOf("csrf") > 0, stack + ": " + refs);
      assertTrue(refs.indexOf("csrf") < refs.indexOf("i18nStack"), stack + ": " + refs);
    }
  }

  /**
   * Actions mapped to a state-changing method only accept POST, so they can't be triggered by a link.
   */
  @ParameterizedTest
  @ValueSource(strings = {"/struts.xml", "/struts-manage.xml", "/struts-admin.xml"})
  public void stateChangingActionsRequirePost(String config) throws Exception {
    Document doc = parse(config);
    Set<String> readOnly = READ_ONLY_METHOD_MAPPINGS.getOrDefault(config, Set.of());
    Set<String> stateChangingExecute = STATE_CHANGING_EXECUTE_MAPPINGS.getOrDefault(config, Set.of());

    List<String> missing = new ArrayList<>();
    NodeList actions = doc.getElementsByTagName("action");
    for (int i = 0; i < actions.getLength(); i++) {
      Element action = (Element) actions.item(i);
      String name = action.getAttribute("name");
      String method = action.getAttribute("method");
      if (readOnly.contains(name) || (method.isEmpty() && !stateChangingExecute.contains(name))) {
        continue;
      }

      List<String> methods = new ArrayList<>();
      if (method.isEmpty()) {
        methods.add("execute");
      } else if (method.contains("{1}")) {
        NodeList allowed = action.getElementsByTagName("allowed-methods");
        assertEquals(1, allowed.getLength(), name + " needs allowed-methods");
        Arrays.stream(allowed.item(0).getTextContent().split(","))
            .map(String::trim)
            .filter(StringUtils::isNotEmpty)
            .forEach(methods::add);
      } else {
        methods.add(method);
      }

      Class<?> actionClass = Class.forName(action.getAttribute("class"));
      for (String m : methods) {
        Method javaMethod = MethodUtils.getMatchingAccessibleMethod(actionClass, m);
        // an allowed method that doesn't exist can't be invoked anyway
        if (javaMethod != null && MethodUtils.getAnnotation(javaMethod, RequirePost.class, true, true) == null) {
          missing.add(name + " -> " + actionClass.getSimpleName() + "." + m + "()");
        }
      }
    }
    assertTrue(missing.isEmpty(), config + " has state-changing actions without @RequirePost "
        + "(annotate them or add read-only ones to READ_ONLY_METHOD_MAPPINGS): " + missing);
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
