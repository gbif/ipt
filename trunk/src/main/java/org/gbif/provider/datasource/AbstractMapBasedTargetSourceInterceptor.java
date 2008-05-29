package org.gbif.provider.datasource;

import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.InitializingBean;

	public abstract class AbstractMapBasedTargetSourceInterceptor implements MethodInterceptor, TargetSource, InitializingBean {
	    private Map targetMapping;
	    private final ThreadLocal targetInThread = new ThreadLocal();
	    private Class targetClass;

	    public void setTargetMapping(Map targetMapping) {
	        this.targetMapping = targetMapping;
	    }

	    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

	        Object key = getKey(methodInvocation);
	        if (key == null) {
	            throw new IllegalStateException("Key value cannot be null");
	        }

	        targetInThread.set(lookupTarget(key));
	        try {
	            return methodInvocation.proceed();
	        } finally {
	            targetInThread.set(null);
	        }
	    }

	    protected abstract Object getKey(MethodInvocation methodInvocation);

	    public void afterPropertiesSet() throws Exception {
	        if (targetMapping == null) {
	            throw new IllegalStateException("targetMapping is required");
	        }

	        if (targetClass == null) {
	            throw new IllegalStateException("targetClass is required");
	        }
	    }

	    public Class getTargetClass() {
	        return targetClass;
	    }

	    public void setTargetClass(Class targetClass) {
	        this.targetClass = targetClass;
	    }

	    public boolean isStatic() {
	        return false;
	    }

	    public Object getTarget() throws Exception {
	        return targetInThread.get();
	    }

	    private Object lookupTarget(Object key) {
	        if (key == null) {
	            throw new IllegalStateException("No key for look up");
	        }

	        Object value = targetMapping.get(key);
	        if (value.getClass() != getTargetClass()) {
	            throw new IllegalStateException("Mapped object isn't appropriate class.  Expecting: " + value.getClass().getName() + ". Actual: " + getTargetClass().getName());
	        }
	        return value;
	    }

	    public void releaseTarget(Object target) throws Exception {
	        // no-op
	    }
	}