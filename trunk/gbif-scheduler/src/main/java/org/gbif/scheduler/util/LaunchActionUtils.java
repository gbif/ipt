/**
 * 
 */
package org.gbif.scheduler.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.gbif.scheduler.model.LaunchAction;

/**
 * Utilities to simplify working with a launch action
 * @author timrobertson
 */
public class LaunchActionUtils {
	protected static Log log = LogFactory.getLog(LaunchActionUtils.class);
	
	/**
	 * Executes the action provided from the instance object
	 * @param callingInstance The object that is calling the action (typically use 'this')
	 * @param launchAction The action to be executed
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws InstantiationException 
	 */
	public static void execute(Object callingInstance, LaunchAction launchAction) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, ClassNotFoundException {
		String methodName = launchAction.getMethodName();
		Object actionInstance = null;
		Object[] args = null;
		
		if (StringUtils.isNotBlank(launchAction.getInstanceParam())) {
			log.debug("Launching using an instance of the action named [" + launchAction.getInstanceParam() + "] on instance: " + callingInstance.getClass());
			actionInstance = PropertyUtils.getProperty(callingInstance, launchAction.getInstanceParam());
			
		} else if (StringUtils.isNotBlank(launchAction.getFullClassName())) {
			log.debug("Launching by creating a new instance of the action: " + launchAction.getFullClassName());
			actionInstance = Class.forName(launchAction.getFullClassName()).newInstance();
			
		} else {
			throw new InvocationTargetException(null, "Cannot launch an action since no instanceParam or fullClassName is not set: " + launchAction.toString());
		}
		
		if (actionInstance == null) {
			throw new InvocationTargetException(null, "Cannot launch an action since no instanceParam or fullClassName was found to be null: " + launchAction.toString());
		}	
		
		if (StringUtils.isNotBlank(launchAction.getMethodParams())) {
				args = propertyValuesFromInstance(callingInstance, launchAction.getMethodParams());
		}
		
		if (log.isDebugEnabled()) {
			if (args != null) {
				StringBuffer sb = new StringBuffer();
				for (Object o : args) {
					sb.append("[" + o + "]");
				}
				log.debug("Executing [" + methodName + "] on [" + actionInstance.getClass() + "] with args: " + sb.toString());
			} else {
				log.debug("Executing [" + methodName + "] on [" + actionInstance.getClass() + "] with no args");
			}
		}
		
        
        for (Method m : actionInstance.getClass().getMethods()) {
        	if (StringUtils.equals(m.getName(), methodName)
        			&& args == null
        			&& m.getParameterTypes().length==0) {
        		m.invoke(actionInstance, new Object[0]);
        		
        	} else if (StringUtils.equals(m.getName(), methodName)
        			&& m.getParameterTypes() != null 
        			&& args != null
        			&& m.getParameterTypes().length == args.length) {
        		m.invoke(actionInstance, args);
        	}
        }
	}
	
	
	protected static String[] commaStringToStringArray(String commaString) {
		return commaString.split(",");
	}
	
	protected static Object[] propertyValuesFromInstance(Object instance, String[] propertyNames) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object[] properties = new Object[propertyNames.length];
		int i=0;
		for (String propertyName : propertyNames) {
			log.debug("Getting property: " + propertyName);
			
			if (!StringUtils.equalsIgnoreCase("NULL", propertyName)) {
				Object value = PropertyUtils.getProperty(instance, propertyName);
				log.debug("Property: " + propertyName + " is: " + value.getClass());
				if (value != null) {
					properties[i++] = value; 
				} else {
					properties[i++] = null;
				}
			} else {
				properties[i++] = null;
			}
		}
		return properties;
	}
	
	protected static Object[] propertyValuesFromInstance(Object instance, String commaString) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return propertyValuesFromInstance(instance, commaStringToStringArray(commaString));
	}
}
