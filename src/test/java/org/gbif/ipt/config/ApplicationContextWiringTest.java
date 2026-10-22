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
package org.gbif.ipt.config;

import org.gbif.ipt.service.manage.ResourceDataTableViewManager;
import org.gbif.ipt.service.manage.ResourceDoiService;
import org.gbif.ipt.service.manage.ResourceImportService;
import org.gbif.ipt.service.manage.ResourceLoader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.service.manage.ResourcePersister;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.service.manage.ResourceTypeService;
import org.gbif.ipt.service.manage.ResourceVersioningService;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.config.ProviderCreatingFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks the bean definitions of applicationContext.xml against the classes they configure, without creating any bean:
 * constructor arguments and properties must match, in order, a constructor or setter of the bean class, and the
 * dependencies Struts autowires into actions must resolve to exactly one bean.
 */
public class ApplicationContextWiringTest {

  private static final String[] STRUTS_FILES = {"struts.xml", "struts-portal.xml", "struts-manage.xml", "struts-admin.xml"};

  private static DefaultListableBeanFactory beanFactory;
  private static Map<String, Class<?>> beanTypes;

  @BeforeAll
  public static void loadBeanDefinitions() {
    beanFactory = new DefaultListableBeanFactory();
    new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));

    beanTypes = new LinkedHashMap<>();
    for (String name : beanFactory.getBeanDefinitionNames()) {
      beanTypes.put(name, resolveType(name));
    }
  }

  @Test
  public void testConstructorArguments() {
    List<String> problems = new ArrayList<>();
    for (String name : beanFactory.getBeanDefinitionNames()) {
      BeanDefinition definition = beanFactory.getBeanDefinition(name);
      ConstructorArgumentValues arguments = definition.getConstructorArgumentValues();
      if (arguments.isEmpty()) {
        continue;
      }
      assertTrue(arguments.getIndexedArgumentValues().isEmpty(), name + " uses indexed constructor arguments");

      Class<?> beanClass = beanClass(definition);
      List<ConstructorArgumentValues.ValueHolder> values = arguments.getGenericArgumentValues();
      boolean matches = Arrays.stream(beanClass.getConstructors())
          .filter(c -> c.getParameterCount() == values.size())
          .anyMatch(c -> argumentsMatch(c, values));
      if (!matches) {
        problems.add(name + " (" + beanClass.getSimpleName() + "): no public constructor matches "
            + values.stream().map(v -> describe(v.getValue())).collect(Collectors.joining(", ", "(", ")")));
      }
    }
    assertTrue(problems.isEmpty(), String.join("\n", problems));
  }

  @Test
  public void testPropertyReferences() {
    List<String> problems = new ArrayList<>();
    for (String name : beanFactory.getBeanDefinitionNames()) {
      BeanDefinition definition = beanFactory.getBeanDefinition(name);
      Class<?> beanClass = beanClass(definition);
      if (isSpringFactoryBean(beanClass)) {
        continue;
      }
      MutablePropertyValues properties = definition.getPropertyValues();
      for (PropertyValue property : properties.getPropertyValues()) {
        if (!(property.getValue() instanceof RuntimeBeanReference reference)) {
          continue;
        }
        Class<?> referencedType = beanTypes.get(reference.getBeanName());
        if (referencedType == null) {
          // provided by the web application context, e.g. servletContext
          continue;
        }
        String setter = "set" + Character.toUpperCase(property.getName().charAt(0)) + property.getName().substring(1);
        boolean found = Arrays.stream(beanClass.getMethods())
            .filter(m -> m.getName().equals(setter) && m.getParameterCount() == 1)
            .anyMatch(m -> m.getParameterTypes()[0].isAssignableFrom(referencedType));
        if (!found) {
          problems.add(name + "." + property.getName() + ": no setter for " + referencedType.getSimpleName());
        }
      }
    }
    assertTrue(problems.isEmpty(), String.join("\n", problems));
  }

  @Test
  public void testReferencesExist() {
    Set<String> missing = new LinkedHashSet<>();
    for (String name : beanFactory.getBeanDefinitionNames()) {
      BeanDefinition definition = beanFactory.getBeanDefinition(name);
      Stream.concat(
              definition.getConstructorArgumentValues().getGenericArgumentValues().stream().map(v -> v.getValue()),
              definition.getPropertyValues().getPropertyValueList().stream().map(PropertyValue::getValue))
          .filter(RuntimeBeanReference.class::isInstance)
          .map(v -> ((RuntimeBeanReference) v).getBeanName())
          .filter(ref -> !beanFactory.containsBeanDefinition(ref))
          .forEach(ref -> missing.add(name + " -> " + ref));
    }
    // provided by the web application context
    missing.remove("iptModule -> servletContext");
    assertTrue(missing.isEmpty(), "Undefined beans: " + missing);
  }

  /**
   * Services split out of the resource manager are each defined once, so they can be autowired by type.
   */
  @ParameterizedTest
  @ValueSource(classes = {
      ResourceManager.class,
      ResourcePublicationManager.class,
      ResourceDataTableViewManager.class,
      ResourceDoiService.class,
      ResourceImportService.class,
      ResourceLoader.class,
      ResourcePersister.class,
      ResourceTypeService.class,
      ResourceVersioningService.class,
      ResourceMetadataLoader.class})
  public void testResourceServicesAreDefinedOnce(Class<?> type) {
    assertEquals(1, beansOfType(type).size(), type.getSimpleName() + " beans: " + beansOfType(type));
  }

  @Test
  public void testResourceManagerProvider() {
    assertEquals(Provider.class, beanTypes.get("resourceManagerProvider"));
    BeanDefinition definition = beanFactory.getBeanDefinition("resourceManagerProvider");
    assertEquals("resourceManager",
        ((TypedStringValue) definition.getPropertyValues().get("targetBeanName")).getValue());
  }

  /**
   * Struts creates actions through Spring, autowiring constructor parameters by type.
   */
  @Test
  public void testActionDependenciesResolveToOneBean() throws Exception {
    Set<Class<?>> actionClasses = strutsActionClasses();
    assertFalse(actionClasses.isEmpty());

    List<String> problems = new ArrayList<>();
    for (Class<?> actionClass : actionClasses) {
      Constructor<?> constructor = autowiredConstructor(actionClass);
      if (constructor == null) {
        continue;
      }
      for (Class<?> parameterType : constructor.getParameterTypes()) {
        List<String> candidates = beansOfType(parameterType).stream()
            // actions defined as beans aren't injected into other actions
            .filter(bean -> !beanTypes.get(bean).getName().startsWith("org.gbif.ipt.action."))
            .toList();
        if (candidates.size() != 1) {
          problems.add(actionClass.getSimpleName() + " needs " + parameterType.getSimpleName() + ": " + candidates);
        }
      }
    }
    assertTrue(problems.isEmpty(), String.join("\n", problems));
  }

  private static boolean argumentsMatch(Constructor<?> constructor, List<ConstructorArgumentValues.ValueHolder> values) {
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      Object value = values.get(i).getValue();
      if (value instanceof RuntimeBeanReference reference) {
        Class<?> referencedType = beanTypes.get(reference.getBeanName());
        if (referencedType == null || !wrap(parameterTypes[i]).isAssignableFrom(referencedType)) {
          return false;
        }
      }
      // literal values and expressions are converted by Spring
    }
    return true;
  }

  private static String describe(Object value) {
    if (value instanceof RuntimeBeanReference reference) {
      Class<?> type = beanTypes.get(reference.getBeanName());
      return reference.getBeanName() + ":" + (type == null ? "?" : type.getSimpleName());
    }
    return String.valueOf(value);
  }

  private static List<String> beansOfType(Class<?> type) {
    return beanTypes.entrySet().stream()
        .filter(e -> e.getValue() != null && type.isAssignableFrom(e.getValue()))
        .map(Map.Entry::getKey)
        .toList();
  }

  private static Class<?> resolveType(String name) {
    BeanDefinition definition = beanFactory.getBeanDefinition(name);
    Class<?> beanClass = beanClass(definition);
    if (MethodInvokingFactoryBean.class.equals(beanClass)) {
      RuntimeBeanReference target = (RuntimeBeanReference) definition.getPropertyValues().get("targetObject");
      String method = ((TypedStringValue) definition.getPropertyValues().get("targetMethod")).getValue();
      Class<?> targetClass = beanClass(beanFactory.getBeanDefinition(target.getBeanName()));
      return Arrays.stream(targetClass.getMethods())
          .filter(m -> m.getName().equals(method))
          .map(Method::getReturnType)
          .findFirst()
          .orElseThrow(() -> new AssertionError(name + ": no method " + method + " in " + targetClass));
    }
    if (ProviderCreatingFactoryBean.class.equals(beanClass)) {
      return Provider.class;
    }
    return beanClass;
  }

  private static Class<?> beanClass(BeanDefinition definition) {
    try {
      return Class.forName(definition.getBeanClassName());
    } catch (ClassNotFoundException e) {
      fail("Bean class not found: " + definition.getBeanClassName());
      return null;
    }
  }

  private static boolean isSpringFactoryBean(Class<?> beanClass) {
    return MethodInvokingFactoryBean.class.equals(beanClass) || ProviderCreatingFactoryBean.class.equals(beanClass);
  }

  private static Class<?> wrap(Class<?> type) {
    if (!type.isPrimitive()) {
      return type;
    }
    if (type == boolean.class) {
      return Boolean.class;
    }
    if (type == int.class) {
      return Integer.class;
    }
    if (type == long.class) {
      return Long.class;
    }
    return type;
  }

  private static Constructor<?> autowiredConstructor(Class<?> actionClass) {
    Constructor<?>[] constructors = actionClass.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(Inject.class)) {
        return constructor;
      }
    }
    return constructors.length == 1 && constructors[0].getParameterCount() > 0 ? constructors[0] : null;
  }

  private static Set<Class<?>> strutsActionClasses() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    DocumentBuilder builder = factory.newDocumentBuilder();

    Set<Class<?>> classes = new LinkedHashSet<>();
    for (String file : STRUTS_FILES) {
      try (InputStream in = ApplicationContextWiringTest.class.getClassLoader().getResourceAsStream(file)) {
        if (in == null) {
          fail(file + " not found");
        }
        Document document = builder.parse(in);
        NodeList actions = document.getElementsByTagName("action");
        for (int i = 0; i < actions.getLength(); i++) {
          String className = ((Element) actions.item(i)).getAttribute("class");
          if (className.startsWith("org.gbif.ipt.")) {
            classes.add(Class.forName(className));
          }
        }
      }
    }
    return classes;
  }
}
