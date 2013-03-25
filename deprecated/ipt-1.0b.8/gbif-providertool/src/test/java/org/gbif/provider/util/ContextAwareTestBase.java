package org.gbif.provider.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class ContextAwareTestBase extends AbstractDependencyInjectionSpringContextTests{
    protected final Log log = LogFactory.getLog(getClass());

    protected String[] getConfigLocations() {
        setAutowireMode(AUTOWIRE_BY_NAME);
        return new String[] {
                "classpath:/applicationContext-resources.xml",
                "classpath:/applicationContext-dao.xml",
                "classpath*:/applicationContext.xml", // for modular projects
                "classpath:**/applicationContext*.xml" // for web projects
            };
    }
}
