package org.appfuse.service;

import org.gbif.provider.util.ConvertUtil;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public abstract class BaseManagerTestCase extends
    AbstractTransactionalDataSourceSpringContextTests {
  // ~ Static fields/initializers =============================================

  protected final Log log = LogFactory.getLog(getClass());
  protected static ResourceBundle rb = null;

  public BaseManagerTestCase() {
    // Since a ResourceBundle is not required for each class, just
    // do a simple check to see if one exists
    String className = this.getClass().getName();

    try {
      rb = ResourceBundle.getBundle(className);
    } catch (MissingResourceException mre) {
      // log.warn("No resource bundle found for: " + className);
    }
  }

  // ~ Constructors ===========================================================

  @Override
  protected String[] getConfigLocations() {
    setAutowireMode(AUTOWIRE_BY_NAME);
    return new String[] {
        "classpath:/applicationContext-filtered.xml",
        "classpath:/applicationContext.xml"};
    // classpath*:/**/applicationContext.xml has to be used since this file does
    // not
    // exist in AppFuse, but may exist in projects that depend on it
  }

  // ~ Methods ================================================================

  /**
   * Utility method to populate a javabean-style object with values from a
   * Properties file
   * 
   * @param obj the model object to populate
   * @return Object populated object
   * @throws Exception if BeanUtils fails to copy properly
   */
  protected Object populate(Object obj) throws Exception {
    // loop through all the beans methods and set its properties from
    // its .properties file
    Map map = ConvertUtil.convertBundleToMap(rb);

    BeanUtils.copyProperties(obj, map);

    return obj;
  }
}