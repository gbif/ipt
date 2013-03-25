package org.gbif.provider.model.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.BaseObject;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.AnnotationManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelBaseFactory<T extends BaseObject> {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired
	protected AnnotationManager annotationManager;

	public T copyPersistentProperties(T target, T source) {
			Class recClass = target.getClass();
			for (Method getter : recClass.getMethods()){
				try {
					String methodName = getter.getName();
					if ( (methodName.startsWith("get") || methodName.startsWith("is")) && !methodName.equals("getClass")  && !getter.isAnnotationPresent(Transient.class)){
						String setterName;
						if (methodName.startsWith("get")){
							setterName = "set"+methodName.substring(3);
						}else{
							setterName = "set"+methodName.substring(2);
						}
						Class returnType = getter.getReturnType();
						try{
							Method setter = recClass.getMethod(setterName, returnType);
							setter.invoke(target, getter.invoke(source));
						} catch (NoSuchMethodException e){
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
