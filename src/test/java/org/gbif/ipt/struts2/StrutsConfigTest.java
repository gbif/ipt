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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the interceptor configuration of the Struts packages.
 * Note: declaring any interceptor-ref on an action replaces the package default interceptor stack.
 */
public class StrutsConfigTest {

  private static final Set<String> ROLE_INTERCEPTORS = Set.of(
      "managerStack", "managerStackWithoutLock", "managerAjaxStack", "requireManager",
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

  /**
   * @return the interceptors of the stack in order. A stack defined in the same document is expanded in place: its name
   * is kept, followed by its interceptors, so both can be looked up. Stacks from struts-default.xml, e.g. defaultStack,
   * are not expanded.
   */
  private static List<String> stackRefs(Document doc, String stackName) {
    Element stack = findStack(doc, stackName);
    if (stack == null) {
      throw new AssertionError("Stack not found: " + stackName);
    }
    List<String> refs = new ArrayList<>();
    NodeList children = stack.getElementsByTagName("interceptor-ref");
    for (int j = 0; j < children.getLength(); j++) {
      String name = ((Element) children.item(j)).getAttribute("name");
      refs.add(name);
      if (findStack(doc, name) != null) {
        refs.addAll(stackRefs(doc, name));
      }
    }
    return refs;
  }

  private static Element findStack(Document doc, String stackName) {
    NodeList stacks = doc.getElementsByTagName("interceptor-stack");
    for (int i = 0; i < stacks.getLength(); i++) {
      Element stack = (Element) stacks.item(i);
      if (stackName.equals(stack.getAttribute("name"))) {
        return stack;
      }
    }
    return null;
  }

  /**
   * The role and resource interceptors run after iptContextStack, so the pages they return have the session, the CSRF
   * token and the user's language, and before the interceptors that set parameters and call prepare().
   */
  @ParameterizedTest
  @CsvSource({"portalStack,protectPrivateResource,iptStackWithoutSetup",
      "managerStack,requireManager,iptStackWithoutSetup",
      "managerStackWithoutLock,requireManager,iptStackWithoutSetup",
      "adminStack,requireAdmin,iptStackWithoutSetup",
      "managerAjaxStack,requireManager,i18nStack",
      "adminAjaxStack,requireAdmin,i18nStack"})
  public void interceptorOrder(String stack, String interceptor, String paramsStack) throws Exception {
    List<String> refs = stackRefs(parse("/struts.xml"), stack);
    int idx = refs.indexOf(interceptor);
    assertTrue(idx >= 0, stack + " misses " + interceptor);
    assertTrue(refs.indexOf("iptContextStack") >= 0, stack + " misses iptContextStack");
    assertTrue(refs.indexOf("iptContextStack") < idx, stack + ": " + refs);
    assertTrue(idx < refs.indexOf(paramsStack), stack + ": " + refs);
  }

  /**
   * Every stack built on iptContextStack starts with the exception interceptor, so an exception in IPT's own
   * interceptors (setupAndCancel, csrf, the role, resource and lock checks) gives the error page, not a bare servlet
   * container 500. The exception interceptor in defaultStack and i18nStack runs after them and can't catch it.
   */
  @Test
  public void exceptionInterceptorFirstInStacks() throws Exception {
    Document doc = parse("/struts.xml");

    Element exception = null;
    NodeList interceptors = doc.getElementsByTagName("interceptor");
    for (int i = 0; i < interceptors.getLength(); i++) {
      Element interceptor = (Element) interceptors.item(i);
      if ("iptException".equals(interceptor.getAttribute("name"))) {
        exception = interceptor;
      }
    }
    assertNotNull(exception, "iptException interceptor not declared");
    assertEquals("org.apache.struts2.interceptor.ExceptionMappingInterceptor", exception.getAttribute("class"));

    List<String> stacksChecked = new ArrayList<>();
    NodeList stacks = doc.getElementsByTagName("interceptor-stack");
    for (int i = 0; i < stacks.getLength(); i++) {
      String stack = ((Element) stacks.item(i)).getAttribute("name");
      List<String> refs = stackRefs(doc, stack);
      if (!"iptContextStack".equals(stack) && refs.contains("iptContextStack")) {
        assertEquals("iptException", refs.get(0), stack + ": " + refs);
        stacksChecked.add(stack);
      }
    }
    assertTrue(stacksChecked.containsAll(List.of("portalStack", "adminStack", "managerStack",
        "managerStackWithoutLock", "managerAjaxStack", "adminAjaxStack")), "checked: " + stacksChecked);
  }

  /**
   * Exceptions are mapped to the "exception" result, which only ipt-default defines. A result for it in a package or
   * an action would take precedence, as an action's own "error" result (a page or a redirect for a failed
   * operation) did, and the exception would render an unprepared page or be lost in a redirect.
   */
  @Test
  public void exceptionsGiveTheErrorPage() throws Exception {
    List<String> definitions = new ArrayList<>();
    for (String config : List.of("/struts.xml", "/struts-portal.xml", "/struts-manage.xml", "/struts-admin.xml")) {
      Document doc = parse(config);
      NodeList mappings = doc.getElementsByTagName("exception-mapping");
      for (int i = 0; i < mappings.getLength(); i++) {
        Element mapping = (Element) mappings.item(i);
        assertEquals("exception", mapping.getAttribute("result"), config + ": " + mapping.getAttribute("exception"));
      }
      NodeList results = doc.getElementsByTagName("result");
      for (int i = 0; i < results.getLength(); i++) {
        Element result = (Element) results.item(i);
        if ("exception".equals(result.getAttribute("name"))) {
          Element parent = (Element) result.getParentNode();
          Element pkg = (Element) parent.getParentNode();
          definitions.add(config + " " + pkg.getAttribute("name") + " " + parent.getTagName());
          assertTrue(result.getTextContent().contains("/WEB-INF/pages/error/error.ftl"), config);
        }
      }
    }
    assertEquals(List.of("/struts.xml ipt-default global-results"), definitions);
  }

  /**
   * The CSRF interceptor is part of iptContextStack, so it runs before parameters are set, and only once per stack.
   * It runs after the session and locale interceptors, so its 403 and 405 pages have the session and the user's
   * language.
   */
  @Test
  public void csrfInterceptorInStacks() throws Exception {
    Document doc = parse("/struts.xml");
    List<String> context = stackRefs(doc, "iptContextStack");
    assertTrue(context.contains("csrf"), "iptContextStack: " + context);
    for (String before : List.of("servletConfig", "defaultLocale", "i18n")) {
      assertTrue(context.contains(before), "iptContextStack: " + context);
      assertTrue(context.indexOf(before) < context.indexOf("csrf"), "iptContextStack: " + context);
    }
    assertFalse(stackRefs(doc, "iptStackWithoutSetup").contains("csrf"));
    for (String stack : List.of("portalStack", "adminStack", "managerStack", "managerStackWithoutLock",
        "managerAjaxStack", "adminAjaxStack")) {
      List<String> refs = stackRefs(doc, stack);
      assertEquals(refs.indexOf("csrf"), refs.lastIndexOf("csrf"), stack + ": " + refs);
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

  /**
   * The lock check runs right after the role check, before parameters are set and prepare() runs: these change the
   * resource in memory, which publishing reads. managerStackWithoutLock is the same stack without the check.
   */
  @Test
  public void lockCheckBeforeParameters() throws Exception {
    Document doc = parse("/struts.xml");
    List<String> refs = stackRefs(doc, "managerStack");
    assertEquals(refs.indexOf("requireManager") + 1, refs.indexOf("resourceLocked"), "managerStack: " + refs);
    assertTrue(refs.indexOf("resourceLocked") < refs.indexOf("iptStackWithoutSetup"), "managerStack: " + refs);

    List<String> withoutLock = new ArrayList<>(refs);
    withoutLock.remove("resourceLocked");
    assertEquals(withoutLock, stackRefs(doc, "managerStackWithoutLock"));
  }

  /**
   * The locked result redirects to the locked page. That action must not run the lock check itself, or the redirect
   * would point to itself until publishing ends.
   */
  @Test
  public void lockedPageSkipsLockCheck() throws Exception {
    Document struts = parse("/struts.xml");
    Document manage = parse("/struts-manage.xml");
    assertTrue(stackRefs(struts, "managerStack").contains("resourceLocked"));

    String target = null;
    NodeList results = manage.getElementsByTagName("result");
    for (int i = 0; i < results.getLength(); i++) {
      Element result = (Element) results.item(i);
      if ("locked".equals(result.getAttribute("name"))
          && "global-results".equals(((Element) result.getParentNode()).getTagName())) {
        target = StringUtils.substringBetween(result.getTextContent(), "/manage/", ".do");
      }
    }
    assertNotNull(target, "No global locked result in struts-manage.xml");

    Element lockedAction = null;
    NodeList actions = manage.getElementsByTagName("action");
    for (int i = 0; i < actions.getLength(); i++) {
      Element action = (Element) actions.item(i);
      if (target.equals(action.getAttribute("name"))) {
        lockedAction = action;
      }
    }
    assertNotNull(lockedAction, "No action " + target + " in struts-manage.xml");

    List<String> refs = new ArrayList<>();
    NodeList children = lockedAction.getChildNodes();
    for (int j = 0; j < children.getLength(); j++) {
      if (children.item(j) instanceof Element el && "interceptor-ref".equals(el.getTagName())) {
        refs.add(el.getAttribute("name"));
      }
    }
    assertEquals(List.of("managerStackWithoutLock"), refs, target + " interceptors");
    assertFalse(stackRefs(struts, "managerStackWithoutLock").contains("resourceLocked"));
  }

  /**
   * An action whose stack runs the lock check needs a locked result, in the action or in its package's global results.
   * Without one, a request while the resource is being published fails with "No result defined" (500).
   */
  @ParameterizedTest
  @ValueSource(strings = {"/struts.xml", "/struts-portal.xml", "/struts-manage.xml", "/struts-admin.xml"})
  public void lockCheckHasLockedResult(String config) throws Exception {
    Document struts = parse("/struts.xml");
    Document doc = parse(config);

    List<String> missing = new ArrayList<>();
    NodeList packages = doc.getElementsByTagName("package");
    for (int i = 0; i < packages.getLength(); i++) {
      Element pkg = (Element) packages.item(i);
      String defaultStack = null;
      boolean globalLocked = false;
      NodeList pkgChildren = pkg.getChildNodes();
      for (int j = 0; j < pkgChildren.getLength(); j++) {
        if (pkgChildren.item(j) instanceof Element el) {
          if ("default-interceptor-ref".equals(el.getTagName())) {
            defaultStack = el.getAttribute("name");
          } else if ("global-results".equals(el.getTagName())) {
            globalLocked = hasResult(el, "locked");
          }
        }
      }

      NodeList actions = pkg.getElementsByTagName("action");
      for (int j = 0; j < actions.getLength(); j++) {
        Element action = (Element) actions.item(j);
        List<String> refs = new ArrayList<>();
        NodeList children = action.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
          if (children.item(k) instanceof Element el && "interceptor-ref".equals(el.getTagName())) {
            refs.add(el.getAttribute("name"));
          }
        }
        if (refs.isEmpty() && defaultStack != null) {
          refs.add(defaultStack);
        }
        boolean lockCheck = refs.stream()
            .anyMatch(r -> "resourceLocked".equals(r)
                || (findStack(struts, r) != null && stackRefs(struts, r).contains("resourceLocked")));
        if (lockCheck && !globalLocked && !hasResult(action, "locked")) {
          missing.add(pkg.getAttribute("name") + "/" + action.getAttribute("name"));
        }
      }
    }
    assertTrue(missing.isEmpty(), config + " has actions with a lock check but no locked result: " + missing);
  }

  private static boolean hasResult(Element parent, String name) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element el && "result".equals(el.getTagName())
          && name.equals(el.getAttribute("name"))) {
        return true;
      }
    }
    return false;
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
