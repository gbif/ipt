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
package org.gbif.provider.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * TODO: Documentation.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(inheritLocations = true, locations = {
    "classpath:/applicationContext-resources.xml",
    "classpath:/applicationContext-dao.xml",
    "classpath:/applicationContext-service.xml",
    "classpath:/applicationContext.xml", "classpath:/applicationContext*.xml"})
@Transactional
@TransactionConfiguration
public class TransactionalTestBase extends
    AbstractTransactionalJUnit4SpringContextTests {
  protected final Log log = LogFactory.getLog(getClass());

  @Override
  @Autowired
  public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
    this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
  }
}
