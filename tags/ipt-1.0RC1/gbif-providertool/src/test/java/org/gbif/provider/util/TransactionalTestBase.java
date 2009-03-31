package org.gbif.provider.util;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(inheritLocations=true, locations={"classpath:/applicationContext-resources.xml","classpath:/applicationContext-dao.xml","classpath:/applicationContext-service.xml","classpath:/applicationContext.xml","classpath:/applicationContext*.xml"})
@Transactional
@TransactionConfiguration
public class TransactionalTestBase extends AbstractTransactionalJUnit4SpringContextTests{
    protected final Log log = LogFactory.getLog(getClass());
    
    @Autowired
    public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
    	this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }
}
