package org.gbif.provider.datasource;

import org.aopalliance.intercept.MethodInvocation;

/**
 * multiple user selectable datasources per ThreadLocal.
 * @See http://forum.springframework.org/showthread.php?t=24300
 * Spring example setup:
 * 
<bean id="targetMapping" class="DataSourceLookup">
  <description>Creates a map of datasources</description>
</bean>

<bean id="dataSourceTarget" class="DataSourceTargetSourceInterceptor">
  <property name="targetClass" value="javax.sql.DataSource"/>
  <property name="targetMapping" ref="targetMapping"/>  
</bean>

<bean id="myDataSource" class="org.springframework.aop.framework.ProxyFactoryBean">
  <property name="proxyInterfaces" value="javax.sql.DataSource"/>
  <property name="targetSource" ref="dataSourceTarget"/>
</bean>

<bean id="myDaoTarget" class="myDaoImpl">
  <property name="dataSource" ref="myDataSource"/>
</bean>

<bean id="myDao" class="org.springframework.aop.framework.ProxyFactoryBean">
  <property name="target" ref="myDaoTarget"/>
  <property name="interceptorNames"><idref local="dataSourceTarget"/></property>
</bean>
 *
 */
public class DataSourceTargetSourceInterceptor extends AbstractMapBasedTargetSourceInterceptor {
    /**
     * Return first argument as the key
     * @param methodInvocation
     * @return
     */
    protected Object getKey(MethodInvocation methodInvocation) {
        return methodInvocation.getArguments()[0];
    }
}