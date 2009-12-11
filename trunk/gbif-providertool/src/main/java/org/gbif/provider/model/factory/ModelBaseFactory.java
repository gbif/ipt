/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.factory;

import org.gbif.provider.model.BaseObject;
import org.gbif.provider.service.AnnotationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public class ModelBaseFactory<T extends BaseObject> {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired
  protected AnnotationManager annotationManager;

  public T copyPersistentProperties(T target, T source) {
    Class recClass = target.getClass();
    for (Method getter : recClass.getMethods()) {
      try {
        String methodName = getter.getName();
        if ((methodName.startsWith("get") || methodName.startsWith("is"))
            && !methodName.equals("getClass")
            && !getter.isAnnotationPresent(Transient.class)) {
          String setterName;
          if (methodName.startsWith("get")) {
            setterName = "set" + methodName.substring(3);
          } else {
            setterName = "set" + methodName.substring(2);
          }
          Class returnType = getter.getReturnType();
          try {
            Method setter = recClass.getMethod(setterName, returnType);
            setter.invoke(target, getter.invoke(source));
          } catch (NoSuchMethodException e) {
            e.printStackTrace();
          } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    return target;
  }
}
